#!/bin/bash

# Update Package Declarations Script
# Updates package declarations in all moved Java files

set -e

echo "==================================="
echo "Updating Package Declarations"
echo "==================================="

# Function to update package declarations in a directory
update_packages() {
    local old_package=$1
    local new_package=$2
    local directory=$3
    
    echo "Updating $old_package → $new_package in $directory"
    
    find "$directory" -name "*.java" -type f | while read -r file; do
        # Get the relative path from the module root
        relative_path=$(dirname "${file#$directory/}")
        
        # Skip if path is just "."
        if [ "$relative_path" = "." ]; then
            new_pkg="$new_package"
        else
            # Convert file path to package path
            pkg_suffix=$(echo "$relative_path" | tr '/' '.')
            new_pkg="$new_package.$pkg_suffix"
        fi
        
        # Update package declaration
        if grep -q "^package $old_package" "$file"; then
            # Extract the current subpackage
            current_pkg=$(grep "^package " "$file" | sed 's/package //;s/;//')
            subpkg="${current_pkg#$old_package}"
            
            if [ -z "$subpkg" ]; then
                sed -i '' "s/^package $old_package;/package $new_package;/" "$file"
            else
                sed -i '' "s/^package $old_package/package $new_package/" "$file"
            fi
            echo "  Updated: $file"
        fi
    done
}

echo ""
echo "Step 1: Updating Core Modules..."

# Auth module
if [ -d "src/main/java/com/pisystem/core/auth" ]; then
    find src/main/java/com/pisystem/core/auth -name "*.java" -type f -exec sed -i '' 's/^package com\.auth/package com.pisystem.core.auth/' {} \;
fi

# Users module
if [ -d "src/main/java/com/pisystem/core/users" ]; then
    find src/main/java/com/pisystem/core/users -name "*.java" -type f -exec sed -i '' 's/^package com\.users/package com.pisystem.core.users/' {} \;
fi

# Admin module  
if [ -d "src/main/java/com/pisystem/core/admin" ]; then
    find src/main/java/com/pisystem/core/admin -name "*.java" -type f -exec sed -i '' 's/^package com\.admin/package com.pisystem.core.admin/' {} \;
fi

echo ""
echo "Step 2: Updating Business Modules..."

# Budget module
if [ -d "src/main/java/com/pisystem/modules/budget" ]; then
    find src/main/java/com/pisystem/modules/budget -name "*.java" -type f -exec sed -i '' 's/^package com\.budget/package com.pisystem.modules.budget/' {} \;
fi

# Tax module
if [ -d "src/main/java/com/pisystem/modules/tax" ]; then
    find src/main/java/com/pisystem/modules/tax -name "*.java" -type f -exec sed -i '' 's/^package com\.tax/package com.pisystem.modules.tax/' {} \;
fi

# Portfolio module
if [ -d "src/main/java/com/pisystem/modules/portfolio" ]; then
    find src/main/java/com/pisystem/modules/portfolio -name "*.java" -type f -exec sed -i '' 's/^package com\.portfolio/package com.pisystem.modules.portfolio/' {} \;
fi

# Stocks module (including from investments)
if [ -d "src/main/java/com/pisystem/modules/stocks" ]; then
    find src/main/java/com/pisystem/modules/stocks -name "*.java" -type f -exec sed -i '' 's/^package com\.investments\.stocks/package com.pisystem.modules.stocks/' {} \;
    find src/main/java/com/pisystem/modules/stocks -name "*.java" -type f -exec sed -i '' 's/^package com\.stocks/package com.pisystem.modules.stocks/' {} \;
fi

# Mutual Funds module
if [ -d "src/main/java/com/pisystem/modules/mutualfunds" ]; then
    find src/main/java/com/pisystem/modules/mutualfunds -name "*.java" -type f -exec sed -i '' 's/^package com\.mutualfund/package com.pisystem.modules.mutualfunds/' {} \;
    find src/main/java/com/pisystem/modules/mutualfunds -name "*.java" -type f -exec sed -i '' 's/^package com\.investments\.mutualfunds/package com.pisystem.modules.mutualfunds/' {} \;
fi

# ETF module
if [ -d "src/main/java/com/pisystem/modules/etf" ]; then
    find src/main/java/com/pisystem/modules/etf -name "*.java" -type f -exec sed -i '' 's/^package com\.etf/package com.pisystem.modules.etf/' {} \;
    find src/main/java/com/pisystem/modules/etf -name "*.java" -type f -exec sed -i '' 's/^package com\.investments\.etf/package com.pisystem.modules.etf/' {} \;
fi

