# Feature Flag System - Architecture Flow

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         ADMIN DASHBOARD                              │
│  http://localhost:5173/admin/features                               │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────┐       │
│  │ Feature Management                                        │       │
│  │                                                           │       │
│  │ Budget Management         [ON]  ← Click to toggle        │       │
│  │ Recurring Transactions    [OFF]                          │       │
│  │ Portfolio Management      [ON]                           │       │
│  └─────────────────────────────────────────────────────────┘       │
│                            │                                         │
│                            │ Toggle Feature                          │
│                            ▼                                         │
│  POST /api/v1/admin/features/BUDGET_MODULE/disable                  │
│                            │                                         │
└────────────────────────────┼─────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    FEATURE_CONFIG TABLE                              │
│  ┌────────────────────┬──────────┬─────────────────────────┐       │
│  │ name               │ enabled  │ category                 │       │
│  ├────────────────────┼──────────┼─────────────────────────┤       │
│  │ BUDGET_MODULE      │ false ✗  │ budget                   │       │
│  │ PORTFOLIO          │ true  ✓  │ investments              │       │
│  │ RECURRING_TRANS... │ false ✗  │ budget                   │       │
│  └────────────────────┴──────────┴─────────────────────────┘       │
└─────────────────────────────────────────────────────────────────────┘
                             │
                             │ Feature State
                             │
        ┌────────────────────┴────────────────────┐
        │                                         │
        ▼                                         ▼
┌─────────────────────┐                 ┌─────────────────────┐
│   FRONTEND          │                 │   BACKEND           │
│                     │                 │                     │
│ FeatureContext      │                 │ FeatureConfigService│
│   ↓                 │                 │   ↓                 │
│ useFeatures()       │                 │ @Cacheable          │
│   ↓                 │                 │   ↓                 │
│ isFeatureEnabled()  │                 │ isFeatureEnabled()  │
└─────────────────────┘                 └─────────────────────┘
        │                                         │
        │                                         │
        ▼                                         ▼
┌─────────────────────┐                 ┌─────────────────────┐
│   SIDEBAR           │                 │   @RequiresFeature  │
│                     │                 │                     │
│ ✓ Dashboard         │                 │ BudgetController    │
│ ✗ Budget  (hidden)  │                 │   @RequiresFeature( │
│ ✗ Cash Flow (hidden)│                 │     BUDGET_MODULE)  │
│ ✓ Portfolio         │                 │                     │
│ ✗ Recurring (hidden)│                 │ When disabled:      │
│ ✓ Insights          │                 │   ↓                 │
│ ✓ Settings          │                 │ FeatureCheckAspect  │
└─────────────────────┘                 │   ↓                 │
        │                                │ Throws Exception    │
        │ User clicks Budget             │   ↓                 │
        │ (if types URL manually)        │ FeatureException    │
        ▼                                │   Handler           │
┌─────────────────────┐                 │   ↓                 │
│   <FeatureGate>     │                 │ 403 Forbidden       │
│                     │                 └─────────────────────┘
│ Shows message:      │                          │
│ ┌─────────────────┐ │                          │
│ │ ⚠️ Feature Not  │ │                          │
│ │ Available       │ │                          │
│ │                 │ │                          │
│ │ The BUDGET      │ │                          │
│ │ _MODULE feature │ │                          │
│ │ is currently    │ │                          │
│ │ disabled.       │ │                          │
│ └─────────────────┘ │                          │
└─────────────────────┘                          │
                                                  │
                User attempts API call            │
                    ↓                             │
        curl /api/v1/budget/limit/1               │
                    ↓                             │
                    └─────────────────────────────┘
                                  ↓
                          ┌───────────────────┐
                          │ Response:         │
                          │ {                 │
                          │   error: "403",   │
                          │   message: "..."  │
                          │ }                 │
                          └───────────────────┘
```

## Request Flow When Feature is DISABLED

### Scenario 1: User Clicks Budget in Sidebar
```
1. Frontend checks: isFeatureEnabled('BUDGET_MODULE')
2. Returns: false
3. Result: Navigation link NOT RENDERED in sidebar
4. User doesn't see the option at all ✅
```

### Scenario 2: User Types URL Manually
```
1. User navigates to: http://localhost:5173/budget
2. Route wrapped in: <FeatureGate feature="BUDGET_MODULE">
3. FeatureGate checks: isFeatureEnabled('BUDGET_MODULE')
4. Returns: false
5. Shows: "Feature Not Available" message ✅
6. API call: NEVER MADE (prevented by frontend)
```

### Scenario 3: Direct API Call (curl/Postman)
```
1. Request: GET /api/v1/budget/limit/1
2. Spring intercepts at: @RequiresFeature(BUDGET_MODULE)
3. FeatureCheckAspect checks: isFeatureEnabled()
4. Returns: false
5. Throws: FeatureNotEnabledException
6. FeatureExceptionHandler catches
7. Response: 403 Forbidden with error details ✅
```

## Request Flow When Feature is ENABLED

### Happy Path
```
1. Admin enables feature in dashboard
   POST /api/v1/admin/features/BUDGET_MODULE/enable
   ↓
