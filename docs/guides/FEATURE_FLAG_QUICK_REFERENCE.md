# Feature Flag Quick Reference

## Problem Statement
**Question**: If the budget module is disabled, should it be invisible in frontend and backend API should not respond?

**Answer**: YES! ✅ Now fully implemented.

## What Happens When Feature is Disabled

### Backend Behavior
- API returns **403 Forbidden**
- Response includes proper error details
- Example:
```bash
curl 'http://localhost:8082/api/v1/budget/limit/1' \
  -H 'Authorization: Bearer TOKEN'
```
Response:
```json
{
    "error": "FEATURE_NOT_ENABLED",
    "message": "Feature 'BUDGET_MODULE' is not enabled",
    "featureName": "BUDGET_MODULE",
    "status": 403
}
```

### Frontend Behavior
- **Navigation**: Budget/Cash Flow links hidden from sidebar
- **Direct Access**: Shows "Feature Not Available" message
- **Real-time**: Updates immediately when admin toggles feature

## Quick Testing

### 1. Test Backend Protection
```bash
# Get your token from localStorage after login
curl 'http://localhost:8082/api/v1/budget/limit/1' \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

### 2. Test Frontend Visibility
1. Login as admin
2. Go to http://localhost:5173/admin/features
3. Find "Budget Management" feature
4. Click toggle to disable
5. Watch sidebar - Budget & Cash Flow items disappear immediately
6. Try accessing http://localhost:5173/budget
7. You'll see: "Feature Not Available - The BUDGET_MODULE feature is currently disabled"

### 3. Test Re-enabling
1. Go back to admin features
2. Toggle "Budget Management" back on
3. Sidebar items reappear automatically
4. APIs work again

## Feature Mapping

| UI Feature | Feature Flag | Backend APIs Protected |
|------------|--------------|------------------------|
| Budget | BUDGET_MODULE | `/api/v1/budget/**` |
| Cash Flow | BUDGET_MODULE | `/api/v1/budget/**` |
| Recurring | RECURRING_TRANSACTIONS | `/api/v1/budget/recurring/**` |
| Portfolio | PORTFOLIO | `/api/v1/investments/portfolio/**` |

## Admin Dashboard
**URL**: http://localhost:5173/admin/features

**Features:**
- Toggle any feature on/off with one click
- Search features by name
- Filter by category
- Real-time UI updates (navigation items appear/disappear)

## Code Architecture

### Backend Protection
```java
@RestController
@RequiresFeature(FeatureFlag.BUDGET_MODULE)  // <- This protects the entire controller
@RequestMapping("/api/v1/budget")
public class BudgetController {
    // All endpoints automatically protected
}
```

### Frontend Visibility
```jsx
// In Layout.jsx - Conditionally show navigation
{isFeatureEnabled('BUDGET_MODULE') && (
    <NavLink to="/budget">Budget</NavLink>
)}

// In App.jsx - Protect route
<Route path="budget" element={
    <FeatureGate feature="BUDGET_MODULE" showDisabledMessage>
        <Budget />
    </FeatureGate>
} />
```

## Summary

✅ **Backend**: Returns 403 when feature disabled  
✅ **Frontend**: Hides UI when feature disabled  
✅ **Admin**: Easy toggle from dashboard  
✅ **Real-time**: Immediate updates when toggled  
✅ **Secure**: Cannot bypass - protected at API level  

---

**Status**: Production Ready  
**Build**: Backend ✅ | Frontend ✅  
**Last Updated**: February 1, 2026
