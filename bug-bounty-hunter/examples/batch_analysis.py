"""
Example: Analyzing Multiple Targets in Batch
"""
import asyncio
import sys
sys.path.append('..')

from ai.bounty_analyzer import BountyAnalyzer


async def main():
    analyzer = BountyAnalyzer()
    
    # List of targets to analyze
    targets = [
        {
            'target': 'https://api.company1.com',
            'context': {
                'reward_min': 1000,
                'reward_max': 10000,
                'scope_size': 'large',
                'vulnerabilities': ['XSS', 'SQLi', 'SSRF'],
                'maturity': 'new'
            }
        },
        {
            'target': 'https://old-app.company2.com',
            'context': {
                'reward_min': 100,
                'reward_max': 500,
                'scope_size': 'small',
                'maturity': 'established'
            }
        },
        {
            'target': 'https://hackerone.com/newcompany',
            'context': {
                'reward_min': 500,
                'reward_max': 5000,
                'scope_size': 'medium',
                'vulnerabilities': ['IDOR', 'Auth bypass'],
                'maturity': 'new'
            }
        },
        {
            'target': 'https://mobile-api.company3.com',
            'context': {
                'reward_min': 2000,
                'reward_max': 15000,
                'scope_size': 'large',
                'vulnerabilities': ['XSS', 'SQLi', 'IDOR', 'SSRF'],
                'maturity': 'mature'
            }
        }
    ]
    
    print("=" * 80)
    print("BATCH TARGET ANALYSIS")
    print("=" * 80)
    print()
    
    results = []
    
    for i, target_info in enumerate(targets, 1):
        result = analyzer.evaluate_target(
            target_info['target'],
            target_info['context']
        )
        results.append((target_info['target'], result))
        
        print(f"\n[{i}/{len(targets)}] {target_info['target']}")
        print("-" * 80)
        print(f"Score: {result.score}/100")
        print(f"Recommended: {'✓ YES' if result.recommended else '✗ NO'}")
        print(f"Risk Level: {result.risk_level.upper()}")
        print(f"Skill Match: {result.skill_match*100:.1f}%")
        print(f"Time Estimate: {result.estimated_time}")
        print(f"Reward Range: {result.potential_reward}")
        print(f"Reasoning: {result.reasoning}")
    
    # Sort by score
    results.sort(key=lambda x: x[1].score, reverse=True)
    
    print("\n" + "=" * 80)
    print("PRIORITIZED RECOMMENDATIONS")
    print("=" * 80)
    print()
    
    for i, (target, result) in enumerate(results, 1):
        status = "🎯 HIGH PRIORITY" if result.score >= 80 else "📌 MEDIUM PRIORITY" if result.score >= 60 else "⚠️  LOW PRIORITY"
        print(f"{i}. [{result.score:.1f}] {target}")
        print(f"   {status} - {result.estimated_time}, {result.potential_reward}")
        print()
    
    # Calculate total time needed
    print("=" * 80)
    recommended_count = sum(1 for _, r in results if r.recommended)
    print(f"Summary: {recommended_count}/{len(results)} targets recommended")
    print("=" * 80)


if __name__ == "__main__":
    asyncio.run(main())
