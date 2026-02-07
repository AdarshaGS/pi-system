"""
Master Node - Bug Bounty Hunter System
Main coordination server that manages workers and bounty hunting operations
"""
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict
import uvicorn
from loguru import logger
import yaml
from datetime import datetime
import sys

# Configure logging
logger.remove()
logger.add(sys.stdout, level="INFO")
logger.add("logs/master.log", rotation="100 MB", retention="10 days")

app = FastAPI(title="Bug Bounty Hunter Master", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load configuration
with open("config/config.yaml", "r") as f:
    config = yaml.safe_load(f)

# In-memory storage (replace with database in production)
workers = {}
bounty_programs = []
scan_results = []


class WorkerRegistration(BaseModel):
    worker_id: str
    hostname: str
    capabilities: List[str]
    cpu_cores: int
    memory_gb: float


class BountyTarget(BaseModel):
    url: str
    program_name: Optional[str] = None
    platform: Optional[str] = None


class AnalysisRequest(BaseModel):
    target: str
    context: Optional[str] = None


class AnalysisResponse(BaseModel):
    score: float
    recommended: bool
    reasoning: str
    estimated_time: str
    potential_reward: str


@app.get("/")
async def root():
    return {
        "status": "online",
        "service": "Bug Bounty Hunter Master",
        "version": "1.0.0",
        "timestamp": datetime.now().isoformat()
    }


@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "workers": len(workers),
        "active_programs": len(bounty_programs),
        "total_scans": len(scan_results)
    }


@app.post("/workers/register")
async def register_worker(worker: WorkerRegistration):
    """Register a new worker node"""
    workers[worker.worker_id] = {
        **worker.dict(),
        "registered_at": datetime.now().isoformat(),
        "status": "active",
        "tasks_completed": 0
    }
    logger.info(f"Worker registered: {worker.worker_id} ({worker.hostname})")
    return {"status": "registered", "worker_id": worker.worker_id}


@app.get("/workers")
async def list_workers():
    """List all registered workers"""
    return {"workers": workers}


@app.post("/workers/{worker_id}/heartbeat")
async def worker_heartbeat(worker_id: str):
    """Update worker heartbeat"""
    if worker_id not in workers:
        raise HTTPException(status_code=404, detail="Worker not found")
    
    workers[worker_id]["last_heartbeat"] = datetime.now().isoformat()
    workers[worker_id]["status"] = "active"
    return {"status": "ok"}


@app.post("/analyze")
async def analyze_bounty_potential(request: AnalysisRequest) -> AnalysisResponse:
    """
    Analyze if a target/input will help in bug bounty hunting
    Uses AI model to evaluate potential
    """
    logger.info(f"Analyzing bounty potential for: {request.target}")
    
    # TODO: Implement actual AI analysis
    # For now, return a mock response
    
    # Simple heuristic scoring (replace with ML model)
    score = 75.0  # Base score
    
    # Check if it's a known bounty platform domain
    bounty_keywords = ['hackerone', 'bugcrowd', 'intigriti', 'yeswehack']
    if any(keyword in request.target.lower() for keyword in bounty_keywords):
        score += 15
    
    # Check for web application indicators
    if request.target.startswith('http'):
        score += 10
    
    recommended = score >= config['ai']['min_confidence'] * 100
    
    reasoning_parts = []
    if score >= 80:
        reasoning_parts.append("High potential target with good scope")
    if any(keyword in request.target.lower() for keyword in bounty_keywords):
        reasoning_parts.append("Recognized bounty platform")
    if not recommended:
        reasoning_parts.append("Score below minimum confidence threshold")
    
    return AnalysisResponse(
        score=score,
        recommended=recommended,
        reasoning=". ".join(reasoning_parts) if reasoning_parts else "General target assessment",
        estimated_time="2-4 hours",
        potential_reward="$500-$2000"
    )


@app.post("/programs/add")
async def add_bounty_program(target: BountyTarget, background_tasks: BackgroundTasks):
    """Add a new bounty program to monitor"""
    program = {
        "id": len(bounty_programs) + 1,
        "url": target.url,
        "program_name": target.program_name,
        "platform": target.platform,
        "added_at": datetime.now().isoformat(),
        "status": "active",
        "last_checked": None
    }
    
    bounty_programs.append(program)
    logger.info(f"Added new bounty program: {target.program_name or target.url}")
    
    # Schedule background scan
    background_tasks.add_task(initiate_scan, program)
    
    return {"status": "added", "program": program}


@app.get("/programs")
async def list_programs():
    """List all monitored bounty programs"""
    return {"programs": bounty_programs}


@app.post("/scan/start")
async def start_scan(target: BountyTarget):
    """Start a scan on a target"""
    scan = {
        "id": len(scan_results) + 1,
        "target": target.url,
        "status": "queued",
        "started_at": datetime.now().isoformat(),
        "findings": []
    }
    scan_results.append(scan)
    
    # TODO: Dispatch to available worker
    logger.info(f"Scan queued: {target.url}")
    
    return {"status": "queued", "scan_id": scan["id"]}


@app.get("/scans")
async def list_scans():
    """List all scans"""
    return {"scans": scan_results}


@app.get("/scans/{scan_id}")
async def get_scan(scan_id: int):
    """Get details of a specific scan"""
    for scan in scan_results:
        if scan["id"] == scan_id:
            return scan
    raise HTTPException(status_code=404, detail="Scan not found")


async def initiate_scan(program: Dict):
    """Background task to initiate scanning"""
    logger.info(f"Initiating scan for program: {program['program_name']}")
    # TODO: Implement actual scanning logic with worker dispatch


@app.post("/monitor/start")
async def start_monitoring():
    """Start continuous monitoring of all programs"""
    logger.info("Starting continuous monitoring")
    # TODO: Implement scheduler for continuous checks
    return {"status": "monitoring_started"}


@app.post("/monitor/stop")
async def stop_monitoring():
    """Stop continuous monitoring"""
    logger.info("Stopping continuous monitoring")
    return {"status": "monitoring_stopped"}


if __name__ == "__main__":
    logger.info("Starting Bug Bounty Hunter Master Node")
    logger.info(f"Configuration loaded from config/config.yaml")
    
    uvicorn.run(
        app,
        host=config['master']['host'],
        port=config['master']['port'],
        log_level="info"
    )
