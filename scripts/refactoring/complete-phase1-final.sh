#!/bin/bash
set -e

echo "==================================="
echo "Completing Phase 1 Backend Refactor"
echo "==================================="

cd /Users/adarshgs/Documents/Stocks/App/pi-system

# Step 1: Flatten all duplicate directories
echo "Step 1: Flattening duplicate module directories..."

for module in mutualfunds etf loans insurance upi; do
  # Handle special cases where inner dir has different name
  case $module in
    mutualfunds)
      inner="mutualfund"
      ;;
    insurance)
      inner="protection"
      ;;
    *)
      inner=$(echo $module | sed 's/s$//')
      ;;
  esac
  
  if [ -d "src/main/java/com/pisystem/modules/$module/$inner" ]; then
    echo "Flattening $module/$inner..."
    mv src/main/java/com/pisystem/modules/$module/$inner/* src/main/java/com/pisystem/modules/$module/ 2>/dev/null || true
    rmdir src/main/java/com/pisystem/modules/$module/$inner 2>/dev/null || true
  fi
done

# Step 2: Update package declarations
echo "Step 2: Fixing package declarations..."

find src/main/java/com/pisystem/modules -name "*.java" -exec sed -i '' \
    -e 's/^package com\.pisystem\.modules\.etf\.etf\./package com.pisystem.modules.etf./g' \
    -e 's/^package com\.pisystem\.modules\.mutualfunds\.mutualfund\./package com.pisystem.modules.mutualfunds./g' \
    -e 's/^package com\.pisystem\.modules\.loans\.loan\./package com.pisystem.modules.loans./g' \
    -e 's/^package com\.pisystem\.modules\.insurance\.protection\./package com.pisystem.modules.insurance./g' \
    -e 's/^package com\.pisystem\.modules\.upi\.upi\./package com.pisystem.modules.upi./g' \
    {} \;

find src/main/java/com/pisystem/integrations -name "*.java" -exec sed -i '' \
    -e 's/^package com\.pisystem\.integrations\.accountaggregator\.aa\./package com.pisystem.integrations.accountaggregator./g' \
    -e 's/^package com\.pisystem\.integrations\.externalservices\.externalServices\./package com.pisystem.integrations.externalservices./g' \
    {} \;

find src/main/java/com/pisystem/shared -name "*.java" -exec sed -i '' \
    -e 's/^package com\.pisystem\.shared\.common\./package com.pisystem.shared./g' \
    -e 's/^package com\.pisystem\.shared\.audit\.audit\./package com.pisystem.shared.audit./g' \
    {} \;

# Step 3: Update import statements  
echo "Step 3: Fixing import statements..."

find src/main/java/com/pisystem -name "*.java" -exec sed -i '' \
    -e 's/import com\.pisystem\.modules\.etf\.etf\./import com.pisystem.modules.etf./g' \
    -e 's/import com\.pisystem\.modules\.mutualfunds\.mutualfund\./import com.pisystem.modules.mutualfunds./g' \
    -e 's/import com\.pisystem\.modules\.loans\.loan\./import com.pisystem.modules.loans./g' \
    -e 's/import com\.pisystem\.modules\.insurance\.protection\./import com.pisystem.modules.insurance./g' \
    -e 's/import com\.pisystem\.modules\.upi\.upi\./import com.pisystem.modules.upi./g' \
    -e 's/import com\.pisystem\.integrations\.accountaggregator\.aa\./import com.pisystem.integrations.accountaggregator./g' \
    -e 's/import com\.pisystem\.integrations\.externalservices\.externalServices\./import com.pisystem.integrations.externalservices./g' \
    -e 's/import com\.pisystem\.shared\.common\./import com.pisystem.shared./g' \
    -e 's/import com\.pisystem\.shared\.audit\.audit\./import com.pisystem.shared.audit./g' \
    {} \;

# Step 4: Flatten integrations/shared if needed
echo "Step 4: Flattening integrations and shared..."

if [ -d "src/main/java/com/pisystem/integrations/accountaggregator/aa" ]; then
  mv src/main/java/com/pisystem/integrations/accountaggregator/aa/* src/main/java/com/pisystem/integrations/accountaggregator/
  rmdir src/main/java/com/pisystem/integrations/accountaggregator/aa
fi

if [ -d "src/main/java/com/pisystem/integrations/externalservices/externalServices" ]; then
  mv src/main/java/com/pisystem/integrations/externalservices/externalServices/* src/main/java/com/pisystem/integrations/externalservices/
  rmdir src/main/java/com/pisystem/integrations/externalservices/externalServices
fi

if [ -d "src/main/java/com/pisystem/shared/common" ]; then
  mv src/main/java/com/pisystem/shared/common/* src/main/java/com/pisystem/shared/
  rmdir src/main/java/com/pisystem/shared/common
fi

if [ -d "src/main/java/com/pisystem/shared/audit/audit" ]; then
  mv src/main/java/com/pisystem/shared/audit/audit/* src/main/java/com/pisystem/shared/audit/
  rmdir src/main/java/com/pisystem/shared/audit/audit
fi

# Step 5: Clean up old empty directories
echo "Step 5: Cleaning up old directories..."
rm -rf src/main/java/com/main
rm -rf src/main/java/com/payments  
rm -rf src/main/java/com/investments
rm -rf src/main/java/com/websocket

# Step 6: Stage all changes
echo "Step 6: Staging changes..."
git add src/main/java/

echo ""
echo "✅ Phase 1 completion steps executed!"
echo ""
echo "Now running compilation test..."
./gradlew clean compileJava

echo ""
echo "Compilation successful! ✅"
echo ""
