---
name: quick-dev
description: "Rapid implementation of small features or bug fixes. Use for quick changes that don't need full story workflow."
---

# Quick Dev Agent

Fast, focused implementation for small changes and bug fixes in PI SYSTEM.

## When to Use

✅ **Use Quick Dev for:**
- Bug fixes (1-3 files)
- Small feature additions
- UI tweaks
- Configuration changes
- Quick improvements

❌ **Don't use Quick Dev for:**
- Multi-module features (use @dev-story)
- Breaking changes
- Database migrations
- Complex integrations

## Workflow

### 1. Understand Change (30 seconds)
- What needs to change?
- Which files affected?
- Any dependencies?

### 2. Implement (5-10 minutes)

**Backend Quick Pattern:**
```java
// 1. Update service method
// 2. Update controller if needed
// 3. Add/update DTO if needed
// 4. Quick test
```

**Frontend Quick Pattern:**
```javascript
// 1. Update component
// 2. Update API service if needed
// 3. Test in browser
```

### 3. Quick Test
- Compilation passes
- Basic functionality works
- No obvious regressions

### 4. Commit
```bash
git add -A
git commit -m "fix: Description of change"
# or
git commit -m "feat: Description of small feature"
```

## Implementation Standards

Follow PI SYSTEM standards even for quick changes:

**Backend:**
- ✓ Correct package structure
- ✓ Return ResponseEntity
- ✓ Add validation annotations
- ✓ Handle exceptions

**Frontend:**
- ✓ Use @ alias imports
- ✓ Place files in correct feature directory
- ✓ Update CSS imports if renaming files
- ✓ Clean up WebSocket connections

## Quick Verification

```bash
# Backend
./gradlew compileJava

# Frontend
cd frontend && npm run build
```

If builds pass → commit and done!

## Example Scenarios

### Scenario 1: Add Validation
**Task**: Add email validation to registration form

**Steps:**
1. Add `@Email` annotation to User entity
2. Add validation to RegisterRequest DTO
3. Add frontend validation in RegisterPage
4. Test registration with invalid email
5. Commit: `fix: Add email validation to registration`

### Scenario 2: UI Enhancement
**Task**: Add loading spinner to budget page

**Steps:**
1. Add loading state: `const [loading, setLoading] = useState(false)`
2. Show spinner: `{loading && <Spinner />}`
3. Set state in async calls
4. Test: `npm run build`
5. Commit: `feat: Add loading indicator to budget page`

### Scenario 3: Fix Bug
**Task**: Fix null pointer in tax calculation

**Steps:**
1. Locate bug in TaxCalculationService
2. Add null check: `if (income == null) return BigDecimal.ZERO;`
3. Add unit test for null case
4. Run: `./gradlew test`
5. Commit: `fix: Handle null income in tax calculation`

## Time Expectations

- **Quick changes**: 5-15 minutes
- **If taking longer**: Consider using @dev-story instead
- **Target**: Under 30 minutes total

## When to Stop

Stop and switch to @dev-story if:
- Change affects 5+ files
- Need database migrations
- Requires architectural decisions
- Testing reveals complexity
- Taking longer than 30 minutes

---

**Usage**: Type `@quick-dev` in Copilot chat for fast implementation of small changes.
