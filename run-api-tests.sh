#!/bin/bash

# API Test Runner Script
# This script runs API integration tests for the PI System

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}PI System - API Integration Tests${NC}"
echo -e "${YELLOW}========================================${NC}"

# Check if Redis is running (required for tests)
if ! redis-cli ping > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Redis is not running!${NC}"
    echo -e "${YELLOW}Please start Redis: brew services start redis${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Redis is running${NC}"

# Run tests based on argument
case "$1" in
    "auth")
        echo -e "${YELLOW}Running Authentication Tests (API + Unit)...${NC}"
        ./gradlew test --tests "com.api.auth.*" --tests "com.auth.*" --info
        ;;
    "savings")
        echo -e "${YELLOW}Running Savings Tests (API + Service)...${NC}"
        ./gradlew test --tests "com.api.savings.*" --tests "com.savings.*" --info
        ;;
    "portfolio")
        echo -e "${YELLOW}Running Portfolio Tests...${NC}"
        ./gradlew test --tests "com.api.portfolio.*" --tests "com.investments.stocks.diversification.portfolio.*" --info
        ;;
    "budget")
        echo -e "${YELLOW}Running Budget Tests (API + Service + Repository)...${NC}"
        ./gradlew test --tests "com.api.budget.*" --tests "com.budget.*" --info
        ;;
    "investments")
        echo -e "${YELLOW}Running Investment Tests...${NC}"
        ./gradlew test --tests "com.api.investments.*" --tests "com.investments.*" --info
        ;;
    "api-only")
        echo -e "${YELLOW}Running API Integration Tests Only...${NC}"
        ./gradlew test --tests "com.api.*" --info
        ;;
    "all")
        echo -e "${YELLOW}Running All Tests (Integration + Unit + Service)...${NC}"
        ./gradlew test --info
        ;;
    "coverage")
        echo -e "${YELLOW}Running All Tests with Coverage...${NC}"
        ./gradlew test jacocoTestReport
        echo -e "${GREEN}Coverage report: build/reports/jacoco/test/html/index.html${NC}"
        ;;
    *)
        echo -e "${YELLOW}Usage: $0 {auth|savings|portfolio|budget|investments|api-only|all|coverage}${NC}"
        echo -e ""
        echo -e "Examples:"
        echo -e "  $0 auth        - Run all authentication tests (API + Unit)"
        echo -e "  $0 savings     - Run all savings tests (API + Service)"
        echo -e "  $0 portfolio   - Run all portfolio tests"
        echo -e "  $0 budget      - Run all budget tests (API + Service + Repository)"
        echo -e "  $0 investments - Run all investment tests"
        echo -e "  $0 api-only    - Run only API integration tests"
        echo -e "  $0 all         - Run ALL tests in the project"
        echo -e "  $0 coverage    - Run all tests with coverage report"
        exit 1
        ;;
esac

# Check test results
if [ $? -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${YELLOW}Test report: build/reports/tests/test/index.html${NC}"
    echo -e "${YELLOW}(Package names simplified: 'portfolio' instead of 'com.portfolio')${NC}"
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}✗ Some tests failed!${NC}"
    echo -e "${RED}========================================${NC}"
    echo -e "${YELLOW}Check: build/reports/tests/test/index.html${NC}"
    exit 1
fi
