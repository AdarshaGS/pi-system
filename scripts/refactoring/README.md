# Backend Restructuring Scripts

This directory contains automated scripts to complete the Phase 1 backend reorganization of the pi-system repository.

## Overview

The backend restructuring involves:
1. Moving remaining Java modules to new package structure
2. Updating all package declarations
3. Updating all import statements throughout the codebase
4. Testing compilation

## Current Progress

### ✅ Already Completed (via Git):
- Main application class moved and updated
- Major business modules moved (budget, tax, portfolio, stocks, lending, loans, insurance, savings, sms, upi, mutualfunds, etf)
- Core modules moved (auth, users, admin)
- Integration modules moved (accountaggregator, externalservices)

### 🔄 Remaining Work:
- Move remaining small modules (~63 files): ai, common, audit, api, alerts, healthstatus, websocket
- Update all package declarations (~500+ files)
- Update all import statements (thousands of imports)
- Test and fix compilation errors

## Scripts

### `run-phase1-restructure.sh` (MASTER SCRIPT)
**This is the main script that orchestrates everything.**

Runs all steps in sequence:
1. Completes remaining module moves
2. Updates package declarations
3. Updates import statements
4. Tests compilation

**Usage:**
```bash
cd /path/to/pi-system
./scripts/refactoring/run-phase1-restructure.sh
```

### `complete-backend-restructure.sh`
Moves remaining modules that weren't moved yet:
- `com.ai` → `com.pisystem.modules.ai`
- `com.common` → `com.pisystem.shared`
- `com.audit` → `com.pisystem.shared.audit`
- `com.api` → `com.pisystem.devtools`
- `com.alerts` → `com.pisystem.infrastructure.alerts`
- `com.healthstatus` → `com.pisystem.infrastructure.healthcheck`
- `com.websocket` → `com.pisystem.infrastructure.websocket`

**Usage:**
```bash
./scripts/refactoring/complete-backend-restructure.sh
```

### `update-package-declarations.sh`
Updates the `package` statement at the top of every Java file to reflect the new package structure.

For example:
- `package com.auth.controller;` → `package com.pisystem.core.auth.controller;`
- `package com.budget.service;` → `package com.pisystem.modules.budget.service;`

**Usage:**
```bash
./scripts/refactoring/update-package-declarations.sh
```

### `update-imports.sh`
Updates all `import` statements throughout the codebase to use the new package names.

For example:
- `import com.auth.data.LoginRequest;` → `import com.pisystem.core.auth.data.LoginRequest;`
- `import com.budget.service.BudgetService;` → `import com.pisystem.modules.budget.service.BudgetService;`

**Usage:**
```bash
./scripts/refactoring/update-imports.sh
```

## Recommended Workflow

### Option 1: Run Everything at Once (Recommended)
```bash
# From pi-system root directory
./scripts/refactoring/run-phase1-restructure.sh
```

This will:
- Execute all steps automatically
- Show progress for each step
- Test compilation at the end
- Provide clear error messages if anything fails

### Option 2: Run Step by Step (For More Control)
```bash
# Step 1: Complete module moves
./scripts/refactoring/complete-backend-restructure.sh

# Step 2: Update package declarations
./scripts/refactoring/update-package-declarations.sh

# Step 3: Update imports
./scripts/refactoring/update-imports.sh

# Step 4: Test compilation
./gradlew clean compileJava
```

## What to Do If Compilation Fails

If you get compilation errors after running the scripts:

### 1. Review the Errors
```bash
./gradlew clean compileJava 2>&1 | tee compile-errors.log
```

### 2. Common Issues and Fixes

**Issue: "cannot find symbol" errors**
- Some imports may still reference old package names
- Search for the old import: `grep -r "import com.oldpackage" src/`
- Replace with new import manually

**Issue: Duplicate class definitions**
- When merging modules (e.g., etf, mutualfunds), there may be duplicate classes
- Identify duplicates and merge or remove as needed

**Issue: Inner package references**
- Some code may reference classes without imports (same package)
- Update these references to use new package structure

### 3. Fix Specific Files
```bash
# Find files with specific import issues
grep -r "import com.auth\." src/main/java

# Fix them with sed or manually in your IDE
```

### 4. Use IDE Help
- Open the project in IntelliJ IDEA or VS Code
- Use "Organize Imports" or "Fix All" features
- The IDE can auto-fix many import issues

## Testing After Restructuring

### 1. Compile
```bash
./gradlew clean build
```

### 2. Run Tests
```bash
./gradlew test
```

### 3. Run the Application
```bash
./gradlew bootRun
```

### 4. Verify Key Endpoints
- Health check: `http://localhost:8080/health`
- API endpoints working correctly

## Git Workflow

### Before Running Scripts
```bash
# Create a feature branch
git checkout -b refactor/backend-restructure

# Or ensure you're on the right branch
git status
```

### After Successfully Completing
```bash
# Review changes
git status
git diff --stat

# Stage all changes
git add .

# Commit
git commit -m "refactor: Reorganize backend into clean module structure

- Moved all modules to com.pisystem namespace
- Organized into core/, modules/, integrations/, infrastructure/, shared/
- Updated all package declarations and imports
- Verified compilation successful"

# Push
git push origin refactor/backend-restructure
```

## Rollback Strategy

If something goes wrong and you need to rollback:

```bash
# If you haven't committed yet
git reset --hard HEAD

# If you've committed but not pushed
git reset --hard HEAD~1

# If you've pushed
git revert <commit-hash>
```

## Next Steps After Phase 1

Once backend restructuring is complete:

1. ✅ Commit and push changes
2. 📋 Proceed to Phase 2: Frontend restructuring
3. 📋 Phase 3: Documentation reorganization
4. 📋 Phase 4: Root cleanup
5. 📋 Phase 5: Testing and validation

## Troubleshooting

### Script Permission Issues
```bash
chmod +x scripts/refactoring/*.sh
```

### Git Issues
If git commands in scripts fail, you may need to manually add/remove files:
```bash
git add src/main/java/com/pisystem/
git rm -r src/main/java/com/oldpackage/
```

### MacOS vs Linux sed Syntax
The scripts use macOS `sed -i ''` syntax. On Linux, use:
```bash
sed -i 's/old/new/g' file.java
```

## Support

For issues or questions:
1. Check the MIGRATION_PLAN.md for detailed information
2. Review compilation errors carefully
3. Use your IDE's refactoring tools as backup
4. The scripts are safe - all changes are in git

## Estimated Time

- Running scripts: 5-10 minutes
- Fixing compilation errors (if any): 30-60 minutes
- Testing: 15-30 minutes
- **Total: 1-2 hours**

Much faster than manual refactoring (4-6 hours)!
