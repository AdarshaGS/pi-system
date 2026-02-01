# Feature Flag System - Complete Implementation

## Overview
Complete feature flag system with backend protection and frontend visibility control. When a feature is disabled:
- **Backend**: Returns 403 Forbidden with proper error message
- **Frontend**: Hides navigation items and shows "Feature Not Available" message

## Architecture

### Backend Protection
- **Annotation**: `@RequiresFeature(FeatureFlag.FEATURE_NAME)` on controllers
- **Aspect**: `FeatureCheckAspect` intercepts method calls
- **Exception**: `FeatureNotEnabledException` → 403 Forbidden
- **Admin API**: `/api/v1/admin/features` (admin-only access)

### Frontend Feature Checking
- **Context**: `FeatureContext` - Global feature state management
- **Hook**: `useFeatures()` - Access features in any component
- **Component**: `<FeatureGate>` - Conditional rendering wrapper
- **API**: `featureApi` - Feature management endpoints

## Components

### 1. FeatureContext (`frontend/src/contexts/FeatureContext.jsx`)
```javascript
import { useFeatures } from '../contexts/FeatureContext';

const { isFeatureEnabled, loading, refreshFeatures } = useFeatures();

// Check if feature is enabled
if (isFeatureEnabled('BUDGET_MODULE')) {
    // Show budget UI
}
```

**Features:**
- Loads enabled features on mount
- Auto-refreshes every 5 minutes
- Provides `isFeatureEnabled(featureName)` function
- Handles loading states and errors
- Manual refresh via `refreshFeatures()`

### 2. FeatureGate Component (`frontend/src/components/FeatureGate.jsx`)
```jsx
<FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
    <Budget />
</FeatureGate>
```

**Props:**
- `feature` (required): Feature flag name (e.g., "BUDGET_MODULE")
- `children` (required): Component to render if feature is enabled
- `fallback` (optional): Component to render if feature is disabled
- `showDisabledMessage` (optional): Show "Feature Not Available" message

### 3. Feature API (`frontend/src/api.js`)
```javascript
import { featureApi } from '../api';

// Get all features
const features = await featureApi.getAllFeatures(token);

// Get only enabled features
const enabled = await featureApi.getEnabledFeatures(token);

// Check specific feature
const isEnabled = await featureApi.isFeatureEnabled('BUDGET_MODULE', token);

// Enable/disable
await featureApi.enableFeature('BUDGET_MODULE', token);
await featureApi.disableFeature('BUDGET_MODULE', token);
```

## Protected Features

### Backend Controllers (8 controllers protected)

| Controller | Feature Flag | Endpoints |
|------------|--------------|-----------|
| BudgetController | BUDGET_MODULE | `/api/v1/budget/**` |
| RecurringTransactionController | RECURRING_TRANSACTIONS | `/api/v1/budget/recurring/**` |
| AlertController | ALERTS | `/api/v1/budget/alerts/**` |
| PortfolioController | PORTFOLIO | `/api/v1/investments/portfolio/**` |
| StockController | STOCKS | `/api/v1/investments/stocks/**` |
| NetWorthController | NET_WORTH | `/api/v1/net-worth/**` |
| EtfController | ETF | `/api/v1/investments/etfs/**` |
| SubscriptionController | SUBSCRIPTIONS | `/api/v1/budget/subscriptions/**` |

### Frontend Routes

| Route | Feature Flag | Component |
|-------|--------------|-----------|
| `/budget` | BUDGET_MODULE | Budget |
| `/cashflow` | BUDGET_MODULE | CashFlow |
| `/recurring` | RECURRING_TRANSACTIONS | RecurringTransactions |
| `/portfolio` | PORTFOLIO | Portfolio |

### Navigation Items (Sidebar)
Navigation items are conditionally rendered based on feature flags:
- **Budget** (Wallet icon): Requires `BUDGET_MODULE`
- **Cash Flow** (TrendingUp icon): Requires `BUDGET_MODULE`
- **Recurring** (RefreshCw icon): Requires `RECURRING_TRANSACTIONS`
- **Portfolio** (Briefcase icon): Requires `PORTFOLIO`

## Usage Examples

### Example 1: Protecting a Route
```jsx
// In App.jsx
<Route path="budget" element={
    <FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
        <Budget />
    </FeatureGate>
} />
```

### Example 2: Conditional Navigation
```jsx
// In Layout.jsx
import { useFeatures } from '../contexts/FeatureContext';

const Layout = () => {
    const { isFeatureEnabled } = useFeatures();
    
    return (
        <nav>
            {isFeatureEnabled('BUDGET_MODULE') && (
                <NavLink to="/budget">Budget</NavLink>
            )}
        </nav>
    );
};
```

### Example 3: Conditional Feature in Component
```jsx
// In any component
import { useFeatures } from '../contexts/FeatureContext';

const MyComponent = () => {
    const { isFeatureEnabled } = useFeatures();
    
    return (
        <div>
            {isFeatureEnabled('BUDGET_MODULE') && (
                <button>Create Budget</button>
            )}
        </div>
    );
};
```

