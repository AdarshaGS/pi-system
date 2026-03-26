#!/bin/bash

# Update Import Statements Script
# Updates import statements throughout the entire codebase

set -e

echo "==================================="
echo "Updating Import Statements"
echo "==================================="

echo ""
echo "This will update import statements in all Java files..."
echo "This may take a few minutes..."
echo ""

# Find all Java files and update imports
find src/main/java -name "*.java" -type f -exec sed -i '' \
    -e 's/import com\.auth\./import com.pisystem.core.auth./g' \
    -e 's/import com\.users\./import com.pisystem.core.users./g' \
    -e 's/import com\.admin\./import com.pisystem.core.admin./g' \
    -e 's/import com\.budget\./import com.pisystem.modules.budget./g' \
    -e 's/import com\.tax\./import com.pisystem.modules.tax./g' \
    -e 's/import com\.portfolio\./import com.pisystem.modules.portfolio./g' \
    -e 's/import com\.investments\.stocks\./import com.pisystem.modules.stocks./g' \
    -e 's/import com\.stocks\./import com.pisystem.modules.stocks./g' \
    -e 's/import com\.mutualfund\./import com.pisystem.modules.mutualfunds./g' \
    -e 's/import com\.investments\.mutualfunds\./import com.pisystem.modules.mutualfunds./g' \
    -e 's/import com\.etf\./import com.pisystem.modules.etf./g' \
    -e 's/import com\.investments\.etf\./import com.pisystem.modules.etf./g' \
    -e 's/import com\.lending\./import com.pisystem.modules.lending./g' \
    -e 's/import com\.loan\./import com.pisystem.modules.loans./g' \
    -e 's/import com\.protection\./import com.pisystem.modules.insurance./g' \
    -e 's/import com\.savings\./import com.pisystem.modules.savings./g' \
    -e 's/import com\.sms\./import com.pisystem.modules.sms./g' \
    -e 's/import com\.payments\.upi\./import com.pisystem.modules.upi./g' \
    -e 's/import com\.upi\./import com.pisystem.modules.upi./g' \
    -e 's/import com\.ai\./import com.pisystem.modules.ai./g' \
    -e 's/import com\.aa\./import com.pisystem.integrations.accountaggregator./g' \
    -e 's/import com\.externalServices\./import com.pisystem.integrations.externalservices./g' \
    -e 's/import com\.healthstatus\./import com.pisystem.infrastructure.healthcheck./g' \
    -e 's/import com\.websocket\./import com.pisystem.infrastructure.websocket./g' \
    -e 's/import com\.alerts\./import com.pisystem.infrastructure.alerts./g' \
    -e 's/import com\.common\./import com.pisystem.shared./g' \
    -e 's/import com\.audit\./import com.pisystem.shared.audit./g' \
    -e 's/import com\.api\./import com.pisystem.devtools./g' \
    {} \;

echo "✅ Main source imports updated!"

echo ""
echo "Updating test imports..."

# Update test files
find src/test/java -name "*.java" -type f -exec sed -i '' \
    -e 's/import com\.auth\./import com.pisystem.core.auth./g' \
    -e 's/import com\.users\./import com.pisystem.core.users./g' \
    -e 's/import com\.admin\./import com.pisystem.core.admin./g' \
    -e 's/import com\.budget\./import com.pisystem.modules.budget./g' \
    -e 's/import com\.tax\./import com.pisystem.modules.tax./g' \
    -e 's/import com\.portfolio\./import com.pisystem.modules.portfolio./g' \
    -e 's/import com\.investments\.stocks\./import com.pisystem.modules.stocks./g' \
    -e 's/import com\.stocks\./import com.pisystem.modules.stocks./g' \
    -e 's/import com\.mutualfund\./import com.pisystem.modules.mutualfunds./g' \
    -e 's/import com\.investments\.mutualfunds\./import com.pisystem.modules.mutualfunds./g' \
    -e 's/import com\.etf\./import com.pisystem.modules.etf./g' \
    -e 's/import com\.investments\.etf\./import com.pisystem.modules.etf./g' \
    -e 's/import com\.lending\./import com.pisystem.modules.lending./g' \
    -e 's/import com\.loan\./import com.pisystem.modules.loans./g' \
    -e 's/import com\.protection\./import com.pisystem.modules.insurance./g' \
    -e 's/import com\.savings\./import com.pisystem.modules.savings./g' \
    -e 's/import com\.sms\./import com.pisystem.modules.sms./g' \
    -e 's/import com\.payments\.upi\./import com.pisystem.modules.upi./g' \
    -e 's/import com\.upi\./import com.pisystem.modules.upi./g' \
    -e 's/import com\.ai\./import com.pisystem.modules.ai./g' \
    -e 's/import com\.aa\./import com.pisystem.integrations.accountaggregator./g' \
    -e 's/import com\.externalServices\./import com.pisystem.integrations.externalservices./g' \
    -e 's/import com\.healthstatus\./import com.pisystem.infrastructure.healthcheck./g' \
    -e 's/import com\.websocket\./import com.pisystem.infrastructure.websocket./g' \
    -e 's/import com\.alerts\./import com.pisystem.infrastructure.alerts./g' \
    -e 's/import com\.common\./import com.pisystem.shared./g' \
    -e 's/import com\.audit\./import com.pisystem.shared.audit./g' \
    -e 's/import com\.api\./import com.pisystem.devtools./g' \
    {} \; 2>/dev/null || true

echo "✅ Test imports updated!"

echo ""
echo "Checking for any remaining old imports..."
remaining=$(grep -r "^import com\." src/main/java --include="*.java" 2>/dev/null | grep -v "import com.pisystem" | wc -l || echo "0")
echo "Found $remaining files with old import statements"

if [ "$remaining" -gt 0 ]; then
    echo ""
    echo "Files that may need manual review:"
    grep -r "^import com\." src/main/java --include="*.java" 2>/dev/null | grep -v "import com.pisystem" | cut -d: -f1 | sort -u | head -20
fi

echo ""
echo "✅ Import statements updated!"
echo ""
echo "Next steps:"
echo "1. Review any remaining old imports listed above"
echo "2. Run: ./gradlew clean build"
echo "3. Fix any compilation errors"
echo "4. Run tests: ./gradlew test"
echo ""
