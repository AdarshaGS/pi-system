# Bug Bounty Hunter - Quick Start Guide

## Prerequisites

- Python 3.8+
- Redis (for task queue)
- PostgreSQL (optional, for persistent storage)

## Installation

### 1. Quick Setup

```bash
cd bug-bounty-hunter
chmod +x setup.sh
./setup.sh
```

### 2. Configure

Edit `config/config.yaml` with your settings:
- Add API keys for bug bounty platforms (HackerOne, Bugcrowd, etc.)
- Configure your skills and preferences
- Set notification preferences

### 3. Start Redis

```bash
redis-server
```

### 4. Start Master Node

```bash
source venv/bin/activate
python master/app.py
```

The master node will start on `http://localhost:8000`

### 5. Connect Worker Nodes

On your main machine:
```bash
python worker/start.py --master-url http://localhost:8000
```

On your old laptop (after setup):
```bash
python worker/start.py --master-url http://YOUR_MAIN_IP:8000
```

## Usage Examples

### Check if Something Will Help with Bounties

```bash
python cli.py analyze --target "https://example.com"
```

Example output:
```
Analysis Results
┌──────────────────┬────────────────────────────────────────┐
│ Score            │ 85/100                                  │
│ Recommended      │ ✓ Yes                                   │
│ Reasoning        │ High potential target with favorable... │
│ Estimated Time   │ 2-4 hours                               │
│ Potential Reward │ $500-$2000                              │
└──────────────────┴────────────────────────────────────────┘

✓ This target is recommended for bug bounty hunting!
```

### Add a Program to Monitor

```bash
python cli.py add-program \
  --url "https://hackerone.com/company" \
  --name "Company XYZ" \
  --platform hackerone \
  --priority high
```

### Start Continuous Monitoring

```bash
python cli.py start-monitoring --interval 3600
```

This will:
- Check all platforms every hour
- Notify you of new programs
- Automatically scan new targets
- Send alerts for high-priority findings

### List All Programs

```bash
python cli.py list-programs
```

### Check System Status

```bash
python cli.py status
```

### View Connected Workers

```bash
python cli.py list-workers
```

## Python API Usage

```python
from ai.bounty_analyzer import BountyAnalyzer

# Initialize analyzer
analyzer = BountyAnalyzer()

# Analyze a target
result = analyzer.evaluate_target(
    target="https://api.example.com",
    context={
        "reward_min": 500,
        "reward_max": 5000,
        "scope_size": "large",
        "vulnerabilities": ["XSS", "SQLi"],
        "maturity": "new"
    }
)

print(f"Score: {result.score}/100")
print(f"Recommended: {result.recommended}")
print(f"Reasoning: {result.reasoning}")
print(f"Risk Level: {result.risk_level}")
```

## Web Dashboard (Coming Soon)

Access the dashboard at `http://localhost:8000/dashboard`

Features:
- Real-time system monitoring
- Active program tracking
- Scan results visualization
- Worker node management
- ROI calculator

## Tips for Maximizing Efficiency

### 1. Connect Multiple Machines

The more workers you connect, the more targets you can scan simultaneously:

```bash
# Laptop 1
python worker/start.py --master-url http://main-ip:8000

# Laptop 2
python worker/start.py --master-url http://main-ip:8000

# Laptop 3
python worker/start.py --master-url http://main-ip:8000
```

### 2. Customize Your Profile

Edit `config/config.yaml` to match your skills:

```yaml
user_profile:
  skills:
    - "XSS"
    - "SQLi"
    - "IDOR"
    - "SSRF"
  experience_level: "intermediate"
  time_available_per_day: 4
  preferred_reward_min: 500
```

This helps the AI prioritize targets that match your expertise.

### 3. Set Up Notifications

Get instant alerts for high-value opportunities:

```yaml
notifications:
  telegram:
    enabled: true
    bot_token: "your_token"
    chat_id: "your_chat"
```

### 4. Schedule Regular Checks

Use cron to keep monitoring running:

```bash
# Check every 2 hours
0 */2 * * * cd /path/to/bug-bounty-hunter && python cli.py start-monitoring
```

## Troubleshooting

### Master node won't start
- Check if port 8000 is available
- Verify Redis is running: `redis-cli ping`
- Check logs: `tail -f logs/master.log`

### Worker can't connect
- Verify master URL is accessible
- Check firewall settings
- Ensure same network or proper port forwarding

### No programs found
- Verify API keys in config/config.yaml
- Check platform status
- Review logs for authentication errors

## Advanced Features

### Custom Scanning Tools

Integrate your own tools by modifying `worker/start.py`:

```python
async def execute_task(self, task):
    if task['type'] == 'custom_scan':
        # Your custom scanning logic
        result = await run_custom_tool(task['target'])
        return result
```

### AI Model Training

Improve accuracy by training on your historical data:

```bash
python ai/train_bounty_analyzer.py --data data/my_bounties.json
```

### API Integration

Build custom integrations using the REST API:

```python
import httpx

async with httpx.AsyncClient() as client:
    # Analyze target
    response = await client.post(
        "http://localhost:8000/analyze",
        json={"target": "example.com"}
    )
    result = response.json()
```

## Security Considerations

⚠️ **Important:**

1. **Always follow program rules** - Respect scope and guidelines
2. **Get authorization** - Never test without permission
3. **Secure your API keys** - Don't commit config/config.yaml
4. **Rate limiting** - Respect platform rate limits
5. **Responsible disclosure** - Follow proper reporting procedures

## Support & Community

- GitHub Issues: Report bugs and request features
- Documentation: See docs/ folder for detailed guides
- Examples: Check examples/ for sample implementations

## Next Steps

1. Configure your API keys
2. Start the master node
3. Connect worker nodes
4. Run your first analysis
5. Add programs to monitor
6. Start continuous hunting!

Happy hunting! 🎯
