#!/bin/bash
# Flatten remaining double-nested module directories

set -e

cd "$(dirname "$0")/../.."

echo "Flattening remaining nested directories..."

# ETF module
if [ -d "src/main/java/com/pisystem/modules/etf/etf" ]; then
    echo "Flattening etf/etf..."
    find src/main/java/com/pisystem/modules/etf/etf -type f -name "*.java" | while read file; do
        target=$(echo "$file" | sed 's|/etf/etf/|/etf/|')
        mkdir -p $(dirname "$target")
        git mv "$file" "$target" 2>/dev/null || mv "$file" "$target"
    done
    rm -rf src/main/java/com/pisystem/modules/etf/etf
    echo "  ✓ etf flattened"
fi

# Stocks module
if [ -d "src/main/java/com/pisystem/modules/stocks/stocks" ]; then
    echo "Flattening stocks/stocks..."
    find src/main/java/com/pisystem/modules/stocks/stocks -type f -name "*.java" | while read file; do
        target=$(echo "$file" | sed 's|/stocks/stocks/|/stocks/|')
        mkdir -p $(dirname "$target")
        git mv "$file" "$target" 2>/dev/null || mv "$file" "$target"
    done
    rm -rf src/main/java/com/pisystem/modules/stocks/stocks
    echo "  ✓ stocks flattened"
fi

# Portfolio module
if [ -d "src/main/java/com/pisystem/modules/portfolio/portfolio" ]; then
    echo "Flattening portfolio/portfolio..."
    find src/main/java/com/pisystem/modules/portfolio/portfolio -type f -name "*.java" | while read file; do
        target=$(echo "$file" | sed 's|/portfolio/portfolio/|/portfolio/|')
        mkdir -p $(dirname "$target")
        git mv "$file" "$target" 2>/dev/null || mv "$file" "$target"
    done
    rm -rf src/main/java/com/pisystem/modules/portfolio/portfolio
    echo "  ✓ portfolio flattened"
fi

# SMS module
if [ -d "src/main/java/com/pisystem/modules/sms/sms" ]; then
    echo "Flattening sms/sms..."
    find src/main/java/com/pisystem/modules/sms/sms -type f -name "*.java" | while read file; do
        target=$(echo "$file" | sed 's|/sms/sms/|/sms/|')
        mkdir -p $(dirname "$target")
        git mv "$file" "$target" 2>/dev/null || mv "$file" "$target"
    done
    rm -rf src/main/java/com/pisystem/modules/sms/sms
    echo "  ✓ sms flattened"
fi

# AI module
if [ -d "src/main/java/com/pisystem/modules/ai/ai" ]; then
    echo "Flattening ai/ai..."
    find src/main/java/com/pisystem/modules/ai/ai -type f -name "*.java" | while read file; do
        target=$(echo "$file" | sed 's|/ai/ai/|/ai/|')
        mkdir -p $(dirname "$target")
        git mv "$file" "$target" 2>/dev/null || mv "$file" "$target"
    done
    rm -rf src/main/java/com/pisystem/modules/ai/ai
    echo "  ✓ ai flattened"
fi

echo ""
echo "Fixing package declarations..."

# Fix package declarations for all modules
find src/main/java/com/pisystem -name "*.java" -exec sed -i '' \
    -e 's/^package com\.pisystem\.modules\.etf\.etf\./package com.pisystem.modules.etf./g' \
    -e 's/^package com\.pisystem\.modules\.stocks\.stocks\./package com.pisystem.modules.stocks./g' \
    -e 's/^package com\.pisystem\.modules\.portfolio\.portfolio\./package com.pisystem.modules.portfolio./g' \
    -e 's/^package com\.pisystem\.modules\.sms\.sms\./package com.pisystem.modules.sms./g' \
    -e 's/^package com\.pisystem\.modules\.ai\.ai\./package com.pisystem.modules.ai./g' \
    {} \;

echo "  ✓ Package declarations updated"

# Fix imports
find src/main/java/com/pisystem -name "*.java" -exec sed -i '' \
    -e 's/import com\.pisystem\.modules\.etf\.etf\./import com.pisystem.modules.etf./g' \
    -e 's/import com\.pisystem\.modules\.stocks\.stocks\./import com.pisystem.modules.stocks./g' \
    -e 's/import com\.pisystem\.modules\.portfolio\.portfolio\./import com.pisystem.modules.portfolio./g' \
    -e 's/import com\.pisystem\.modules\.sms\.sms\./import com.pisystem.modules.sms./g' \
    -e 's/import com\.pisystem\.modules\.ai\.ai\./import com.pisystem.modules.ai./g' \
    {} \;

echo "  ✓ Import statements updated"

echo ""
echo "✓ All remaining modules flattened"
echo "Run: ./gradlew compileJava to verify"
