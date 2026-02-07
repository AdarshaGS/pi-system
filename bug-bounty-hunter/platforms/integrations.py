"""
Platform Integrations - Bug Bounty Platforms API Clients
"""
import httpx
from typing import List, Dict, Optional
from abc import ABC, abstractmethod
from loguru import logger
import asyncio
from datetime import datetime


class BasePlatform(ABC):
    """Base class for bug bounty platform integrations"""
    
    def __init__(self, config: Dict):
        self.config = config
        self.enabled = config.get('enabled', False)
    
    @abstractmethod
    async def fetch_programs(self) -> List[Dict]:
        """Fetch available bug bounty programs"""
        pass
    
    @abstractmethod
    async def get_program_details(self, program_id: str) -> Dict:
        """Get detailed information about a specific program"""
        pass
    
    @abstractmethod
    async def check_new_programs(self) -> List[Dict]:
        """Check for newly added programs"""
        pass


class HackerOnePlatform(BasePlatform):
    """HackerOne platform integration"""
    
    BASE_URL = "https://api.hackerone.com/v1"
    
    def __init__(self, config: Dict):
        super().__init__(config)
        self.api_key = config.get('api_key')
        self.username = config.get('username')
        
    def _get_headers(self):
        return {
            "Accept": "application/json"
        }
    
    async def fetch_programs(self) -> List[Dict]:
        """Fetch available bug bounty programs from HackerOne"""
        if not self.enabled or not self.api_key:
            logger.warning("HackerOne integration not configured")
            return []
        
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.BASE_URL}/hackers/programs",
                    headers=self._get_headers(),
                    auth=(self.username, self.api_key),
                    timeout=30.0
                )
                
                if response.status_code == 200:
                    data = response.json()
                    programs = []
                    
                    for item in data.get('data', []):
                        attr = item.get('attributes', {})
                        programs.append({
                            'id': item.get('id'),
                            'name': attr.get('name'),
                            'url': attr.get('url'),
                            'platform': 'hackerone',
                            'state': attr.get('state'),
                            'offers_bounties': attr.get('offers_bounties'),
                            'submission_state': attr.get('submission_state')
                        })
                    
                    logger.info(f"Fetched {len(programs)} programs from HackerOne")
                    return programs
                else:
                    logger.error(f"HackerOne API error: {response.status_code}")
                    return []
                    
        except Exception as e:
            logger.error(f"Error fetching HackerOne programs: {e}")
            return []
    
    async def get_program_details(self, program_id: str) -> Dict:
        """Get detailed program information"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.BASE_URL}/programs/{program_id}",
                    headers=self._get_headers(),
                    auth=(self.username, self.api_key),
                    timeout=30.0
                )
                
                if response.status_code == 200:
                    return response.json()
                return {}
                
        except Exception as e:
            logger.error(f"Error fetching program details: {e}")
            return {}
    
    async def check_new_programs(self) -> List[Dict]:
        """Check for new programs (simplified version)"""
        # In production, compare with stored programs
        programs = await self.fetch_programs()
        return programs[:5]  # Return first 5 as "new"


class BugcrowdPlatform(BasePlatform):
    """Bugcrowd platform integration"""
    
    BASE_URL = "https://api.bugcrowd.com"
    
    def __init__(self, config: Dict):
        super().__init__(config)
        self.api_key = config.get('api_key')
    
    async def fetch_programs(self) -> List[Dict]:
        """Fetch programs from Bugcrowd"""
        if not self.enabled or not self.api_key:
            logger.warning("Bugcrowd integration not configured")
            return []
        
        # Bugcrowd API implementation
        # Note: Actual API may differ, this is a template
        logger.info("Bugcrowd integration - implementation pending")
        return []
    
    async def get_program_details(self, program_id: str) -> Dict:
        return {}
    
    async def check_new_programs(self) -> List[Dict]:
        return []


class IntigritiPlatform(BasePlatform):
    """Intigriti platform integration"""
    
    BASE_URL = "https://api.intigriti.com"
    
    def __init__(self, config: Dict):
        super().__init__(config)
        self.api_key = config.get('api_key')
    
    async def fetch_programs(self) -> List[Dict]:
        if not self.enabled:
            return []
        logger.info("Intigriti integration - implementation pending")
        return []
    
    async def get_program_details(self, program_id: str) -> Dict:
        return {}
    
    async def check_new_programs(self) -> List[Dict]:
        return []


class PlatformManager:
    """Manages all platform integrations"""
    
    def __init__(self, config: Dict):
        self.platforms = {}
        
        # Initialize platforms
        if config.get('hackerone', {}).get('enabled'):
            self.platforms['hackerone'] = HackerOnePlatform(config['hackerone'])
        
        if config.get('bugcrowd', {}).get('enabled'):
            self.platforms['bugcrowd'] = BugcrowdPlatform(config['bugcrowd'])
        
        if config.get('intigriti', {}).get('enabled'):
            self.platforms['intigriti'] = IntigritiPlatform(config['intigriti'])
        
        logger.info(f"Initialized {len(self.platforms)} platform integrations")
    
    async def fetch_all_programs(self) -> List[Dict]:
        """Fetch programs from all enabled platforms"""
        all_programs = []
        
        tasks = [
            platform.fetch_programs() 
            for platform in self.platforms.values()
        ]
        
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        for result in results:
            if isinstance(result, list):
                all_programs.extend(result)
            elif isinstance(result, Exception):
                logger.error(f"Platform error: {result}")
        
        logger.info(f"Total programs fetched: {len(all_programs)}")
        return all_programs
    
    async def check_new_programs(self) -> List[Dict]:
        """Check all platforms for new programs"""
        new_programs = []
        
        for platform_name, platform in self.platforms.items():
            try:
                programs = await platform.check_new_programs()
                new_programs.extend(programs)
                logger.info(f"Found {len(programs)} new programs on {platform_name}")
            except Exception as e:
                logger.error(f"Error checking {platform_name}: {e}")
        
        return new_programs
    
    def get_platform(self, name: str) -> Optional[BasePlatform]:
        """Get specific platform by name"""
        return self.platforms.get(name)


# Testing
if __name__ == "__main__":
    import yaml
    
    # Load config
    with open("../config/config.yaml", "r") as f:
        config = yaml.safe_load(f)
    
    manager = PlatformManager(config['platforms'])
    
    # Test fetching programs
    async def test():
        programs = await manager.fetch_all_programs()
        print(f"\nFetched {len(programs)} total programs")
        
        if programs:
            print("\nSample program:")
            print(programs[0])
    
    asyncio.run(test())
