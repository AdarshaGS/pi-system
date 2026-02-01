# 🚀 Feature Toggle System - Quick Start Guide

## 📍 Access

### Admin UI Dashboard (Recommended)
```
http://localhost:3000/admin/features
```

**Features:**
- ✅ View all 40+ features in a table
- ✅ Filter by category
- ✅ Search features
- ✅ Toggle on/off with one click
- ✅ Real-time updates

**Requires:** Admin login

---

## 📡 API Endpoints (Admin Only)

### Get All Features
```
GET /api/v1/admin/features
Authorization: Bearer ADMIN_TOKEN
```

---

## 🎨 Frontend Integration (3 Steps)

### 1. Load Features on App Start
```javascript
// App.jsx
import { useState, useEffect } from 'react';

function App() {
  const [features, setFeatures] = useState([]);
  
  useEffect(() => {
    fetch('/api/v1/public/features/config')
      .then(res => res.json())
      .then(config => {
        setFeatures(config.enabledFeatures);
        // Optional: cache for offline
        localStorage.setItem('features', JSON.stringify(config.enabledFeatures));
      })
      .catch(err => {
        // Fallback to cached features
        const cached = localStorage.getItem('features');
        if (cached) setFeatures(JSON.parse(cached));
      });
  }, []);
  
  return (
    <FeatureContext.Provider value={features}>
      <YourApp />
    </FeatureContext.Provider>
  );
}
```

### 2. Create Feature Hook
```javascript
// hooks/useFeatures.js
import { useContext } from 'react';
import { FeatureContext } from './FeatureContext';

export function useFeatures() {
  const features = useContext(FeatureContext);
  
  const isEnabled = (feature) => features.includes(feature);
  
  return { features, isEnabled };
}
```

### 3. Conditional Rendering
```jsx
// pages/Dashboard.jsx
import { useFeatures } from '../hooks/useFeatures';

function Dashboard() {
  const { isEnabled } = useFeatures();
  
  return (
    <div>
      {isEnabled('BUDGET_MODULE') && <BudgetWidget />}
      {isEnabled('SUBSCRIPTIONS') && <SubscriptionManager />}
      {isEnabled('TAX_MODULE') && <TaxCalculator />}
      {isEnabled('PORTFOLIO') && <InvestmentPortfolio />}
    </div>
  );
}
```

---

## 🛡️ Backend Protection

### Protect Entire Controller
```java
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiresFeature(FeatureFlag.SUBSCRIPTIONS)
public class SubscriptionController {
    // All endpoints require SUBSCRIPTIONS feature
}
```

### Protect Specific Method
```java
@GetMapping("/forecast")
@RequiresFeature(FeatureFlag.BUDGET_FORECASTING)
public ResponseEntity<ForecastDTO> getForecast() {
    // Only works if BUDGET_FORECASTING is enabled
}
```

---

## 🔧 Admin Management

### Toggle via cURL
```bash
# Disable feature
curl -X POST http://localhost:8080/api/v1/admin/features/SUBSCRIPTIONS/disable \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"

# Enable feature
curl -X POST http://localhost:8080/api/v1/admin/features/SUBSCRIPTIONS/enable \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

### Toggle via SQL (Emergency)
```sql
-- Disable feature
UPDATE feature_config SET enabled = FALSE WHERE feature_flag = 'SUBSCRIPTIONS';