### Example 4: Admin Features Toggle
```jsx
// In AdminFeatures.jsx
const toggleFeature = async (featureName, currentEnabled) => {
    const action = currentEnabled ? 'disable' : 'enable';
    await featureApi[`${action}Feature`](featureName, token);
    
    // Refresh global feature context
    await refreshFeatures();
};
```

## Error Handling

### Backend Error Response (403 Forbidden)
```json
{
    "error": "FEATURE_NOT_ENABLED",
    "message": "Feature 'BUDGET_MODULE' is not enabled",
    "featureName": "BUDGET_MODULE",
    "status": 403
}
```

### Frontend Error Display
When a feature is disabled and user tries to access:
1. **Navigation**: Item not visible in sidebar
2. **Direct URL**: Shows "Feature Not Available" message with AlertCircle icon
3. **API Call**: 403 error caught and handled by component

## Testing

### Test Feature Toggle Flow

1. **Start Backend**
```bash
cd /Users/adarshgs/Documents/Stocks/App/pi-system
./gradlew bootRun
```

2. **Start Frontend**
```bash
cd frontend
npm run dev
```

3. **Login as Admin**
```
http://localhost:5173/login
```

4. **Navigate to Feature Management**
```
http://localhost:5173/admin/features
```

5. **Disable BUDGET_MODULE**
- Click toggle button for "Budget Management"
- Observe navigation items disappear from sidebar
- Try accessing http://localhost:5173/budget
- Should see "Feature Not Available" message

6. **Test Backend Protection**
```bash
curl 'http://localhost:8082/api/v1/budget/limit/1' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

Response when disabled:
```json
{
    "error": "FEATURE_NOT_ENABLED",
    "message": "Feature 'BUDGET_MODULE' is not enabled",
    "featureName": "BUDGET_MODULE",
    "status": 403
}
```

7. **Re-enable Feature**
- Toggle button again in Admin Features
- Navigation items reappear immediately
- APIs work again

## Implementation Checklist

### Backend ✅
- [x] `@RequiresFeature` annotation on 8 controllers
- [x] `FeatureCheckAspect` for AOP interception
- [x] `FeatureNotEnabledException` with proper error details
- [x] Admin-only API at `/api/v1/admin/features`
- [x] Database-only configuration (feature_config table)
- [x] Build successful

### Frontend ✅
- [x] `FeatureContext` for global state
- [x] `useFeatures()` hook
- [x] `FeatureGate` component
- [x] Feature API endpoints in api.js
- [x] Protected routes with FeatureGate
- [x] Conditional navigation rendering
- [x] AdminFeatures auto-refresh global context
- [x] Build successful

## Key Benefits

1. **Security**: Features are protected at API level - cannot be bypassed
2. **UX**: Users don't see UI for disabled features
3. **Admin Control**: Easy toggle from admin dashboard
4. **Real-time**: Navigation updates immediately when features toggled
5. **Consistency**: Same feature names in backend and frontend
6. **Performance**: Features cached and refreshed every 5 minutes

## Feature Flags Available

All 40+ features from FeatureFlag enum are supported:
- BUDGET_MODULE
- RECURRING_TRANSACTIONS
- SUBSCRIPTIONS
- ALERTS
- PORTFOLIO
- STOCKS
- MUTUAL_FUNDS
- ETF
- NET_WORTH
- TAX_PLANNING
- And more...

## Admin Dashboard Access

**URL**: `http://localhost:5173/admin/features`

**Features:**
- View all 40+ features
- Filter by category
- Search by name/description
- Enable/Disable with one click
- Real-time stats (enabled/disabled counts)
- Category-based color coding

## Migration Notes

### From Mixed Config to Database-Only
- ✅ Removed application.yml environment dependency
- ✅ Single source of truth: feature_config table
- ✅ All APIs are admin-only
- ✅ No public feature endpoints

### Adding New Features
1. Add enum to `FeatureFlag.java`
2. Add migration to insert into feature_config table
3. Add `@RequiresFeature(FeatureFlag.NEW_FEATURE)` to controller
4. Wrap frontend route with `<FeatureGate feature="NEW_FEATURE">`
5. Conditionally render navigation item if needed

## Troubleshooting

### Features Not Loading in Frontend
- Check browser console for errors
- Verify API endpoint: `http://localhost:8082/api/v1/admin/features/enabled`
- Check JWT token in localStorage
- Ensure backend is running

### Navigation Items Not Updating
- AdminFeatures now calls `refreshFeatures()` after toggle
- Context auto-refreshes every 5 minutes
- Hard refresh page if needed (Cmd+Shift+R)

### 403 Error Even When Feature Enabled
- Check cache: Feature cache might be stale
- Restart backend to clear cache
- Verify feature in database: `SELECT * FROM feature_config WHERE name = 'FEATURE_NAME'`

## Documentation
- [FEATURE_TOGGLE_FINAL.md](FEATURE_TOGGLE_FINAL.md) - Backend implementation
- [FEATURE_PROTECTION_COMPLETE.md](FEATURE_PROTECTION_COMPLETE.md) - API protection details
- [FEATURE_TOGGLE_ADMIN_IMPLEMENTATION.md](FEATURE_TOGGLE_ADMIN_IMPLEMENTATION.md) - Admin UI guide

---

**Status**: ✅ Complete
**Last Updated**: February 1, 2026
**Build Status**: Backend ✅ | Frontend ✅
