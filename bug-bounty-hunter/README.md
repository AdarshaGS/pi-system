# Distributed Bug Bounty Hunter System

An intelligent, distributed system for automated bug bounty hunting that leverages multiple machines to increase hunting capacity and uses AI to prioritize opportunities.

## Features

### 🌐 Distributed Computing
- Connect multiple machines (including old laptops) to increase scanning capacity
- Load balancing across worker nodes
- Centralized coordination and result aggregation

### 🤖 AI-Powered Analysis
- Intelligent bounty prioritization based on:
  - Your skill set and past successes
  - Bounty reward amounts
  - Program complexity and scope
  - Time constraints
- Automated vulnerability pattern recognition

### 🔍 Active Bounty Discovery
- Automated scraping of platforms:
  - HackerOne
  - Bugcrowd
  - Intigriti
  - YesWeHack
  - Custom programs
- Real-time notifications for new programs
- Continuous monitoring of program updates

### 🛠️ Automated Scanning
- Integration with popular tools:
  - Nuclei
  - Subfinder
  - HTTPx
  - FFUF
  - Custom scripts
- Scheduled recurring scans
- Smart retry logic

### 📊 Intelligence Dashboard
- Real-time system status
- Bounty pipeline visualization
- Success rate tracking
- ROI calculator

## Architecture

```
┌─────────────────┐
│  Master Node    │
│  (Main System)  │
│  - Coordinator  │
│  - Dashboard    │
│  - AI Engine    │
└────────┬────────┘
         │
    ┌────┴────┬────────┬────────┐
    │         │        │        │
┌───▼───┐ ┌──▼───┐ ┌──▼───┐ ┌──▼───┐
│Worker1│ │Worker2│ │Worker3│ │Worker│
│(Laptop)│ │(Main) │ │(Cloud)│ │  N   │
└────────┘ └───────┘ └───────┘ └──────┘
```

## Tech Stack

- **Backend**: Python (FastAPI)
- **Worker Communication**: Redis + Celery
- **Database**: PostgreSQL + MongoDB
- **AI/ML**: TensorFlow/PyTorch for analysis
- **Frontend**: React.js
- **Scanning Tools**: Integrated via Docker containers

## Quick Start

### 1. Master Node Setup
```bash
cd bug-bounty-hunter
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
python setup.py install
```

### 2. Configure API Keys
```bash
cp config/config.example.yaml config/config.yaml
# Edit config.yaml with your platform API keys
```

### 3. Start Master Node
```bash
python master/app.py
```

### 4. Connect Worker Node (Old Laptop)
```bash
# On your old laptop
python worker/start.py --master-url http://your-master-ip:8000
```

## Configuration

### Platform API Keys
Edit `config/config.yaml`:
```yaml
platforms:
  hackerone:
    api_key: "your_key"
    username: "your_username"
  bugcrowd:
    api_key: "your_key"
```

### AI Model Training
```bash
python ai/train_bounty_analyzer.py --data data/historical_bounties.json
```

## Usage

### Check if Input Helps with Bounties
```python
from analyzer import BountyAnalyzer

analyzer = BountyAnalyzer()
result = analyzer.evaluate_target("example.com")

print(f"Bounty Potential: {result.score}/100")
print(f"Recommended: {result.recommended}")
print(f"Reasoning: {result.reasoning}")
```

### Add New Program to Monitor
```bash
python cli.py add-program --url "hackerone.com/company" --priority high
```

### Start Continuous Scanning
```bash
python cli.py start-monitoring --interval 3600  # Check every hour
```

## Development Roadmap

- [x] Project structure
- [ ] Master node core
- [ ] Worker node system
- [ ] Platform integrations
- [ ] AI analyzer
- [ ] Dashboard
- [ ] Docker deployment

## Security & Ethics

⚠️ **Important**: This tool is for legitimate bug bounty hunting only. Always:
- Respect program scope and rules
- Never test without authorization
- Follow responsible disclosure
- Comply with all laws and regulations

## License

MIT License - See LICENSE file for details
