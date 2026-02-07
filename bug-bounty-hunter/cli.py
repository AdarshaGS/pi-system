"""
CLI - Bug Bounty Hunter Command Line Interface
"""
import click
import httpx
import yaml
from rich.console import Console
from rich.table import Table
from rich import print as rprint
import asyncio

console = Console()

# Load config
with open("config/config.yaml", "r") as f:
    config = yaml.safe_load(f)

MASTER_URL = f"http://{config['master']['host']}:{config['master']['port']}"


@click.group()
def cli():
    """Bug Bounty Hunter CLI - Manage your bounty hunting operations"""
    pass


@cli.command()
@click.option('--target', required=True, help='Target URL or domain to analyze')
@click.option('--context', help='Additional context (JSON format)')
def analyze(target, context):
    """Analyze if a target is worth pursuing for bug bounties"""
    console.print(f"[cyan]Analyzing target:[/cyan] {target}")
    
    async def do_analyze():
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{MASTER_URL}/analyze",
                json={"target": target, "context": context}
            )
            
            if response.status_code == 200:
                result = response.json()
                
                # Create results table
                table = Table(title="Analysis Results", show_header=False)
                table.add_column("Property", style="cyan")
                table.add_column("Value", style="green")
                
                table.add_row("Score", f"{result['score']}/100")
                table.add_row("Recommended", "✓ Yes" if result['recommended'] else "✗ No")
                table.add_row("Reasoning", result['reasoning'])
                table.add_row("Estimated Time", result['estimated_time'])
                table.add_row("Potential Reward", result['potential_reward'])
                
                console.print(table)
                
                if result['recommended']:
                    console.print("\n[green]✓ This target is recommended for bug bounty hunting![/green]")
                else:
                    console.print("\n[yellow]⚠ This target may not be the best use of your time.[/yellow]")
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_analyze())


@cli.command()
@click.option('--url', required=True, help='Program URL')
@click.option('--name', help='Program name')
@click.option('--platform', help='Platform (hackerone, bugcrowd, etc.)')
@click.option('--priority', default='medium', help='Priority (low, medium, high)')
def add_program(url, name, platform, priority):
    """Add a new bug bounty program to monitor"""
    console.print(f"[cyan]Adding program:[/cyan] {name or url}")
    
    async def do_add():
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{MASTER_URL}/programs/add",
                json={
                    "url": url,
                    "program_name": name,
                    "platform": platform
                }
            )
            
            if response.status_code == 200:
                console.print("[green]✓ Program added successfully![/green]")
                result = response.json()
                console.print(f"Program ID: {result['program']['id']}")
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_add())


@cli.command()
def list_programs():
    """List all monitored bug bounty programs"""
    async def do_list():
        async with httpx.AsyncClient() as client:
            response = await client.get(f"{MASTER_URL}/programs")
            
            if response.status_code == 200:
                programs = response.json()['programs']
                
                if not programs:
                    console.print("[yellow]No programs found. Add some with 'add-program'[/yellow]")
                    return
                
                table = Table(title="Monitored Programs")
                table.add_column("ID", style="cyan")
                table.add_column("Program", style="green")
                table.add_column("Platform", style="blue")
                table.add_column("Status", style="yellow")
                table.add_column("Added", style="magenta")
                
                for program in programs:
                    table.add_row(
                        str(program['id']),
                        program['program_name'] or program['url'],
                        program['platform'] or 'N/A',
                        program['status'],
                        program['added_at'][:10]
                    )
                
                console.print(table)
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_list())


@cli.command()
@click.option('--interval', default=3600, help='Check interval in seconds (default: 3600)')
def start_monitoring(interval):
    """Start continuous monitoring of all programs"""
    console.print(f"[cyan]Starting monitoring with {interval}s interval...[/cyan]")
    
    async def do_start():
        async with httpx.AsyncClient() as client:
            response = await client.post(f"{MASTER_URL}/monitor/start")
            
            if response.status_code == 200:
                console.print("[green]✓ Monitoring started![/green]")
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_start())


@cli.command()
def stop_monitoring():
    """Stop continuous monitoring"""
    async def do_stop():
        async with httpx.AsyncClient() as client:
            response = await client.post(f"{MASTER_URL}/monitor/stop")
            
            if response.status_code == 200:
                console.print("[green]✓ Monitoring stopped[/green]")
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_stop())


@cli.command()
def list_workers():
    """List all connected worker nodes"""
    async def do_list():
        async with httpx.AsyncClient() as client:
            response = await client.get(f"{MASTER_URL}/workers")
            
            if response.status_code == 200:
                workers = response.json()['workers']
                
                if not workers:
                    console.print("[yellow]No workers connected[/yellow]")
                    return
                
                table = Table(title="Connected Workers")
                table.add_column("ID", style="cyan")
                table.add_column("Hostname", style="green")
                table.add_column("CPU", style="blue")
                table.add_column("Memory", style="yellow")
                table.add_column("Status", style="magenta")
                
                for worker_id, worker in workers.items():
                    table.add_row(
                        worker_id[:8] + "...",
                        worker['hostname'],
                        f"{worker['cpu_cores']} cores",
                        f"{worker['memory_gb']:.1f} GB",
                        worker['status']
                    )
                
                console.print(table)
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_list())


@cli.command()
def status():
    """Show system status"""
    async def do_status():
        async with httpx.AsyncClient() as client:
            response = await client.get(f"{MASTER_URL}/health")
            
            if response.status_code == 200:
                health = response.json()
                
                table = Table(title="System Status", show_header=False)
                table.add_column("Metric", style="cyan")
                table.add_column("Value", style="green")
                
                table.add_row("Status", health['status'])
                table.add_row("Workers Connected", str(health['workers']))
                table.add_row("Active Programs", str(health['active_programs']))
                table.add_row("Total Scans", str(health['total_scans']))
                
                console.print(table)
            else:
                console.print(f"[red]Master node is not responding[/red]")
    
    asyncio.run(do_status())


@cli.command()
@click.option('--target', required=True, help='Target to scan')
def scan(target):
    """Start a scan on a target"""
    console.print(f"[cyan]Starting scan on:[/cyan] {target}")
    
    async def do_scan():
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{MASTER_URL}/scan/start",
                json={"url": target}
            )
            
            if response.status_code == 200:
                result = response.json()
                console.print(f"[green]✓ Scan queued! Scan ID: {result['scan_id']}[/green]")
            else:
                console.print(f"[red]Error: {response.status_code}[/red]")
    
    asyncio.run(do_scan())


if __name__ == '__main__':
    cli()
