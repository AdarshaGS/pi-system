"""
Example: Using the API to Analyze Targets
"""
import asyncio
import httpx


async def analyze_target(target: str, context: dict = None):
    """
    Analyze if a target is worth pursuing for bug bounty hunting
    
    Args:
        target: URL or domain to analyze
        context: Additional context about the target
    """
    async with httpx.AsyncClient() as client:
        response = await client.post(
            "http://localhost:8000/analyze",
            json={
                "target": target,
                "context": context
            }
        )
        
        if response.status_code == 200:
            return response.json()
        else:
            return {"error": f"Status code: {response.status_code}"}


async def main():
    print("=" * 70)
    print("Bug Bounty Target Analysis API Example")
    print("=" * 70)
    print()
    
    # Example 1: Simple URL analysis
    print("Example 1: Simple URL Analysis")
    print("-" * 70)
    result = await analyze_target("https://example.com")
    print(f"Target: https://example.com")
    print(f"Score: {result['score']}/100")
    print(f"Recommended: {result['recommended']}")
    print(f"Reasoning: {result['reasoning']}")
    print()
    
    # Example 2: Analysis with context
    print("Example 2: Analysis with Full Context")
    print("-" * 70)
    result = await analyze_target(
        "https://api.startup.com",
        context={
            "reward_min": 500,
            "reward_max": 5000,
            "scope_size": "large",
            "vulnerabilities": ["XSS", "SQLi", "IDOR"],
            "maturity": "new",
            "tech_stack": ["React", "Node.js", "PostgreSQL"]
        }
    )
    print(f"Target: https://api.startup.com")
    print(f"Score: {result['score']}/100")
    print(f"Recommended: {result['recommended']}")
    print(f"Estimated Time: {result['estimated_time']}")
    print(f"Potential Reward: {result['potential_reward']}")
    print(f"Reasoning: {result['reasoning']}")
    print()
    
    # Example 3: HackerOne program
    print("Example 3: HackerOne Program")
    print("-" * 70)
    result = await analyze_target(
        "https://hackerone.com/security",
        context={
            "reward_min": 1000,
            "reward_max": 10000,
            "scope_size": "medium",
            "maturity": "mature"
        }
    )
    print(f"Target: https://hackerone.com/security")
    print(f"Score: {result['score']}/100")
    print(f"Recommended: {result['recommended']}")
    print()
    
    print("=" * 70)
    print("Analysis complete!")
    print("=" * 70)


if __name__ == "__main__":
    asyncio.run(main())
