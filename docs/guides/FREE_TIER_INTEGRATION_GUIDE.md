# 🔧 Integration Guide - Using Tier System in Existing Components

This guide shows how to integrate the tier system into your existing components.

---

## 📱 Frontend Integration Examples

### Example 1: Portfolio Page - Add Stock Button

**File: `frontend/src/pages/Portfolio.jsx`**

```jsx
import React, { useState } from 'react';
import { useTier } from '../contexts/TierContext';
import TierLimitIndicator from '../components/TierLimitIndicator';
import UpgradePrompt from '../components/UpgradePrompt';

function Portfolio() {
  const { checkLimit } = useTier();
  const [stocks, setStocks] = useState([]);
  const [showUpgrade, setShowUpgrade] = useState(false);

  const handleAddStock = async () => {
    // Check tier limit BEFORE showing add form
    const { allowed, limit } = checkLimit('stocks', stocks.length);
    
    if (!allowed) {
      setShowUpgrade(true);
      return;
    }
    
    // Show add stock form
    showAddStockModal();
  };

  return (
    <div className="portfolio-page">
      <h1>My Portfolio</h1>
      
      {/* Show progress towards limit */}
      <TierLimitIndicator feature="stocks" currentCount={stocks.length} />
      
      <button onClick={handleAddStock}>
        Add Stock
      </button>
      
      {/* Stocks list */}
      <div className="stocks-list">
        {stocks.map(stock => <StockCard key={stock.id} stock={stock} />)}
      </div>
      
      {/* Upgrade prompt */}
      <UpgradePrompt 
        show={showUpgrade}
        onClose={() => setShowUpgrade(false)}
        feature="stocks"
        limit={20}
      />
    </div>
  );
}
```

---

### Example 2: Budget Page - Category Creation

**File: `frontend/src/pages/Budget.jsx`**

```jsx
import React, { useState, useEffect } from 'react';
import { useTier } from '../contexts/TierContext';
import TierLimitIndicator from '../components/TierLimitIndicator';
import UpgradePrompt from '../components/UpgradePrompt';
import api from '../api';

function Budget() {
  const { checkLimit } = useTier();
  const [categories, setCategories] = useState([]);
  const [showUpgrade, setShowUpgrade] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    const response = await api.get('/budget/category/custom/1');
    setCategories(response.data);
  };

  const handleCreateCategory = async (categoryData) => {
    // Check limit before API call
    const { allowed, limit } = checkLimit('categories', categories.length);
    
    if (!allowed) {
      setShowUpgrade(true);
      return;
    }

    try {
      const response = await api.post('/budget/category/custom', categoryData);
      setCategories([...categories, response.data]);
    } catch (error) {
      if (error.response?.status === 402) {
        // Backend also caught the limit
        setShowUpgrade(true);
      }
    }
  };

  return (
    <div className="budget-page">
      <h1>Budget Categories</h1>
      
      {/* Show progress */}
      <TierLimitIndicator feature="categories" currentCount={categories.length} />
      
      <button onClick={() => handleCreateCategory(newCategoryData)}>
        Create Category
      </button>
      
      <UpgradePrompt 
        show={showUpgrade}
        onClose={() => setShowUpgrade(false)}
        feature="budget categories"
        limit={5}
      />
    </div>
  );
}
```

---

### Example 3: Insurance Page

**File: `frontend/src/pages/Insurance.jsx`**

