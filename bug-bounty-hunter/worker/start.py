"""
Worker Node - Bug Bounty Hunter System
Connects to master node and executes scanning tasks
"""
import argparse
import asyncio
import httpx
import platform
import psutil
import socket
import uuid
from loguru import logger
import sys
from typing import List
import time

logger.remove()
logger.add(sys.stdout, level="INFO")


class WorkerNode:
    def __init__(self, master_url: str, worker_id: str = None):
        self.master_url = master_url.rstrip('/')
        self.worker_id = worker_id or str(uuid.uuid4())
        self.hostname = socket.gethostname()
        self.running = False
        self.capabilities = self._detect_capabilities()
        
    def _detect_capabilities(self) -> List[str]:
        """Detect what scanning tools are available"""
        capabilities = ["basic_scan"]
        
        # TODO: Check for installed tools
        # For now, return basic capabilities
        return capabilities
    
    def get_system_info(self):
        """Get system information"""
        return {
            "worker_id": self.worker_id,
            "hostname": self.hostname,
            "capabilities": self.capabilities,
            "cpu_cores": psutil.cpu_count(),
            "memory_gb": round(psutil.virtual_memory().total / (1024**3), 2)
        }
    
    async def register(self):
        """Register with master node"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    f"{self.master_url}/workers/register",
                    json=self.get_system_info()
                )
                response.raise_for_status()
                logger.info(f"Successfully registered with master: {self.worker_id}")
                return True
        except Exception as e:
            logger.error(f"Failed to register with master: {e}")
            return False
    
    async def send_heartbeat(self):
        """Send heartbeat to master"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    f"{self.master_url}/workers/{self.worker_id}/heartbeat"
                )
                response.raise_for_status()
                logger.debug("Heartbeat sent")
        except Exception as e:
            logger.warning(f"Failed to send heartbeat: {e}")
    
    async def heartbeat_loop(self):
        """Continuously send heartbeats"""
        while self.running:
            await self.send_heartbeat()
            await asyncio.sleep(30)  # Send heartbeat every 30 seconds
    
    async def fetch_tasks(self):
        """Fetch available tasks from master"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.master_url}/workers/{self.worker_id}/tasks"
                )
                if response.status_code == 200:
                    return response.json().get('tasks', [])
        except Exception as e:
            logger.debug(f"No tasks available: {e}")
        return []
    
    async def execute_task(self, task):
        """Execute a scanning task"""
        logger.info(f"Executing task: {task.get('type')} on {task.get('target')}")
        
        # TODO: Implement actual scanning logic
        # This is where you'd integrate with tools like nuclei, subfinder, etc.
        
        await asyncio.sleep(5)  # Simulate work
        
        result = {
            "task_id": task.get('id'),
            "status": "completed",
            "findings": []
        }
        
        return result
    
    async def report_result(self, result):
        """Report task result to master"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    f"{self.master_url}/tasks/result",
                    json=result
                )
                response.raise_for_status()
                logger.info(f"Result reported for task: {result['task_id']}")
        except Exception as e:
            logger.error(f"Failed to report result: {e}")
    
    async def work_loop(self):
        """Main work loop - fetch and execute tasks"""
        while self.running:
            tasks = await self.fetch_tasks()
            
            for task in tasks:
                if not self.running:
                    break
                    
                result = await self.execute_task(task)
                await self.report_result(result)
            
            await asyncio.sleep(10)  # Wait before checking for new tasks
    
    async def start(self):
        """Start the worker node"""
        logger.info(f"Starting worker node: {self.worker_id}")
        logger.info(f"Master URL: {self.master_url}")
        logger.info(f"Hostname: {self.hostname}")
        logger.info(f"Capabilities: {', '.join(self.capabilities)}")
        
        # Register with master
        if not await self.register():
            logger.error("Failed to register with master. Exiting.")
            return
        
        self.running = True
        
        # Start heartbeat and work loops
        await asyncio.gather(
            self.heartbeat_loop(),
            self.work_loop()
        )
    
    def stop(self):
        """Stop the worker node"""
        logger.info("Stopping worker node...")
        self.running = False


async def main():
    parser = argparse.ArgumentParser(description="Bug Bounty Hunter Worker Node")
    parser.add_argument(
        "--master-url",
        required=True,
        help="URL of the master node (e.g., http://192.168.1.100:8000)"
    )
    parser.add_argument(
        "--worker-id",
        help="Custom worker ID (default: auto-generated UUID)"
    )
    
    args = parser.parse_args()
    
    worker = WorkerNode(args.master_url, args.worker_id)
    
    try:
        await worker.start()
    except KeyboardInterrupt:
        logger.info("Received shutdown signal")
        worker.stop()


if __name__ == "__main__":
    asyncio.run(main())