# Lending module
if [ -d "src/main/java/com/pisystem/modules/lending" ]; then
    find src/main/java/com/pisystem/modules/lending -name "*.java" -type f -exec sed -i '' 's/^package com\.lending/package com.pisystem.modules.lending/' {} \;
fi

# Loans module
if [ -d "src/main/java/com/pisystem/modules/loans" ]; then
    find src/main/java/com/pisystem/modules/loans -name "*.java" -type f -exec sed -i '' 's/^package com\.loan/package com.pisystem.modules.loans/' {} \;
fi

# Insurance module (from protection)
if [ -d "src/main/java/com/pisystem/modules/insurance" ]; then
    find src/main/java/com/pisystem/modules/insurance -name "*.java" -type f -exec sed -i '' 's/^package com\.protection/package com.pisystem.modules.insurance/' {} \;
fi

# Savings module
if [ -d "src/main/java/com/pisystem/modules/savings" ]; then
    find src/main/java/com/pisystem/modules/savings -name "*.java" -type f -exec sed -i '' 's/^package com\.savings/package com.pisystem.modules.savings/' {} \;
fi

# SMS module
if [ -d "src/main/java/com/pisystem/modules/sms" ]; then
    find src/main/java/com/pisystem/modules/sms -name "*.java" -type f -exec sed -i '' 's/^package com\.sms/package com.pisystem.modules.sms/' {} \;
fi

# UPI module (from payments.upi and upi)
if [ -d "src/main/java/com/pisystem/modules/upi" ]; then
    find src/main/java/com/pisystem/modules/upi -name "*.java" -type f -exec sed -i '' 's/^package com\.payments\.upi/package com.pisystem.modules.upi/' {} \;
    find src/main/java/com/pisystem/modules/upi -name "*.java" -type f -exec sed -i '' 's/^package com\.upi/package com.pisystem.modules.upi/' {} \;
fi

# AI module
if [ -d "src/main/java/com/pisystem/modules/ai" ]; then
    find src/main/java/com/pisystem/modules/ai -name "*.java" -type f -exec sed -i '' 's/^package com\.ai/package com.pisystem.modules.ai/' {} \;
fi

echo ""
echo "Step 3: Updating Integrations..."

# Account Aggregator
if [ -d "src/main/java/com/pisystem/integrations/accountaggregator" ]; then
    find src/main/java/com/pisystem/integrations/accountaggregator -name "*.java" -type f -exec sed -i '' 's/^package com\.aa/package com.pisystem.integrations.accountaggregator/' {} \;
fi

# External Services
if [ -d "src/main/java/com/pisystem/integrations/externalservices" ]; then
    find src/main/java/com/pisystem/integrations/externalservices -name "*.java" -type f -exec sed -i '' 's/^package com\.externalServices/package com.pisystem.integrations.externalservices/' {} \;
fi

echo ""
echo "Step 4: Updating Infrastructure..."

# Health Check
if [ -d "src/main/java/com/pisystem/infrastructure/healthcheck" ]; then
    find src/main/java/com/pisystem/infrastructure/healthcheck -name "*.java" -type f -exec sed -i '' 's/^package com\.healthstatus/package com.pisystem.infrastructure.healthcheck/' {} \;
fi

# Websocket
if [ -d "src/main/java/com/pisystem/infrastructure/websocket" ]; then
    find src/main/java/com/pisystem/infrastructure/websocket -name "*.java" -type f -exec sed -i '' 's/^package com\.websocket/package com.pisystem.infrastructure.websocket/' {} \;
fi

# Alerts
if [ -d "src/main/java/com/pisystem/infrastructure/alerts" ]; then
    find src/main/java/com/pisystem/infrastructure/alerts -name "*.java" -type f -exec sed -i '' 's/^package com\.alerts/package com.pisystem.infrastructure.alerts/' {} \;
fi

echo ""
echo "Step 5: Updating Shared/Common..."

# Common/Shared
if [ -d "src/main/java/com/pisystem/shared" ]; then
    find src/main/java/com/pisystem/shared -name "*.java" -type f -exec sed -i '' 's/^package com\.common/package com.pisystem.shared/' {} \;
    find src/main/java/com/pisystem/shared/audit -name "*.java" -type f -exec sed -i '' 's/^package com\.audit/package com.pisystem.shared.audit/' {} \; 2>/dev/null || true
fi

echo ""
echo "Step 6: Updating Devtools..."

# Devtools (from api)
if [ -d "src/main/java/com/pisystem/devtools" ]; then
    find src/main/java/com/pisystem/devtools -name "*.java" -type f -exec sed -i '' 's/^package com\.api/package com.pisystem.devtools/' {} \;
fi

echo ""
echo "✅ Package declarations updated!"
echo ""
echo "Next: Run update-imports.sh to update import statements"
echo ""
