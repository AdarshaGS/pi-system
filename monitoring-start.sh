#!/bin/bash

# PI System - Monitoring Quick Start Script
# This script helps set up and verify Prometheus & Grafana monitoring

set -e

echo "🚀 PI System - Monitoring Setup"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
echo -n "Checking Docker... "
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗${NC}"
    echo "Docker is not running. Please start Docker and try again."
    exit 1
fi
echo -e "${GREEN}✓${NC}"

# Check if docker-compose is available
echo -n "Checking docker-compose... "
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}✗${NC}"
    echo "docker-compose is not installed. Please install it and try again."
    exit 1
fi
echo -e "${GREEN}✓${NC}"

echo ""
echo "📦 Starting monitoring services..."
echo ""

# Start Prometheus and Grafana
docker-compose up -d prometheus grafana

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 5

# Check Prometheus
echo -n "Checking Prometheus... "
if curl -s http://localhost:9090/-/healthy > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${YELLOW}⚠${NC}  (may take a few more seconds)"
fi

# Check Grafana
echo -n "Checking Grafana... "
if curl -s http://localhost:3000/api/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${YELLOW}⚠${NC}  (may take a few more seconds)"
fi

# Check Application
echo -n "Checking PI System App... "
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${YELLOW}⚠${NC}  (app may not be running - start with 'docker-compose up -d app')"
fi

echo ""
echo "=========================================="
echo "✅ Monitoring Setup Complete!"
echo "=========================================="
echo ""
echo "📊 Access Points:"
echo "  • Prometheus:  http://localhost:9090"
echo "  • Grafana:     http://localhost:3000"
echo "  • Actuator:    http://localhost:8080/actuator"
echo ""
echo "🔐 Grafana Credentials:"
echo "  Username: admin"
echo "  Password: admin123"
echo ""
echo "📖 Documentation:"
echo "  • Quick Guide:  monitoring/README.md"
echo "  • Full Guide:   docs/MONITORING_GUIDE.md"
echo ""
echo "🔍 View Metrics:"
echo "  curl http://localhost:8080/actuator/prometheus"
echo ""
echo "🎯 Next Steps:"
echo "  1. Login to Grafana"
echo "  2. Navigate to Dashboards"
echo "  3. Open 'PI System - Application Metrics'"
echo ""
echo "💡 Useful Commands:"
echo "  • View logs:     docker-compose logs -f prometheus grafana"
echo "  • Stop services: docker-compose stop prometheus grafana"
echo "  • Restart:       docker-compose restart prometheus grafana"
echo ""

# Optional: Try to open Grafana in browser (macOS/Linux)
if [[ "$OSTYPE" == "darwin"* ]]; then
    read -p "Open Grafana in browser? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        open http://localhost:3000
    fi
fi
