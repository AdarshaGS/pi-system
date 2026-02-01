# 🎛️ Feature Toggle System - Complete Guide

**Date:** February 1, 2026  
**Status:** ✅ **PRODUCTION READY**  
**Build:** ✅ **SUCCESSFUL**

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Quick Start](#quick-start)
4. [Configuration Methods](#configuration-methods)
5. [API Endpoints](#api-endpoints)
6. [Frontend Integration](#frontend-integration)
7. [Admin Management](#admin-management)
8. [Adding New Features](#adding-new-features)
9. [Best Practices](#best-practices)

---

## 🎯 Overview

The Feature Toggle System allows you to enable/disable features dynamically without code changes or redeployment. This enables:

- **Safe Rollouts:** Gradually enable features for users
- **A/B Testing:** Test features with specific user groups
- **Emergency Disable:** Quickly disable problematic features
- **Beta Features:** Release experimental features to select users
- **Subscription Tiers:** Gate features behind subscription levels
- **UI Adaptation:** UI automatically hides disabled features

---

## 🏗️ Architecture

### 3-Layer Configuration Priority

```
1. Database (Runtime) 🔥 Highest Priority
   ↓
2. application.yml (Deployment)
   ↓
3. Default (Code) - All features enabled by default
```

### Components

1. **FeatureFlag Enum** - Defines all features
2. **FeatureConfig Entity** - Database storage
3. **FeatureConfigService** - Business logic
4. **FeatureController** - REST API
5. **@RequiresFeature Annotation** - Method protection
6. **FeatureCheckAspect** - AOP interceptor

---

## 🚀 Quick Start

### For Frontend Developers

#### 1. Check Enabled Features on App Load

```javascript
// Fetch feature configuration
const response = await fetch('/api/v1/features/config');
const config = await response.json();

// Store in app state
const enabledFeatures = config.enabledFeatures;
// ['BUDGET_MODULE', 'TAX_MODULE', 'SUBSCRIPTIONS', ...]

// Check if specific feature is enabled
const hasSubscriptions = enabledFeatures.includes('SUBSCRIPTIONS');
```

#### 2. Conditionally Render UI Components

```jsx
// React Example
import { useFeatures } from './hooks/useFeatures';

function App() {
  const { isFeatureEnabled, features } = useFeatures();
  
  return (
    <div>
      {isFeatureEnabled('BUDGET_MODULE') && <BudgetDashboard />}
      {isFeatureEnabled('TAX_MODULE') && <TaxCalculator />}
      {isFeatureEnabled('SUBSCRIPTIONS') && <SubscriptionManager />}
      
      {/* Show beta badge */}
      {features.BUDGET_FORECASTING?.betaFeature && 
        <BetaBadge feature="Budget Forecasting" />
      }
    </div>
  );
}
```

#### 3. Hide Navigation Items

```javascript
// Generate navigation based on enabled features
const navigation = [
  {
    name: 'Budget',
    path: '/budget',
    enabled: isFeatureEnabled('BUDGET_MODULE')
  },
  {
    name: 'Subscriptions',
    path: '/subscriptions',
    enabled: isFeatureEnabled('SUBSCRIPTIONS')
  },
  {
    name: 'Tax Planning',
    path: '/tax',
    enabled: isFeatureEnabled('TAX_MODULE')
  }
].filter(item => item.enabled);
```

### For Backend Developers

#### 1. Protect Controller Endpoints

```java
// Protect entire controller
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiresFeature(FeatureFlag.SUBSCRIPTIONS)
public class SubscriptionController {
    // All methods require SUBSCRIPTIONS feature
}

// Or protect specific method
@RestController
@RequestMapping("/api/v1/budget")
public class BudgetController {
    
    @GetMapping("/forecast")
    @RequiresFeature(FeatureFlag.BUDGET_FORECASTING)
    public ResponseEntity<ForecastDTO> getForecast() {
        // Only accessible if BUDGET_FORECASTING is enabled
    }
}
```

#### 2. Check Feature in Service Layer

```java
@Service
public class ReportService {
    
    @Autowired
    private FeatureConfigService featureConfigService;
    
    public Report generateReport() {
        // Check feature programmatically
        if (featureConfigService.isFeatureEnabled(FeatureFlag.REPORTS)) {
            return doGenerateReport();
        }
        throw new FeatureNotEnabledException("Reports feature is not enabled");
    }
}
```

---

## ⚙️ Configuration Methods

### Method 1: application.yml (Deployment-Time)

```yaml
features:
  # Enable/disable at deployment
  subscriptions:
    enabled: true
  
  budget-forecasting:
    enabled: false  # Disabled for this deployment
  
  multi-currency:
    enabled: false  # Not ready yet
```

**Use Case:** Environment-specific configuration (dev vs prod)

### Method 2: Database (Runtime)

```sql
-- Enable a feature
UPDATE feature_config 
SET enabled = TRUE 
WHERE feature_flag = 'BUDGET_FORECASTING';

-- Disable a feature
UPDATE feature_config 
SET enabled = FALSE 
WHERE feature_flag = 'MULTI_CURRENCY';
```

**Use Case:** Dynamic changes without restart

### Method 3: REST API (Admin Interface)

```bash
# Enable feature
curl -X POST http://localhost:8080/api/v1/features/SUBSCRIPTIONS/enable \
  -H "Authorization: Bearer ADMIN_TOKEN"

# Disable feature
curl -X POST http://localhost:8080/api/v1/features/SPLIT_EXPENSES/disable \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

**Use Case:** Admin dashboard, emergency disable

---

## 📡 API Endpoints

### Public Endpoints (No Auth Required)

#### 1. Get All Features
```http
GET /api/v1/features
```

**Response:**
```json
[
  {
    "name": "SUBSCRIPTIONS",
    "displayName": "Subscription Management",
    "description": "Track recurring subscriptions",
    "category": "budget",
    "enabled": true,
    "betaFeature": false,
    "requiresSubscription": false
  },
  {
    "name": "BUDGET_FORECASTING",
    "displayName": "Budget Forecasting",
    "description": "AI-powered budget predictions",
    "category": "budget",
    "enabled": false,
    "betaFeature": true,
    "requiresSubscription": true,
    "minSubscriptionTier": "PREMIUM"
  }
]
```

#### 2. Get Enabled Features (Simple List)
```http
GET /api/v1/features/enabled
```

**Response:**
```json
[
  "BUDGET_MODULE",
  "EXPENSES",
  "INCOME",
  "SUBSCRIPTIONS",
  "TAX_MODULE",
  "PORTFOLIO"
]
```

#### 3. Get Feature Configuration (For UI Initialization)
```http
GET /api/v1/features/config
```

**Response:**
```json
{
  "enabledFeatures": ["BUDGET_MODULE", "TAX_MODULE", ...],
  "featuresByCategory": {
    "budget": ["BUDGET_MODULE", "EXPENSES", "SUBSCRIPTIONS"],
    "tax": ["TAX_MODULE", "CAPITAL_GAINS"],
    "investments": ["PORTFOLIO", "STOCKS"]
  }
}
```

#### 4. Check Specific Feature
```http
GET /api/v1/features/SUBSCRIPTIONS/enabled
```

**Response:**
```json
{
  "enabled": true,
  "feature": true
}
```

#### 5. Get Features by Category
```http
GET /api/v1/features/category/budget
```

#### 6. Get All Categories
```http
GET /api/v1/features/categories
```

**Response:**
```json
["budget", "tax", "investments", "banking", "insurance", "admin"]
```

### Admin Endpoints (Admin Auth Required)

#### 1. Enable Feature
```http
POST /api/v1/features/{featureName}/enable
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/features/BUDGET_FORECASTING/enable \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

**Response:**
```json
{
  "message": "Feature enabled successfully",
  "feature": "BUDGET_FORECASTING",
  "displayName": "Budget Forecasting"
}
```

#### 2. Disable Feature
```http
POST /api/v1/features/{featureName}/disable
```

#### 3. Update Feature Configuration
```http
PUT /api/v1/features/{featureName}
```

**Body:**
```json
{
  "enabled": true,
  "betaFeature": false,
  "requiresSubscription": true,
  "minSubscriptionTier": "PREMIUM",
  "description": "Updated description"
}
```

---

## 🎨 Frontend Integration

### React Hook Example

```javascript
// hooks/useFeatures.js
import { useState, useEffect, createContext, useContext } from 'react';

const FeatureContext = createContext();

export function FeatureProvider({ children }) {
  const [features, setFeatures] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFeatures();
  }, []);

  const fetchFeatures = async () => {
    try {
      const response = await fetch('/api/v1/features');
      const data = await response.json();
      
      // Convert to map for easy lookup
      const featureMap = {};
      data.forEach(feature => {
        featureMap[feature.name] = feature;
      });
      
      setFeatures(featureMap);
      setLoading(false);
    } catch (error) {
      console.error('Failed to fetch features:', error);
      setLoading(false);
    }
  };

  const isFeatureEnabled = (featureName) => {
    return features[featureName]?.enabled || false;
  };

  const isFeatureBeta = (featureName) => {
    return features[featureName]?.betaFeature || false;
  };

  return (
    <FeatureContext.Provider value={{ 
      features, 
      loading, 
      isFeatureEnabled, 
      isFeatureBeta,
      refresh: fetchFeatures 
    }}>
      {children}
    </FeatureContext.Provider>
  );
}

export function useFeatures() {
  return useContext(FeatureContext);
}
```

### Component Usage

```jsx
// components/FeatureGate.jsx
import { useFeatures } from '../hooks/useFeatures';

export function FeatureGate({ feature, children, fallback = null }) {
  const { isFeatureEnabled, loading } = useFeatures();

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!isFeatureEnabled(feature)) {
    return fallback;
  }

  return children;
}

// Usage
<FeatureGate feature="SUBSCRIPTIONS">
  <SubscriptionManager />
</FeatureGate>

<FeatureGate 
  feature="BUDGET_FORECASTING" 
  fallback={<UpgradePrompt />}
>
  <ForecastingDashboard />
</FeatureGate>
```

### Navigation Example

```jsx
// components/Navigation.jsx
import { useFeatures } from '../hooks/useFeatures';

export function Navigation() {
  const { isFeatureEnabled } = useFeatures();

  const navItems = [
    {
      name: 'Dashboard',
      path: '/',
      icon: <DashboardIcon />,
      alwaysShow: true
    },
    {
      name: 'Budget',
      path: '/budget',
      icon: <BudgetIcon />,
      feature: 'BUDGET_MODULE'
    },
    {
      name: 'Subscriptions',
      path: '/subscriptions',
      icon: <SubscriptionIcon />,
      feature: 'SUBSCRIPTIONS'
    },
    {
      name: 'Tax Planning',
      path: '/tax',
      icon: <TaxIcon />,
      feature: 'TAX_MODULE'
    },
    {
      name: 'Investments',
      path: '/portfolio',
      icon: <PortfolioIcon />,
      feature: 'PORTFOLIO'
    }
  ].filter(item => item.alwaysShow || isFeatureEnabled(item.feature));

  return (
    <nav>
      {navItems.map(item => (
        <NavLink key={item.path} to={item.path}>
          {item.icon} {item.name}
        </NavLink>
      ))}
    </nav>
  );
}
```

### Beta Badge Component

```jsx
// components/BetaBadge.jsx
import { useFeatures } from '../hooks/useFeatures';

export function BetaBadge({ feature }) {
  const { isFeatureBeta } = useFeatures();

  if (!isFeatureBeta(feature)) {
    return null;
  }

  return (
    <span className="beta-badge">
      BETA
    </span>
  );
}
```

---

## 🛡️ Admin Management

### Admin Dashboard Example

```jsx
// pages/admin/FeatureManagement.jsx
import { useState, useEffect } from 'react';

export function FeatureManagement() {
  const [features, setFeatures] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFeatures();
  }, []);

  const fetchFeatures = async () => {
    const response = await fetch('/api/v1/features');
    const data = await response.json();
    setFeatures(data);
    setLoading(false);
  };

  const toggleFeature = async (featureName, currentlyEnabled) => {
    const endpoint = currentlyEnabled ? 'disable' : 'enable';
    
    try {
      await fetch(`/api/v1/features/${featureName}/${endpoint}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${getAdminToken()}`
        }
      });
      
      // Refresh features
      fetchFeatures();
      
      showNotification('Feature updated successfully');
    } catch (error) {
      showError('Failed to update feature');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="feature-management">
      <h1>Feature Management</h1>
      
      <table>
        <thead>
          <tr>
            <th>Feature</th>
            <th>Category</th>
            <th>Status</th>
            <th>Beta</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {features.map(feature => (
            <tr key={feature.name}>
              <td>
                <strong>{feature.displayName}</strong>
                <br />
                <small>{feature.description}</small>
              </td>
              <td>{feature.category}</td>
              <td>
                <StatusBadge enabled={feature.enabled} />
              </td>
              <td>
                {feature.betaFeature && <BetaBadge />}
              </td>
              <td>
                <button 
                  onClick={() => toggleFeature(feature.name, feature.enabled)}
                  className={feature.enabled ? 'btn-danger' : 'btn-success'}
                >
                  {feature.enabled ? 'Disable' : 'Enable'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

## ➕ Adding New Features

### Step 1: Add to FeatureFlag Enum

```java
// src/main/java/com/common/features/FeatureFlag.java

public enum FeatureFlag {
    // ... existing features
    
    // Add your new feature
    MY_NEW_FEATURE("My New Feature", "Description of the feature", "category");
}
```

### Step 2: Add to application.yml (Optional)

```yaml
features:
  my-new-feature:
    enabled: false  # Disabled by default
```

### Step 3: Protect Your Controller

```java
@RestController
@RequestMapping("/api/v1/my-feature")
@RequiresFeature(FeatureFlag.MY_NEW_FEATURE)
public class MyFeatureController {
    // Implementation
}
```

### Step 4: Restart Application

The feature will be automatically inserted into the database on startup.

### Step 5: Update UI

```jsx
<FeatureGate feature="MY_NEW_FEATURE">
  <MyNewFeatureComponent />
</FeatureGate>
```

---

## ✅ Best Practices

### 1. **Use Feature Gates for All New Features**
```java
// ✅ Good
@RequiresFeature(FeatureFlag.NEW_FEATURE)
public void newEndpoint() { }

// ❌ Bad - No feature gate
public void newEndpoint() { }
```

### 2. **Check Features Early**
```javascript
// ✅ Good - Check on route entry
if (!isFeatureEnabled('SUBSCRIPTIONS')) {
  return <Navigate to="/dashboard" />;
}

// ❌ Bad - Render then check
<SubscriptionPage /> // Might show briefly before redirect
```

### 3. **Handle Feature Disabled Gracefully**
```jsx
// ✅ Good - Show upgrade prompt
<FeatureGate 
  feature="PREMIUM_REPORTS"
  fallback={<UpgradeToUnlock />}
>
  <PremiumReports />
</FeatureGate>

// ❌ Bad - Show nothing
<FeatureGate feature="PREMIUM_REPORTS">
  <PremiumReports />
</FeatureGate>
```

### 4. **Use Categories for Bulk Operations**
```javascript
// Enable all tax features
const taxFeatures = features.filter(f => f.category === 'tax');
taxFeatures.forEach(feature => enableFeature(feature.name));
```

### 5. **Cache Feature Config in Frontend**
```javascript
// ✅ Good - Cache for session
localStorage.setItem('features', JSON.stringify(features));

// Refresh on critical actions or periodically
if (Date.now() - lastFetch > 5 * 60 * 1000) {
  fetchFeatures(); // Refresh every 5 minutes
}
```

### 6. **Log Feature Access**
```java
@Aspect
@Component
public class FeatureAuditAspect {
    
    @Around("@annotation(requiresFeature)")
    public Object auditFeatureAccess(ProceedingJoinPoint pjp, RequiresFeature requiresFeature) {
        logger.info("Feature accessed: {}", requiresFeature.value().name());
        return pjp.proceed();
    }
}
```

### 7. **Use Beta Features for Testing**
```java
// Mark as beta
FeatureConfig config = new FeatureConfig();
config.setFeatureFlag(FeatureFlag.NEW_FEATURE);
config.setBetaFeature(true);
config.setEnabledForAll(false); // Only specific users

// UI shows beta badge
{isFeatureBeta('NEW_FEATURE') && <BetaBadge />}
```

### 8. **Emergency Disable**
```bash
# Quick disable if production issue
curl -X POST http://localhost:8080/api/v1/features/PROBLEMATIC_FEATURE/disable \
  -H "Authorization: Bearer ADMIN_TOKEN"

# No restart required!
```

---

## 🔍 Troubleshooting

### Issue: Feature not showing in UI

**Check:**
1. Feature is enabled: `GET /api/v1/features/MY_FEATURE/enabled`
2. Frontend has latest config: Clear cache, refresh
3. Feature name matches exactly (case-sensitive)

### Issue: 403 Forbidden on API call

**Reason:** Feature is disabled

**Solution:**
```bash
# Check feature status
curl http://localhost:8080/api/v1/features/FEATURE_NAME/enabled

# Enable if needed
curl -X POST http://localhost:8080/api/v1/features/FEATURE_NAME/enable \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Issue: Feature shows as enabled but endpoints return 403

**Check:**
1. Controller has `@RequiresFeature` annotation
2. Aspect is properly configured
3. Check server logs for feature check

---

## 📊 Feature Statistics

### Current Features: 40+

**By Category:**
- Budget: 8 features
- Tax: 6 features
- Investments: 7 features
- Banking: 5 features
- Insurance: 3 features
- Net Worth: 2 features
- Admin: 4 features
- Future: 5 features (disabled)

**By Status:**
- Production: 35 features (enabled)
- Beta: 5 features (disabled)

---

## 🎉 Benefits Summary

### For Product Team
- ✅ Gradual rollout control
- ✅ A/B testing capability
- ✅ Quick rollback without deployment
- ✅ Beta testing with select users

### For Development Team
- ✅ No code changes to enable/disable
- ✅ Clean separation of concerns
- ✅ Easy to add new features
- ✅ Consistent pattern across codebase

### For Users
- ✅ Only see relevant features
- ✅ Clean, uncluttered UI
- ✅ Smooth feature rollouts
- ✅ Beta access to new features

---

**System Status:** ✅ **FULLY OPERATIONAL**  
**Build Status:** ✅ **BUILD SUCCESSFUL**  
**Documentation:** ✅ **COMPLETE**  

---

*For implementation details, see: [FEATURE_TOGGLE_IMPLEMENTATION.md](FEATURE_TOGGLE_IMPLEMENTATION.md)*