2. Database updated: enabled = true
   ↓
3. Backend cache cleared (or updated)
   ↓
4. Frontend refreshFeatures() called
   ↓
5. FeatureContext updated
   ↓
6. Sidebar re-renders
   ↓
7. Budget link now visible ✓
   ↓
8. User clicks Budget
   ↓
9. Route renders normally
   ↓
10. API calls work (200 OK) ✓
```

## Component Interaction

```
┌────────────────────────────────────────────────────────────┐
│                    App Component                            │
│  <FeatureProvider>  ← Wraps entire app                     │
│    ├── <Router>                                             │
│    │   ├── <Layout> ← Contains sidebar navigation          │
│    │   │   ├── useFeatures() ← Access feature state       │
│    │   │   └── Conditional NavLinks                        │
│    │   │                                                    │
│    │   └── <Routes>                                         │
│    │       ├── /dashboard ← Always visible                 │
│    │       ├── /budget                                      │
│    │       │   └── <FeatureGate feature="BUDGET_MODULE">   │
│    │       │       └── <Budget /> ← Protected component    │
│    │       │                                                │
│    │       ├── /portfolio                                   │
│    │       │   └── <FeatureGate feature="PORTFOLIO">       │
│    │       │       └── <Portfolio />                       │
│    │       │                                                │
│    │       └── /admin/features                             │
│    │           └── <AdminFeatures />                       │
│    │               ├── useFeatures() ← For refresh         │
│    │               └── Toggle buttons                      │
└────────────────────────────────────────────────────────────┘
```

## State Management

```
FeatureContext State:
{
  features: {
    "BUDGET_MODULE": false,        ← Hidden in sidebar
    "PORTFOLIO": true,             ← Visible in sidebar
    "RECURRING_TRANSACTIONS": false ← Hidden in sidebar
  },
  loading: false,
  error: null,
  isFeatureEnabled: (name) => features[name] === true,
  refreshFeatures: async () => { /* reload from API */ }
}

Backend Cache:
@Cacheable(value = "features", key = "#flag != null ? #flag.name() : 'null'")
├── Cache Key: "BUDGET_MODULE"
├── Cache Value: false
└── Cleared on: enable/disable operations
```

## Timeline: Feature Toggle

```
T=0s:   Admin clicks "Budget Management" toggle
        │
T=0.1s: POST /api/v1/admin/features/BUDGET_MODULE/disable
        │
T=0.2s: Database UPDATE: enabled = false
        │
T=0.3s: Backend cache cleared
        │
T=0.4s: Frontend refreshFeatures() called
        │
T=0.5s: GET /api/v1/admin/features/enabled
        │
T=0.6s: FeatureContext updated
        │
T=0.7s: Layout component re-renders
        │
T=0.8s: Budget/Cash Flow links disappear ✓
        │
T=1.0s: Success message: "Budget Management disabled"
```

## Security Layers

```
Layer 1: Frontend Navigation
├── Purpose: UX - Don't show disabled features
├── Implementation: Conditional rendering
└── Can bypass: Yes (manual URL entry)

Layer 2: Frontend Route Guard
├── Purpose: UX - Show helpful message
├── Implementation: <FeatureGate> component
└── Can bypass: Yes (direct API call)

Layer 3: Backend AOP Protection (FINAL SECURITY)
├── Purpose: Security - Enforce at API level
├── Implementation: @RequiresFeature + Aspect
└── Can bypass: NO ✅ CANNOT BE BYPASSED
    ├── All requests intercepted
    ├── Feature check before method execution
    └── 403 Forbidden if disabled
```

## Key Benefits

1. **Defense in Depth**: 3 layers of protection
2. **User Experience**: Users don't see disabled features
3. **Security**: Cannot bypass - API level enforcement
4. **Real-time**: Immediate UI updates
5. **Admin Control**: Easy toggle from dashboard
6. **Single Source**: Database is single source of truth

## Testing Checklist

- [ ] Disable feature in admin dashboard
- [ ] Verify navigation link disappears
- [ ] Try manual URL navigation → See "Feature Not Available"
- [ ] Try API call → Get 403 Forbidden
- [ ] Enable feature again
- [ ] Verify navigation link reappears
- [ ] Verify API calls work (200 OK)

---

**Status**: ✅ Production Ready  
**Architecture**: Defense in Depth (3 layers)  
**Last Updated**: February 1, 2026
