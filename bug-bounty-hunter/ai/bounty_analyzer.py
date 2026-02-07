"""
Bounty Analyzer - AI-powered bounty potential analysis
"""
import torch
import torch.nn as nn
from typing import Dict, List
import numpy as np
from dataclasses import dataclass
import re
from loguru import logger


@dataclass
class AnalysisResult:
    score: float
    recommended: bool
    reasoning: str
    estimated_time: str
    potential_reward: str
    risk_level: str
    skill_match: float


class BountyAnalyzer:
    """
    Analyzes targets and determines their potential for bug bounty hunting
    """
    
    def __init__(self, config: Dict = None):
        self.config = config or {}
        self.min_confidence = self.config.get('min_confidence', 0.7)
        
        # Feature weights
        self.weights = {
            'reward_amount': 0.3,
            'skill_match': 0.25,
            'time_available': 0.15,
            'program_maturity': 0.15,
            'scope_size': 0.15
        }
        
        # User profile (should be loaded from config)
        self.user_skills = set([
            'XSS', 'SQLi', 'IDOR', 'SSRF', 'Authentication'
        ])
        
    def evaluate_target(self, target: str, context: Dict = None) -> AnalysisResult:
        """
        Evaluate if a target will help in bug bounty hunting
        
        Args:
            target: URL, domain, or description of the target
            context: Additional context (program details, rewards, etc.)
        """
        context = context or {}
        
        # Extract features
        features = self._extract_features(target, context)
        
        # Calculate score
        score = self._calculate_score(features)
        
        # Determine recommendation
        recommended = score >= (self.min_confidence * 100)
        
        # Generate reasoning
        reasoning = self._generate_reasoning(features, score)
        
        # Estimate time and reward
        estimated_time = self._estimate_time(features)
        potential_reward = self._estimate_reward(features)
        
        # Assess risk
        risk_level = self._assess_risk(features)
        
        return AnalysisResult(
            score=round(score, 2),
            recommended=recommended,
            reasoning=reasoning,
            estimated_time=estimated_time,
            potential_reward=potential_reward,
            risk_level=risk_level,
            skill_match=features['skill_match']
        )
    
    def _extract_features(self, target: str, context: Dict) -> Dict:
        """Extract features from target for analysis"""
        features = {
            'has_web_interface': False,
            'is_api': False,
            'platform': None,
            'domain_age': 0,
            'tech_stack': [],
            'reward_min': 0,
            'reward_max': 0,
            'scope_size': 'medium',
            'skill_match': 0.0,
            'program_maturity': 'unknown',
            'response_time': 'unknown'
        }
        
        # Analyze target URL/domain
        target_lower = target.lower()
        
        # Check for web interface
        if target.startswith('http'):
            features['has_web_interface'] = True
        
        # Check if it's an API
        if '/api' in target_lower or 'api.' in target_lower:
            features['is_api'] = True
        
        # Detect bounty platform
        platform_indicators = {
            'hackerone': 'hackerone.com',
            'bugcrowd': 'bugcrowd.com',
            'intigriti': 'intigriti.com',
            'yeswehack': 'yeswehack.com'
        }
        
        for platform, indicator in platform_indicators.items():
            if indicator in target_lower:
                features['platform'] = platform
                break
        
        # Extract context features
        if context:
            features['reward_min'] = context.get('reward_min', 0)
            features['reward_max'] = context.get('reward_max', 0)
            features['scope_size'] = context.get('scope_size', 'medium')
            features['program_maturity'] = context.get('maturity', 'unknown')
            features['tech_stack'] = context.get('tech_stack', [])
        
        # Calculate skill match
        if 'vulnerabilities' in context:
            target_vulns = set(context['vulnerabilities'])
            features['skill_match'] = len(target_vulns & self.user_skills) / len(self.user_skills)
        else:
            # Default moderate match
            features['skill_match'] = 0.5
        
        return features
    
    def _calculate_score(self, features: Dict) -> float:
        """Calculate overall bounty potential score (0-100)"""
        score = 0.0
        
        # Reward amount score
        if features['reward_max'] > 0:
            reward_score = min(features['reward_max'] / 5000, 1.0) * 100
            score += reward_score * self.weights['reward_amount']
        else:
            score += 50 * self.weights['reward_amount']  # Default mid-range
        
        # Skill match score
        skill_score = features['skill_match'] * 100
        score += skill_score * self.weights['skill_match']
        
        # Scope size score
        scope_scores = {'small': 60, 'medium': 80, 'large': 100}
        scope_score = scope_scores.get(features['scope_size'], 70)
        score += scope_score * self.weights['scope_size']
        
        # Program maturity score
        maturity_scores = {'new': 90, 'mature': 60, 'established': 50, 'unknown': 70}
        maturity_score = maturity_scores.get(features['program_maturity'], 70)
        score += maturity_score * self.weights['program_maturity']
        
        # Time availability (assume moderate for now)
        score += 75 * self.weights['time_available']
        
        # Bonus points
        if features['has_web_interface']:
            score += 5
        if features['platform']:
            score += 5
        if features['is_api']:
            score += 3
        
        return min(score, 100)
    
    def _generate_reasoning(self, features: Dict, score: float) -> str:
        """Generate human-readable reasoning for the score"""
        reasons = []
        
        if score >= 80:
            reasons.append("High potential target with favorable characteristics")
        elif score >= 60:
            reasons.append("Moderate potential target worth investigating")
        else:
            reasons.append("Lower potential - may not be the best use of time")
        
        if features['platform']:
            reasons.append(f"Recognized bounty platform ({features['platform']})")
        
        if features['skill_match'] > 0.7:
            reasons.append("Strong skill match with your expertise")
        elif features['skill_match'] < 0.3:
            reasons.append("Limited skill match - may require learning new techniques")
        
        if features['reward_max'] > 2000:
            reasons.append(f"High reward potential (up to ${features['reward_max']})")
        
        if features['program_maturity'] == 'new':
            reasons.append("New program - higher chance of finding vulnerabilities")
        elif features['program_maturity'] == 'established':
            reasons.append("Established program - competition may be higher")
        
        if features['has_web_interface']:
            reasons.append("Web interface available for testing")
        
        return ". ".join(reasons)
    
    def _estimate_time(self, features: Dict) -> str:
        """Estimate time required for initial assessment"""
        base_hours = 2
        
        if features['scope_size'] == 'large':
            base_hours *= 2
        elif features['scope_size'] == 'small':
            base_hours *= 0.5
        
        if features['skill_match'] < 0.5:
            base_hours *= 1.5  # More time needed to learn
        
        hours = int(base_hours)
        if hours < 1:
            return "1-2 hours"
        elif hours < 4:
            return f"{hours}-{hours+2} hours"
        else:
            return f"{hours}-{hours+4} hours"
    
    def _estimate_reward(self, features: Dict) -> str:
        """Estimate potential reward range"""
        if features['reward_max'] > 0:
            return f"${features['reward_min']}-${features['reward_max']}"
        
        # Default estimates based on program type
        if features['platform']:
            return "$500-$3000"
        return "$200-$1500"
    
    def _assess_risk(self, features: Dict) -> str:
        """Assess risk level of pursuing this target"""
        risk_score = 0
        
        if features['program_maturity'] == 'new':
            risk_score += 1  # Higher risk of program issues
        
        if features['skill_match'] < 0.3:
            risk_score += 2  # Risk of wasting time
        
        if not features['platform']:
            risk_score += 1  # Unknown platform reliability
        
        if risk_score >= 3:
            return "high"
        elif risk_score >= 1:
            return "medium"
        else:
            return "low"


# Example usage and testing
if __name__ == "__main__":
    analyzer = BountyAnalyzer()
    
    # Test cases
    test_cases = [
        {
            'target': 'https://example.com',
            'context': {
                'reward_min': 500,
                'reward_max': 5000,
                'scope_size': 'large',
                'vulnerabilities': ['XSS', 'SQLi', 'CSRF'],
                'maturity': 'new'
            }
        },
        {
            'target': 'https://api.oldcompany.com',
            'context': {
                'reward_min': 100,
                'reward_max': 1000,
                'scope_size': 'small',
                'maturity': 'established'
            }
        }
    ]
    
    for i, test in enumerate(test_cases, 1):
        print(f"\n=== Test Case {i} ===")
        result = analyzer.evaluate_target(test['target'], test['context'])
        print(f"Target: {test['target']}")
        print(f"Score: {result.score}/100")
        print(f"Recommended: {result.recommended}")
        print(f"Reasoning: {result.reasoning}")
        print(f"Estimated Time: {result.estimated_time}")
        print(f"Potential Reward: {result.potential_reward}")
        print(f"Risk Level: {result.risk_level}")
