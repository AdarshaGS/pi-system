#!/bin/bash

# Pi System - Developer Tools Setup Script
# Run this script to set up and test all developer tools

echo "🚀 Pi System Developer Tools Setup"
echo "=================================="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install it first."
    echo "   Visit: https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js found: $(node --version)"
echo ""

# Navigate to project root
cd "$(dirname "$0")/.."

echo "📂 Current directory: $(pwd)"
echo ""

# Generate documentation
echo "📚 Generating documentation index..."
node devtools/doc-index-generator.js
echo ""

echo "🚀 Generating API dashboard..."
node devtools/api-doc-generator.js
echo ""

# Check if files were created
if [ -f "DOC_INDEX.md" ] && [ -f "API_DASHBOARD.md" ]; then
    echo "✅ All documentation generated successfully!"
    echo ""
    echo "📁 Generated files:"
    echo "   - DOC_INDEX.md ($(wc -l < DOC_INDEX.md) lines)"
    echo "   - DOC_NAVIGATION.md ($(wc -l < DOC_NAVIGATION.md) lines)"
    echo "   - API_DASHBOARD.md ($(wc -l < API_DASHBOARD.md) lines)"
    echo ""
else
    echo "❌ Error generating documentation"
    exit 1
fi

# Open the developer dashboard
echo "🎯 Opening Developer Dashboard..."
echo ""

if command -v open &> /dev/null; then
    open devtools/dev-dashboard.html
elif command -v xdg-open &> /dev/null; then
    xdg-open devtools/dev-dashboard.html
else
    echo "⚠️  Could not auto-open dashboard"
    echo "   Please open manually: devtools/dev-dashboard.html"
fi

echo "✨ Setup complete!"
echo ""
echo "📖 Next steps:"
echo "   1. Check DOC_INDEX.md for all documentation"
echo "   2. Check API_DASHBOARD.md for API inventory"
echo "   3. Use dev-dashboard.html for daily development"
echo ""
echo "💡 Quick commands:"
echo "   - npm run gen:all    (regenerate all docs)"
echo "   - npm run dashboard  (open dashboard)"
echo ""
