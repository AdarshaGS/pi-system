#!/bin/bash

# Master Backend Restructuring Script
# Completes Phase 1 of the pi-system reorganization

set -e

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║   Pi-System Backend Restructuring - Phase 1 Completion   ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Check if we're in the right directory
if [ ! -f "build.gradle" ]; then
    echo "❌ Error: Please run this script from the pi-system root directory"
    exit 1
fi

echo "📋 Phase 1 will:"
echo "  1. Complete remaining module moves"
echo "  2. Update package declarations in all Java files"
echo "  3. Update import statements throughout codebase"
echo "  4. Test compilation"
echo ""

read -p "Continue with Phase 1? (y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 0
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Step 1/4: Moving remaining modules"
echo "════════════════════════════════════════════════════════════"
bash "$SCRIPT_DIR/complete-backend-restructure.sh"

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Step 2/4: Updating package declarations"
echo "════════════════════════════════════════════════════════════"
bash "$SCRIPT_DIR/update-package-declarations.sh"

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Step 3/4: Updating import statements"
echo "════════════════════════════════════════════════════════════"
bash "$SCRIPT_DIR/update-imports.sh"

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Step 4/4: Testing compilation"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "Running: ./gradlew clean compileJava"
echo ""

if ./gradlew clean compileJava; then
    echo ""
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║              ✅ Backend Restructuring Complete!           ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo ""
    echo "Summary:"
    echo "  ✅ All modules moved to new structure"
    echo "  ✅ Package declarations updated"
    echo "  ✅ Import statements updated"
    echo "  ✅ Backend compiles successfully"
    echo ""
    echo "Next steps:"
    echo "  1. Review the changes: git status"
    echo "  2. Run tests: ./gradlew test"
    echo "  3. Commit changes: git commit -m 'Refactor: Reorganize backend into clean module structure'"
    echo "  4. Proceed to Phase 2: Frontend restructuring"
    echo ""
else
    echo ""
    echo "╔═══════════════════════════════════════════════════════════╗"
    echo "║         ⚠️  Compilation Errors Found                     ║"
    echo "╚═══════════════════════════════════════════════════════════╝"
    echo ""
    echo "There are compilation errors that need to be fixed."
    echo ""
    echo "Common issues:"
    echo "  1. Some imports may reference classes by old paths"
    echo "  2. Duplicate class names in merged modules"
    echo "  3. Inner package references that weren't caught"
    echo ""
    echo "To fix:"
    echo "  1. Review compilation errors above"
    echo "  2. Fix imports manually in affected files"
    echo "  3. Run: ./gradlew clean compileJava"
    echo "  4. Repeat until successful"
    echo ""
    exit 1
fi
