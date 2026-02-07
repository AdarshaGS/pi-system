#!/bin/bash

# Setup script for Bug Bounty Hunter System

echo "🚀 Setting up Bug Bounty Hunter System..."

# Check Python version
python_version=$(python3 --version 2>&1 | grep -oP '\d+\.\d+')
required_version="3.8"

if [ "$(printf '%s\n' "$required_version" "$python_version" | sort -V | head -n1)" != "$required_version" ]; then
    echo "❌ Python 3.8+ is required. You have Python $python_version"
    exit 1
fi

echo "✓ Python version: $python_version"

# Create virtual environment
echo "Creating virtual environment..."
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate

# Upgrade pip
echo "Upgrading pip..."
pip install --upgrade pip

# Install requirements
echo "Installing Python dependencies..."
pip install -r requirements.txt

# Create necessary directories
echo "Creating directory structure..."
mkdir -p logs
mkdir -p models
mkdir -p data
mkdir -p config

# Copy config example if config doesn't exist
if [ ! -f config/config.yaml ]; then
    echo "Creating default configuration..."
    cp config/config.example.yaml config/config.yaml
    echo "⚠️  Please edit config/config.yaml with your API keys and settings"
fi

# Create database initialization script
echo "Creating database setup..."
cat > setup_db.py << 'EOF'
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

def setup_database():
    try:
        # Connect to PostgreSQL
        conn = psycopg2.connect(
            host="localhost",
            user="postgres",
            password="postgres"
        )
        conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = conn.cursor()
        
        # Create database
        cursor.execute("CREATE DATABASE bugbounty")
        print("✓ Database created successfully")
        
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Database setup: {e}")

if __name__ == "__main__":
    setup_database()
EOF

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Edit config/config.yaml with your API keys"
echo "2. Start Redis: redis-server"
echo "3. Start PostgreSQL (if using database features)"
echo "4. Activate venv: source venv/bin/activate"
echo "5. Start master node: python master/app.py"
echo "6. Start worker node: python worker/start.py --master-url http://localhost:8000"
echo ""
echo "For CLI usage: python cli.py --help"
echo ""
echo "Happy bug hunting! 🎯"