```jsx
import React, { useState, useEffect } from 'react';
import { useTier } from '../contexts/TierContext';
import TierLimitIndicator from '../components/TierLimitIndicator';
import UpgradePrompt from '../components/UpgradePrompt';

function Insurance() {
  const { checkLimit } = useTier();
  const [policies, setPolicies] = useState([]);
  const [showUpgrade, setShowUpgrade] = useState(false);

  const handleAddPolicy = async (policyData) => {
    const { allowed } = checkLimit('policies', policies.length);
    
    if (!allowed) {
      setShowUpgrade(true);
      return;
    }

    try {
      const response = await api.post('/api/v1/insurance/create', policyData);
      setPolicies([...policies, response.data]);
    } catch (error) {
      if (error.response?.status === 402) {
        setShowUpgrade(true);
      }
    }
  };

  return (
    <div className="insurance-page">
      <h1>Insurance Policies</h1>
      
      <TierLimitIndicator feature="policies" currentCount={policies.length} />
      
      <button onClick={() => handleAddPolicy(newPolicy)}>
        Add Policy
      </button>
      
      <UpgradePrompt 
        show={showUpgrade}
        onClose={() => setShowUpgrade(false)}
        feature="insurance policies"
        limit={2}
      />
    </div>
  );
}
```

---

### Example 4: Navigation Bar - Show Tier Badge

**File: `frontend/src/components/Navbar.jsx`**

```jsx
import React from 'react';
import TierBadge from './TierBadge';
import { useTier } from '../contexts/TierContext';

function Navbar() {
  const { isFree } = useTier();

  return (
    <nav className="navbar">
      <div className="nav-left">
        <Logo />
        <NavLinks />
      </div>
      
      <div className="nav-right">
        {/* Show tier badge */}
        <TierBadge showDetails={false} />
        
        {/* Show upgrade button for free users */}
        {isFree && (
          <button className="upgrade-nav-btn">
            Upgrade to Premium
          </button>
        )}
        
        <UserMenu />
      </div>
    </nav>
  );
}
```

---

## 🔧 Backend Integration Examples

### Example 1: Adding Tier Check to New Feature

**File: `YourNewFeatureService.java`**

```java
@Service
public class YourNewFeatureService {
    
    @Autowired
    private SubscriptionTierService subscriptionTierService;
    
    @Autowired
    private YourFeatureRepository repository;
    
    public Feature createFeature(Long userId, Feature feature) {
        // 1. Count current items
        int currentCount = repository.findByUserId(userId).size();
        
        // 2. Check tier limit
        subscriptionTierService.checkFeatureLimit(userId, currentCount);
        // This will throw TierLimitExceededException if limit exceeded
        
        // 3. Proceed with creation
        return repository.save(feature);
    }
}
```

### Example 2: Custom Limit Check

**File: `CustomFeatureService.java`**

```java
@Service
public class CustomFeatureService {
    
    @Autowired
    private SubscriptionTierService subscriptionTierService;
    
    public void performLimitedAction(Long userId) {
        // Get user's tier
        SubscriptionTier tier = subscriptionTierService.getUserTier(userId);
        
        // Check tier and apply logic
        if (tier == SubscriptionTier.FREE) {
            // Apply FREE tier restrictions
            applyBasicFeatures();
        } else {
            // PREMIUM tier gets all features
            applyAdvancedFeatures();
        }
    }
}
```

### Example 3: Adding New Limit Type

**Step 1: Update TierLimits.java**
```java
public class TierLimits {
    // Add new limit constant
    public static final int FREE_MAX_REPORTS = 5;
    public static final int PREMIUM_MAX_REPORTS = Integer.MAX_VALUE;
    
    // Add getter method
    public static int getMaxReports(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> FREE_MAX_REPORTS;
            case PREMIUM, ENTERPRISE -> PREMIUM_MAX_REPORTS;
        };
    }
}
```

**Step 2: Update SubscriptionTierService.java**
```java
@Service
public class SubscriptionTierService {
    
    // Add new check method
    public void checkReportLimit(Long userId, int currentCount) {
        SubscriptionTier tier = getUserTier(userId);
        int maxAllowed = TierLimits.getMaxReports(tier);
        
        if (currentCount >= maxAllowed) {
            throw new TierLimitExceededException(tier, "reports", maxAllowed);
        }
    }
}
```

**Step 3: Use in your service**
```java
@Service
public class ReportService {
    
    @Autowired
    private SubscriptionTierService subscriptionTierService;
    
    public Report generateReport(Long userId) {
        int currentReports = countUserReports(userId);
        subscriptionTierService.checkReportLimit(userId, currentReports);
        
        return createReport();
    }
}
```

