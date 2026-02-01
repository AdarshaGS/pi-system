#!/bin/bash
# Test migration generator compilation

cd "$(dirname "$0")/.."

echo "🔨 Compiling migration generator classes..."
echo "Working directory: $(pwd)"

if [ ! -f "gradlew" ]; then
    echo "❌ gradlew not found in $(pwd)"
    exit 1
fi

sh ./gradlew clean compileJava --console=plain

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "📋 Migration Generator Files:"
    find src/main/java/com/common/devtools/migration -name "*.java" | wc -l | xargs echo "  Java files:"
    find src/main/java/com/common/config -name "JacksonConfig.java" 2>/dev/null && echo "  ✅ JacksonConfig found"
    echo ""
    echo "🚀 Migration generator is ready to use!"
    echo ""
    echo "Try:"
    echo "  npm run migrate:help"
else
    echo "❌ Compilation failed"
    exit 1
fi
