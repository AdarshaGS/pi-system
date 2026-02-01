# Feature Flag System - Complete Documentation

> **Status**: ✅ Production Ready  
> **Last Updated**: February 1, 2026  
> **Build Status**: Backend ✅ | Frontend ✅

## Table of Contents
1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Architecture](#architecture)
4. [Backend Implementation](#backend-implementation)
5. [Frontend Implementation](#frontend-implementation)
6. [Feature Protection](#feature-protection)
7. [Admin Dashboard](#admin-dashboard)
8. [Testing Guide](#testing-guide)
9. [Troubleshooting](#troubleshooting)
10. [Adding New Features](#adding-new-features)

---

## Overview

### What is Feature Flag System?

A complete feature toggle system that allows admins to enable/disable application features without code deployment. When a feature is disabled:

✅ **Backend**: Returns `403 Forbidden` with proper error message  
✅ **Frontend**: Hides navigation items and shows "Feature Not Available" page  
✅ **Security**: Cannot be bypassed - protected at API level  
✅ **Real-time**: UI updates immediately when features are toggled

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│ Layer 1: Frontend Navigation (UX)                            │
│ └─ Hide disabled features from sidebar                      │
│                                                              │
│ Layer 2: Frontend Route Guard (UX)                          │
│ └─ Show "Feature Not Available" message                     │
│                                                              │
│ Layer 3: Backend API Protection (SECURITY) ← FINAL GUARD   │
│ └─ Return 403 Forbidden - CANNOT BE BYPASSED ✅            │
└─────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### 1. Test Current Setup

```bash
# Start backend (if not running)
cd /Users/adarshgs/Documents/Stocks/App/pi-system
./gradlew bootRun

# Start frontend (if not running)
cd frontend
npm run dev
```

### 2. Disable a Feature

1. Login as admin at `http://localhost:5173/login`
2. Navigate to `http://localhost:5173/admin/features`
3. Find "Budget Management" and toggle it **OFF**
4. Observe:
   - Budget & Cash Flow links disappear from sidebar immediately ✓
   - Navigation auto-updates across all tabs ✓

### 3. Verify Backend Protection

```bash
curl -v 'http://localhost:8082/api/v1/budget/report/1' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

**Expected Response (403 Forbidden):**
```json
{
  "error": "FEATURE_NOT_ENABLED",
  "message": "Feature 'Budget Module' is not enabled",
  "featureName": "BUDGET_MODULE",
  "status": 403
}
```

### 4. Verify Frontend Protection

- Try accessing `http://localhost:5173/budget` directly
- You'll see: "⚠️ Feature Not Available - The BUDGET_MODULE feature is currently disabled"

### 5. Re-enable Feature

- Toggle "Budget Management" back **ON** in admin dashboard
- Links reappear immediately in sidebar ✓
- APIs work again ✓

---

## Architecture

### System Flow

```
Admin Toggles Feature → Database Updated → Cache Cleared
           ↓                                      ↓
    Frontend Refreshes                    Backend Enforces
           ↓                                      ↓
  Navigation Updates                      AOP Intercepts
           ↓                                      ↓
   User Experience                       403 if Disabled
```

### Data Flow When Feature is DISABLED

#### Scenario 1: User Sees Navigation
```
User opens app → FeatureContext loads → isFeatureEnabled('BUDGET_MODULE')
→ Returns false → Navigation link NOT RENDERED → User doesn't see it ✅
```

#### Scenario 2: User Types URL Manually
```
User → http://localhost:5173/budget → FeatureGate checks feature
→ Returns false → Shows "Feature Not Available" message ✅
```

#### Scenario 3: Direct API Call
```
curl → GET /api/v1/budget/report/1 → @RequiresFeature annotation
→ FeatureCheckAspect intercepts → Checks isFeatureEnabled()
→ Returns false → Throws FeatureNotEnabledException
→ FeatureExceptionHandler catches → 403 Forbidden ✅
```

---

## Backend Implementation

### 1. Protected Controllers

| Controller | Feature Flag | Path Protected |
|------------|--------------|----------------|
| BudgetController | `BUDGET_MODULE` | `/api/v1/budget/**` |
| RecurringTransactionController | `RECURRING_TRANSACTIONS` | `/api/v1/budget/recurring/**` |
| AlertController | `ALERTS` | `/api/v1/budget/alerts/**` |
| SubscriptionController | `SUBSCRIPTIONS` | `/api/v1/budget/subscriptions/**` |
| PortfolioController | `PORTFOLIO` | `/api/v1/investments/portfolio/**` |
| StockController | `STOCKS` | `/api/v1/investments/stocks/**` |
| NetWorthController | `NET_WORTH` | `/api/v1/net-worth/**` |
| EtfController | `ETF` | `/api/v1/investments/etfs/**` |

### 2. Protection Mechanism

**Step 1**: Add annotation to controller class:
```java
@RestController
@RequestMapping("/api/v1/budget")
@RequiresFeature(FeatureFlag.BUDGET_MODULE)  // ← Protects entire controller
public class BudgetController {
    // All endpoints automatically protected
}
```

**Step 2**: AOP Aspect intercepts calls:
```java
@Aspect
@Component
public class FeatureCheckAspect {
    @Around("@within(requiresFeature) || @annotation(requiresFeature)")
    public Object checkFeature(ProceedingJoinPoint joinPoint, RequiresFeature requiresFeature) {
        if (!featureConfigService.isFeatureEnabled(flag)) {
            throw new FeatureNotEnabledException(displayName);
        }
        return joinPoint.proceed();
    }
}
```

**Step 3**: Exception handler returns 403:
```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)  // ← CRITICAL: Process before GlobalExceptionHandler
public class FeatureExceptionHandler {
    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity<Map<String, Object>> handleFeatureNotEnabled(Exception ex) {
        // Returns 403 Forbidden
    }
}
```

### 3. Feature Service

```java
@Service
public class FeatureConfigService {
    
    @Cacheable(value = "features", key = "#flag != null ? #flag.name() : 'null'", 
               unless = "#flag == null")
    public boolean isFeatureEnabled(FeatureFlag flag) {
        if (flag == null) return false;
        
        // Query database
        Optional<FeatureConfig> config = featureConfigRepository.findByName(flag.name());
        return config.map(FeatureConfig::isEnabled).orElse(false);
    }
}
```

### 4. Database Schema

```sql
CREATE TABLE feature_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    enabled BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 5. Admin API Endpoints

**Base URL**: `/api/v1/admin/features`  
**Authorization**: Admin role required

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all features |
| GET | `/enabled` | Get only enabled features |
| GET | `/disabled` | Get only disabled features |
| GET | `/{name}` | Get specific feature by name |
| GET | `/{name}/enabled` | Check if feature is enabled |
| POST | `/{name}/enable` | Enable a feature |
| POST | `/{name}/disable` | Disable a feature |
| PUT | `/{name}` | Update feature metadata |
| GET | `/category/{category}` | Get features by category |

---

## Frontend Implementation

### 1. Feature Context

**Location**: `frontend/src/contexts/FeatureContext.jsx`

```jsx
import { useFeatures } from '../contexts/FeatureContext';

const MyComponent = () => {
    const { isFeatureEnabled, loading, refreshFeatures } = useFeatures();
    
    if (isFeatureEnabled('BUDGET_MODULE')) {
        return <BudgetUI />;
    }
    
    return <div>Feature not available</div>;
};
```

**Features:**
- Auto-loads enabled features on mount
- Auto-refreshes every 5 minutes
- Provides `isFeatureEnabled(name)` function
- Manual refresh via `refreshFeatures()`

### 2. FeatureGate Component

**Location**: `frontend/src/components/FeatureGate.jsx`

```jsx
<FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
    <Budget />
</FeatureGate>
```

**Props:**
- `feature` (required): Feature flag name
- `children` (required): Component to render if enabled
- `fallback` (optional): Component to render if disabled
- `showDisabledMessage` (optional): Show "Feature Not Available" message

### 3. Protected Routes

**Location**: `frontend/src/App.jsx`

```jsx
<Route path="budget" element={
    <FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
        <Budget />
    </FeatureGate>
} />
```

### 4. Conditional Navigation

**Location**: `frontend/src/layouts/Layout.jsx`

```jsx
import { useFeatures } from '../contexts/FeatureContext';

const Layout = () => {
    const { isFeatureEnabled } = useFeatures();
    
    return (
        <nav>
            {isFeatureEnabled('BUDGET_MODULE') && (
                <>
                    <NavLink to="/budget">Budget</NavLink>
                    <NavLink to="/cashflow">Cash Flow</NavLink>
                </>
            )}
            {isFeatureEnabled('PORTFOLIO') && (
                <NavLink to="/portfolio">Portfolio</NavLink>
            )}
        </nav>
    );
};
```

### 5. Feature API

**Location**: `frontend/src/api.js`

```javascript
export const featureApi = {
    getAllFeatures: (token) => apiCall('/v1/admin/features', 'GET', null, token),
    getEnabledFeatures: (token) => apiCall('/v1/admin/features/enabled', 'GET', null, token),
    isFeatureEnabled: (name, token) => apiCall(`/v1/admin/features/${name}/enabled`, 'GET', null, token),
    enableFeature: (name, token) => apiCall(`/v1/admin/features/${name}/enable`, 'POST', null, token),
    disableFeature: (name, token) => apiCall(`/v1/admin/features/${name}/disable`, 'POST', null, token),
};
```

---

## Feature Protection

### Protected Routes & Navigation

| UI Feature | Feature Flag | Route | Navigation Visibility |
|------------|--------------|-------|----------------------|
| Budget | BUDGET_MODULE | `/budget` | Conditional |
| Cash Flow | BUDGET_MODULE | `/cashflow` | Conditional |
| Recurring | RECURRING_TRANSACTIONS | `/recurring` | Conditional |
| Portfolio | PORTFOLIO | `/portfolio` | Conditional |
| Dashboard | Always ON | `/dashboard` | Always visible |
| Settings | Always ON | `/settings` | Always visible |

### Error Response Format

When API call is made to disabled feature:

```json
{
  "error": "FEATURE_NOT_ENABLED",
  "message": "Feature 'Budget Module' is not enabled",
  "featureName": "BUDGET_MODULE",
  "status": 403
}
```

**HTTP Status**: 403 Forbidden

---

## Admin Dashboard

### Access
**URL**: `http://localhost:5173/admin/features`  
**Required Role**: ADMIN or SUPER_ADMIN

### Features

#### 1. Feature List
- View all 40+ features in a table
- Color-coded categories
- Real-time stats (enabled/disabled counts)

#### 2. Filter & Search
- Filter by category dropdown
- Search by name/description
- Real-time filtering

#### 3. Toggle Features
- One-click enable/disable
- Loading state during toggle
- Success/error notifications

#### 4. Stats Cards
```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│  Enabled    │  Disabled   │ Categories  │  Filtered   │
│     15      │     25      │      9      │     40      │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

#### 5. Real-time Updates
- Navigation updates immediately when toggled
- All open tabs/windows receive updates
- No page refresh needed

---

## Testing Guide

### Test Plan

#### Test 1: Backend Protection
```bash
# Get your token from localStorage after login
TOKEN="your_jwt_token_here"

# Test disabled feature
curl -v 'http://localhost:8082/api/v1/budget/report/1' \
  -H "Authorization: Bearer $TOKEN"

# Expected: 403 Forbidden
# {
#   "error": "FEATURE_NOT_ENABLED",
#   "message": "Feature 'Budget Module' is not enabled",
#   "featureName": "BUDGET_MODULE",
#   "status": 403
# }
```

#### Test 2: Frontend Navigation
1. Login as admin
2. Go to admin features dashboard
3. Disable "Budget Management"
4. **Expected**: Budget & Cash Flow links disappear from sidebar
5. **Expected**: All tabs/windows update immediately

#### Test 3: Direct URL Access
1. With Budget disabled, navigate to `http://localhost:5173/budget`
2. **Expected**: See "Feature Not Available" message with warning icon
3. **Expected**: No API calls made (check Network tab)

#### Test 4: Feature Re-enable
1. Enable "Budget Management" in admin dashboard
2. **Expected**: Links reappear in sidebar immediately
3. **Expected**: API calls work (200 OK)
4. Navigate to `/budget`
5. **Expected**: Budget page loads successfully

#### Test 5: Multiple Features
1. Disable multiple features (Budget, Portfolio, Recurring)
2. **Expected**: All corresponding navigation items hidden
3. **Expected**: All corresponding APIs return 403
4. Enable one feature
5. **Expected**: Only that feature's navigation reappears

### Automated Testing

```bash
# Backend tests
cd /Users/adarshgs/Documents/Stocks/App/pi-system
./gradlew test --tests *FeatureConfigServiceTest
./gradlew test --tests *FeatureCheckAspectTest

# Frontend tests
cd frontend
npm test -- FeatureContext
npm test -- FeatureGate
```

---

## Troubleshooting

### Problem: API Still Returns Data When Feature Disabled

**Solution**: Restart the backend server
```bash
# Kill existing process
lsof -ti:8082 | xargs kill -9

# Restart server
./gradlew bootRun
```

**Reason**: AOP aspects are loaded at startup. Changes to `@RequiresFeature` annotations require restart.

### Problem: Frontend Navigation Not Updating

**Symptoms**: Toggle feature in admin dashboard, but navigation doesn't update

**Solutions**:
1. Check `AdminFeatures.jsx` calls `refreshFeatures()` after toggle
2. Verify `FeatureContext` is wrapped around entire app
3. Hard refresh browser (Cmd+Shift+R)
4. Check browser console for errors

### Problem: Getting 500 Error Instead of 403

**Symptoms**: API returns 500 Internal Server Error

**Root Cause**: `GlobalExceptionHandler` catching exception before `FeatureExceptionHandler`

**Solution**: Ensure `FeatureExceptionHandler` has `@Order(Ordered.HIGHEST_PRECEDENCE)`

```java
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)  // ← CRITICAL
public class FeatureExceptionHandler {
    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity<Map<String, Object>> handleFeatureNotEnabled(...) {
        // ...
    }
}
```

### Problem: Features Not Loading in Frontend

**Symptoms**: Navigation items missing, but features are enabled

**Solutions**:
1. Check API endpoint: `http://localhost:8082/api/v1/admin/features/enabled`
2. Verify JWT token in localStorage
3. Check browser console for CORS errors
4. Verify backend is running
5. Check `FeatureContext` is not throwing errors

### Problem: Cache Issues

**Symptoms**: Feature state not updating after toggle

**Solutions**:
```bash
# Clear Redis cache (if using Redis)
redis-cli FLUSHALL

# Or restart backend (clears Spring cache)
lsof -ti:8082 | xargs kill -9 && ./gradlew bootRun
```

### Problem: Database Out of Sync

**Symptoms**: Feature shows different state in DB vs application

**Solution**: Check database
```sql
SELECT name, enabled FROM feature_config WHERE name = 'BUDGET_MODULE';
```

If incorrect, update:
```sql
UPDATE feature_config SET enabled = false WHERE name = 'BUDGET_MODULE';
```

---

## Adding New Features

### Step-by-Step Guide

#### Step 1: Add Feature Flag Enum
```java
// File: FeatureFlag.java
public enum FeatureFlag {
    BUDGET_MODULE("Budget Module", "budget"),
    MY_NEW_FEATURE("My New Feature", "category"),  // ← Add here
    // ...
}
```

#### Step 2: Create Database Migration
```sql
-- File: V99__add_new_feature.sql
INSERT INTO feature_config (name, display_name, description, category, enabled)
VALUES ('MY_NEW_FEATURE', 'My New Feature', 'Description here', 'category', false);
```

#### Step 3: Protect Controller
```java
@RestController
@RequestMapping("/api/v1/myfeature")
@RequiresFeature(FeatureFlag.MY_NEW_FEATURE)  // ← Add annotation
public class MyFeatureController {
    // All endpoints now protected
}
```

#### Step 4: Add Frontend Route Protection
```jsx
// File: App.jsx
<Route path="myfeature" element={
    <FeatureGate feature="MY_NEW_FEATURE" showDisabledMessage>
        <MyFeature />
    </FeatureGate>
} />
```

#### Step 5: Add Conditional Navigation
```jsx
// File: Layout.jsx
{isFeatureEnabled('MY_NEW_FEATURE') && (
    <NavLink to="/myfeature">My Feature</NavLink>
)}
```

#### Step 6: Test
1. Build and restart backend
2. Refresh frontend
3. Feature should appear in admin dashboard
4. Toggle to test protection

---

## Key Files Reference

### Backend
```
src/main/java/com/common/features/
├── FeatureFlag.java                 # Feature enum definitions
├── RequiresFeature.java             # Annotation for protection
├── FeatureCheckAspect.java          # AOP interceptor
├── FeatureNotEnabledException.java  # Custom exception
├── FeatureExceptionHandler.java     # Exception handler (403 response)
└── FeatureConfigService.java        # Feature state service

src/main/java/com/budget/controller/
├── BudgetController.java            # Protected with @RequiresFeature
└── ... (7 more controllers)

src/main/resources/db/migration/
└── V33__create_feature_config_table.sql
```

### Frontend
```
frontend/src/
├── contexts/
│   └── FeatureContext.jsx           # Global feature state
├── components/
│   └── FeatureGate.jsx              # Route protection component
├── pages/
│   ├── Budget.jsx                   # Protected page
│   └── admin/
│       └── AdminFeatures.jsx        # Admin dashboard for features
├── layouts/
│   └── Layout.jsx                   # Navigation with conditional rendering
├── App.jsx                          # Routes with FeatureGate
└── api.js                           # Feature API endpoints
```

---

## Summary

### What This System Provides

✅ **Admin Control**: Toggle features without code deployment  
✅ **Security**: API-level protection that cannot be bypassed  
✅ **User Experience**: Clean UI that only shows enabled features  
✅ **Real-time Updates**: Navigation updates immediately  
✅ **Single Source of Truth**: Database-only configuration  
✅ **Defense in Depth**: 3 layers of protection (navigation, routes, API)  
✅ **Performance**: Cached feature state with auto-refresh  
✅ **Maintainability**: Easy to add new features

### Production Checklist

- [x] Backend protection with AOP
- [x] Frontend feature checking
- [x] Admin dashboard for management
- [x] Error handling (403 responses)
- [x] Database migrations
- [x] Cache management
- [x] Real-time UI updates
- [x] Documentation complete
- [x] Build successful (Backend & Frontend)
- [x] Tested with disabled features

### Important Notes

1. **Server Restart Required**: After adding `@RequiresFeature` to controllers
2. **Exception Handler Order**: `FeatureExceptionHandler` must have `@Order(Ordered.HIGHEST_PRECEDENCE)`
3. **Feature Names**: Must match exactly between backend enum and frontend strings
4. **Cache**: Features cached for performance; clears on enable/disable
5. **Auto-refresh**: Frontend reloads features every 5 minutes

---

**System Status**: 🎉 Production Ready  
**Last Tested**: February 1, 2026  
**API Protection**: ✅ Working (403 Forbidden)  
**UI Protection**: ✅ Working (Navigation conditional)  
**Build Status**: ✅ Backend & Frontend passing

