"""
Example: Connecting a Worker Node
"""
import asyncio
import sys
sys.path.append('..')

from worker.start import WorkerNode


async def main():
    """
    Example of connecting a worker node to the master
    
    Usage:
        python connect_worker.py
    
    Or with custom settings:
        python connect_worker.py --master-url http://192.168.1.100:8000
    """
    
    # Replace with your master node URL
    master_url = "http://localhost:8000"
    
    # Create worker
    worker = WorkerNode(master_url)
    
    print("=" * 60)
    print("Bug Bounty Hunter - Worker Node")
    print("=" * 60)
    print(f"Worker ID: {worker.worker_id}")
    print(f"Hostname: {worker.hostname}")
    print(f"Master URL: {master_url}")
    print(f"Capabilities: {', '.join(worker.capabilities)}")
    print("=" * 60)
    print()
    print("Starting worker... Press Ctrl+C to stop")
    print()
    
    try:
        await worker.start()
    except KeyboardInterrupt:
        print("\n\nShutting down worker...")
        worker.stop()
        print("Worker stopped. Goodbye!")


if __name__ == "__main__":
    asyncio.run(main())