---

## 🎨 Styling Tips

### Custom Tier Badge Colors

**File: `frontend/src/components/TierBadge.css`**

```css
/* Add custom colors for your brand */
.tier-badge.free {
  background: linear-gradient(135deg, #your-color-1 0%, #your-color-2 100%);
}

.tier-badge.premium {
  background: linear-gradient(135deg, #gold-1 0%, #gold-2 100%);
}
```

### Upgrade Modal Customization

**File: `frontend/src/components/UpgradePrompt.css`**

```css
/* Change price color */
.price {
  color: #your-brand-color;
}

/* Customize button */
.upgrade-button {
  background: linear-gradient(135deg, #your-color-1 0%, #your-color-2 100%);
}
```

---

## 🧪 Testing Examples

### Frontend Test with Jest

```jsx
import { render, screen, fireEvent } from '@testing-library/react';
import { TierProvider } from '../contexts/TierContext';
import Portfolio from '../pages/Portfolio';

test('shows upgrade prompt when limit reached', () => {
  render(
    <TierProvider>
      <Portfolio />
    </TierProvider>
  );
  
  // Add 20 stocks (limit)
  for (let i = 0; i < 20; i++) {
    fireEvent.click(screen.getByText('Add Stock'));
  }
  
  // Try to add 21st stock
  fireEvent.click(screen.getByText('Add Stock'));
  
  // Should show upgrade prompt
  expect(screen.getByText('Upgrade to Premium')).toBeInTheDocument();
});
```

### Backend Test with JUnit

```java
@SpringBootTest
class PortfolioServiceTest {
    
    @Autowired
    private PortfolioWriteService portfolioService;
    
    @Test
    void shouldThrowExceptionWhenStockLimitExceeded() {
        // Create FREE user with 20 stocks
        Long userId = createFreeUserWith20Stocks();
        
        // Try to add 21st stock
        Portfolio newStock = createTestStock();
        
        // Should throw TierLimitExceededException
        assertThrows(TierLimitExceededException.class, () -> {
            portfolioService.addPortfolio(newStock);
        });
    }
    
    @Test
    void shouldAllowPremiumUserUnlimitedStocks() {
        // Create PREMIUM user with 100 stocks
        Long userId = createPremiumUserWith100Stocks();
        
        // Should be able to add more
        Portfolio newStock = createTestStock();
        assertDoesNotThrow(() -> {
            portfolioService.addPortfolio(newStock);
        });
    }
}
```

---

## 🔍 Debugging Tips

### Check User's Current Tier

```bash
# Via API
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/subscription/my-tier

# Via Database
SELECT id, email, subscription_tier FROM users WHERE id = 1;
```

### Monitor Tier Limit Exceptions

```bash
# Watch logs for tier exceptions
tail -f logs/application.log | grep "TierLimitExceededException"
```

### Frontend Debug

```jsx
function DebugTier() {
  const tierContext = useTier();
  
  return (
    <pre>
      {JSON.stringify(tierContext, null, 2)}
    </pre>
  );
}
```

---

## ✅ Integration Checklist

Frontend:
- [ ] Added TierProvider to App.jsx
- [ ] Import useTier hook in components
- [ ] Add TierLimitIndicator to feature pages
- [ ] Add checkLimit before create actions
- [ ] Add UpgradePrompt components
- [ ] Add TierBadge to navbar/header
- [ ] Handle 402 errors from API

Backend:
- [ ] Inject SubscriptionTierService
- [ ] Add tier checks before create operations
- [ ] Test limits with unit tests
- [ ] Verify exception handling
- [ ] Check Swagger docs updated

---

## 🚀 Ready to Integrate!

You now have everything needed to integrate tier-based limitations into any feature in your application!

**Remember:**
1. Always check limits on **both** frontend and backend
2. Show progress indicators to users
3. Make upgrade prompts beautiful and helpful
4. Test thoroughly with different tier levels
