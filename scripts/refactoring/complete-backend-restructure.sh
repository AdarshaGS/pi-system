#!/bin/bash

# Pi-System Backend Restructuring - Phase 1 Completion Script
# This script completes the backend package reorganization

set -e

echo "==================================="
echo "Backend Restructuring - Part 2"
echo "==================================="

# Move remaining modules
echo "Moving remaining modules..."

# AI module
if [ -d "src/main/java/com/ai" ]; then
    echo "Moving AI module..."
    mkdir -p src/main/java/com/pisystem/modules/ai
    cp -r src/main/java/com/ai/* src/main/java/com/pisystem/modules/ai/
    git add src/main/java/com/pisystem/modules/ai
    git rm -r src/main/java/com/ai
fi

# Common to shared
if [ -d "src/main/java/com/common" ]; then
    echo "Moving common to shared..."
    mkdir -p src/main/java/com/pisystem/shared
    cp -r src/main/java/com/common/* src/main/java/com/pisystem/shared/
    git add src/main/java/com/pisystem/shared
    git rm -r src/main/java/com/common
fi

# Audit to shared/audit
if [ -d "src/main/java/com/audit" ]; then
    echo "Moving audit to shared..."
    mkdir -p src/main/java/com/pisystem/shared/audit
    cp -r src/main/java/com/audit/* src/main/java/com/pisystem/shared/audit/
    git add src/main/java/com/pisystem/shared/audit
    git rm -r src/main/java/com/audit
fi

# API to devtools
if [ -d "src/main/java/com/api" ]; then
    echo "Moving API (testrunner) to devtools..."
    mkdir -p src/main/java/com/pisystem/devtools
    cp -r src/main/java/com/api/* src/main/java/com/pisystem/devtools/
    git add src/main/java/com/pisystem/devtools
    git rm -r src/main/java/com/api
fi

# Alerts to infrastructure
if [ -d "src/main/java/com/alerts" ]; then
    echo "Moving alerts to infrastructure..."
    mkdir -p src/main/java/com/pisystem/infrastructure/alerts
    cp -r src/main/java/com/alerts/* src/main/java/com/pisystem/infrastructure/alerts/
    git add src/main/java/com/pisystem/infrastructure/alerts
    git rm -r src/main/java/com/alerts
fi

# Healthstatus to infrastructure/healthcheck
if [ -d "src/main/java/com/healthstatus" ]; then
    echo "Moving healthstatus to infrastructure..."
    mkdir -p src/main/java/com/pisystem/infrastructure/healthcheck
    cp -r src/main/java/com/healthstatus/* src/main/java/com/pisystem/infrastructure/healthcheck/
    git add src/main/java/com/pisystem/infrastructure/healthcheck
    git rm -r src/main/java/com/healthstatus
fi

# Websocket to infrastructure (if it has files)
if [ -d "src/main/java/com/websocket" ] && [ "$(find src/main/java/com/websocket -name '*.java' | wc -l)" -gt 0 ]; then
    echo "Moving websocket to infrastructure..."
    mkdir -p src/main/java/com/pisystem/infrastructure/websocket
    cp -r src/main/java/com/websocket/* src/main/java/com/pisystem/infrastructure/websocket/
    git add src/main/java/com/pisystem/infrastructure/websocket
    git rm -r src/main/java/com/websocket
fi

# Check for separate UPI directory
if [ -d "src/main/java/com/upi" ] && [ "$(find src/main/java/com/upi -name '*.java' | wc -l)" -gt 0 ]; then
    echo "Merging separate UPI directory..."
    cp -r src/main/java/com/upi/* src/main/java/com/pisystem/modules/upi/
    git add src/main/java/com/pisystem/modules/upi
    git rm -r src/main/java/com/upi
fi

# Clean up empty directories
echo "Cleaning up empty directories..."
rmdir src/main/java/com/main 2>/dev/null || true
rmdir src/main/java/com/payments 2>/dev/null || true
rmdir src/main/java/com/investments 2>/dev/null || true

echo ""
echo "✅ Module moves complete!"
echo ""
echo "Next steps:"
echo "1. Run update-package-declarations.sh to update package statements"
echo "2. Run update-imports.sh to update import statements"
echo "3. Run ./gradlew clean build to test compilation"
echo ""