-- Enable feature
UPDATE feature_config SET enabled = TRUE WHERE feature_flag = 'SUBSCRIPTIONS';
```

---

## 📊 Available Features (40+)

### Budget Module
- `BUDGET_MODULE`
- `EXPENSES`
- `INCOME`
- `ALERTS`
- `RECURRING_TRANSACTIONS`
- `CUSTOM_CATEGORIES`
- `CASH_FLOW_ANALYSIS`
- `SUBSCRIPTIONS`

### Tax Module
- `TAX_MODULE`
- `TAX_REGIME_COMPARISON`
- `CAPITAL_GAINS`
- `TAX_SAVING_RECOMMENDATIONS`
- `TDS_TRACKING`
- `TAX_PROJECTIONS`
- `ITR_EXPORT`

### Investments
- `PORTFOLIO`
- `STOCKS`
- `MUTUAL_FUNDS`
- `BONDS`
- `GOLD`
- `ETF`
- `REAL_ESTATE`

### Banking
- `BANK_ACCOUNTS`
- `CREDIT_CARDS`
- `LOANS`
- `FIXED_DEPOSITS`
- `RECURRING_DEPOSITS`

### Insurance
- `INSURANCE`
- `LIFE_INSURANCE`
- `HEALTH_INSURANCE`

### Net Worth
- `NET_WORTH`
- `ASSET_ALLOCATION`

### Admin
- `ADMIN_PORTAL`
- `USER_MANAGEMENT`
- `AUDIT_LOGS`
- `REPORTS`

---

## 🎯 Common Scenarios

### Scenario 1: Hide Menu Items
```jsx
const menuItems = [
  { name: 'Budget', path: '/budget', feature: 'BUDGET_MODULE' },
  { name: 'Subscriptions', path: '/subscriptions', feature: 'SUBSCRIPTIONS' },
  { name: 'Tax', path: '/tax', feature: 'TAX_MODULE' },
  { name: 'Portfolio', path: '/portfolio', feature: 'PORTFOLIO' }
];

function Navigation() {
  const { isEnabled } = useFeatures();
  
  return (
    <nav>
      {menuItems
        .filter(item => isEnabled(item.feature))
        .map(item => (
          <Link key={item.path} to={item.path}>
            {item.name}
          </Link>
        ))}
    </nav>
  );
}
```

### Scenario 2: Feature Component Wrapper
```jsx
function FeatureGate({ feature, children, fallback = null }) {
  const { isEnabled } = useFeatures();
  
  if (!isEnabled(feature)) {
    return fallback;
  }
  
  return children;
}

// Usage
<FeatureGate feature="SUBSCRIPTIONS">
  <SubscriptionManager />
</FeatureGate>
```

### Scenario 3: Protected Routes
```jsx
function ProtectedRoute({ feature, component: Component, ...rest }) {
  const { isEnabled } = useFeatures();
  
  return (
    <Route
      {...rest}
      render={props =>
        isEnabled(feature) ? (
          <Component {...props} />
        ) : (
          <Redirect to="/dashboard" />
        )
      }
    />
  );
}

// Usage
<ProtectedRoute
  path="/subscriptions"
  feature="SUBSCRIPTIONS"
  component={SubscriptionsPage}
/>
```

### Scenario 4: Emergency Disable
```bash
# Production bug detected - immediately disable feature
curl -X POST https://api.yourapp.com/api/v1/admin/features/SUBSCRIPTIONS/disable \
  -H "Authorization: Bearer ADMIN_TOKEN"

# UI automatically hides feature within seconds
# No deployment or restart needed
# Fix bug, test, re-enable
```

---

## 🔍 Testing

### Check Feature Status (Public)
```bash
curl http://localhost:8080/api/v1/public/features/SUBSCRIPTIONS/enabled
```
Response:
```json
{
  "enabled": true,
  "exists": true
}
```

### Get All Enabled Features
```bash
curl http://localhost:8080/api/v1/public/features/enabled
```
Response:
```json
["BUDGET_MODULE", "EXPENSES", "SUBSCRIPTIONS", ...]
```

---

## 🎉 That's It!

**3 Simple Steps:**
1. ✅ Load features on app start: `GET /api/v1/public/features/config`
2. ✅ Store in context/state
3. ✅ Conditional render: `{isEnabled('FEATURE') && <Component />}`

**Admin Management:**
- Toggle via REST API: `POST /api/v1/admin/features/{name}/enable`
- Or directly in database: `UPDATE feature_config SET enabled = ...`

**Backend Protection:**
- Add `@RequiresFeature(FeatureFlag.FEATURE_NAME)` to controllers/methods

---

For detailed documentation, see:
- [FEATURE_TOGGLE_IMPLEMENTATION.md](FEATURE_TOGGLE_IMPLEMENTATION.md) - Complete guide
- [FEATURE_TOGGLE_UPDATE.md](FEATURE_TOGGLE_UPDATE.md) - Latest changes
- [docs/FEATURE_TOGGLE_SYSTEM.md](docs/FEATURE_TOGGLE_SYSTEM.md) - Comprehensive documentation
