#!/bin/bash

# Test Runner Script for Pi-System Integration Tests
# Created: February 5, 2026

set -e

echo "=================================================="
echo "🧪 Pi-System Integration Test Runner"
echo "=================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to run tests
run_tests() {
    local test_name=$1
    local test_pattern=$2
    
    echo -e "${BLUE}Running ${test_name}...${NC}"
    ./gradlew test --tests "${test_pattern}" --rerun-tasks
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ ${test_name} passed!${NC}"
    else
        echo -e "${RED}✗ ${test_name} failed!${NC}"
        return 1
    fi
    echo ""
}

# Main menu
echo "Select test suite to run:"
echo "1. All Integration Tests"
echo "2. Lending Controller Tests"
echo "3. Tax Controller Tests"
echo "4. Insurance Controller Tests"
echo "5. Stock Controller Tests"
echo "6. Portfolio Controller Tests"
echo "7. All New Tests (Lending + Tax + Insurance + Stock + Portfolio)"
echo "8. Generate Test Report"
echo "9. Run with Coverage"
echo ""

read -p "Enter your choice (1-9): " choice

case $choice in
    1)
        echo -e "${YELLOW}Running all integration tests...${NC}"
        ./gradlew test --rerun-tasks
        ;;
    2)
        run_tests "Lending Controller Tests" "*LendingControllerIntegrationTest"
        ;;
    3)
        run_tests "Tax Controller Tests" "*TaxControllerIntegrationTest"
        ;;
    4)
        run_tests "Insurance Controller Tests" "*InsuranceControllerIntegrationTest"
        ;;
    5)
        run_tests "Stock Controller Tests" "*StockControllerIntegrationTest"
        ;;
    6)
        run_tests "Portfolio Controller Tests" "*PortfolioControllerIntegrationTest"
        ;;
    7)
        echo -e "${YELLOW}Running all new integration tests...${NC}"
        run_tests "Lending Tests" "*LendingControllerIntegrationTest"
        run_tests "Tax Tests" "*TaxControllerIntegrationTest"
        run_tests "Insurance Tests" "*InsuranceControllerIntegrationTest"
        run_tests "Stock Tests" "*StockControllerIntegrationTest"
        run_tests "Portfolio Tests" "*PortfolioControllerIntegrationTest"
        ;;
    8)
        echo -e "${YELLOW}Generating test report...${NC}"
        ./gradlew test --rerun-tasks
        echo ""
        echo -e "${GREEN}Test report generated!${NC}"
        echo "Opening report in browser..."
        open build/reports/tests/test/index.html
        ;;
    9)
        echo -e "${YELLOW}Running tests with coverage...${NC}"
        ./gradlew test jacocoTestReport --rerun-tasks
        echo ""
        echo -e "${GREEN}Coverage report generated!${NC}"
        echo "Opening coverage report in browser..."
        open build/reports/jacoco/test/html/index.html
        ;;
    *)
        echo -e "${RED}Invalid choice!${NC}"
        exit 1
        ;;
esac

echo ""
echo "=================================================="
echo -e "${GREEN}✓ Test execution completed!${NC}"
echo "=================================================="
echo ""
echo "📊 View detailed results:"
echo "   - Test Report: build/reports/tests/test/index.html"
echo "   - Coverage Report: build/reports/jacoco/test/html/index.html"
echo ""
