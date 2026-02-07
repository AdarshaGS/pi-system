# 🚀 FREE Tier Quick Start Guide

## 🎯 What You Built

A complete FREE tier system that lets users experience your financial platform:

### ✅ Features Available in FREE Tier:

1. **Portfolio Tracking** - Up to 20 stocks
2. **Budget Management** - 5 custom categories  
3. **Insurance** - Store 2 policies
4. **UPI Payments** - Always Free!
5. **Loan Calculator** - Always Free!

---

## 📦 What Was Created

### Backend (9 files)
- Subscription tier enums and DTOs
- Service layer with limit checks
- REST API endpoints
- Exception handling
- Database migration

### Frontend (7 files)
- React context for tier state
- UI components (Badge, Indicator, Upgrade Modal)
- Styled with modern CSS

---

## 🔧 Setup Instructions

### 1. Run Database Migration

The migration will add `subscription_tier` column to users table:

```bash
# Migration runs automatically on startup
# Or manually run:
./gradlew flywayMigrate
```

### 2. Start Backend

```bash
cd /Users/adarshgs/Documents/Stocks/App/pi-system
./gradlew bootRun
```

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 📡 Test the APIs

### Get Your Tier Info
```bash
curl -X GET http://localhost:8080/api/v1/subscription/my-tier \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get All Tiers (Comparison)
```bash
curl -X GET http://localhost:8080/api/v1/subscription/tiers
```

### Get Free Features
```bash
curl -X GET http://localhost:8080/api/v1/subscription/free-features
```

---

## 🎨 Using in Frontend

### 1. The TierProvider is already added to App.jsx

```jsx
import { TierProvider } from './contexts/TierContext';

<TierProvider>
  <App />
</TierProvider>
```

### 2. Use in Any Component

```jsx
import { useTier } from '../contexts/TierContext';
import TierBadge from '../components/TierBadge';
import TierLimitIndicator from '../components/TierLimitIndicator';
import UpgradePrompt from '../components/UpgradePrompt';

function MyComponent() {
  const { tier, limits, checkLimit } = useTier();
  const [showUpgrade, setShowUpgrade] = useState(false);
  
  // Show tier badge
  return (
    <div>
      <TierBadge showDetails={true} />
      
      {/* Show limit progress */}
      <TierLimitIndicator feature="stocks" currentCount={15} />
      
      {/* Upgrade modal */}
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

### 3. Check Limits Before Actions

```jsx
function AddStockButton() {
  const { checkLimit } = useTier();
  const [showUpgrade, setShowUpgrade] = useState(false);
  
  const handleAdd = () => {
    const { allowed, limit } = checkLimit('stocks', currentCount);
    
    if (!allowed) {
      setShowUpgrade(true);
      return;
    }
    
    // Proceed with adding stock
    addStock();
  };
  
  return <button onClick={handleAdd}>Add Stock</button>;
}
```

---

## 🧪 Testing the Limits

### Test FREE User Limits:

1. **Add 20 stocks** ✅ Should work
2. **Try to add 21st stock** ❌ Should show error:
   ```json
   {
     "status": 402,
     "error": "Subscription Limit Exceeded",
     "message": "Your Free plan allows only 20 stocks. Please upgrade to access more."
   }
   ```

3. **Create 5 budget categories** ✅ Should work
4. **Try 6th category** ❌ Should trigger upgrade prompt

5. **Add 2 insurance policies** ✅ Should work
6. **Try 3rd policy** ❌ Should show limit error

7. **UPI payments** ✅ Unlimited for all users
8. **Loan calculator** ✅ Unlimited for all users

---

## 🎯 Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/subscription/my-tier` | GET | Get current user's tier |
| `/api/v1/subscription/tiers` | GET | Compare all tiers |
| `/api/v1/subscription/free-features` | GET | List FREE features |
| `/api/v1/subscription/usage/{userId}` | GET | Get usage counts |

---

## 💡 How Limits Work

### Backend Enforcement

When a user tries to exceed their limit:

```java
// In PortfolioWriteServiceImpl
int currentCount = portfolioRepository.findByUserId(userId).size();
subscriptionTierService.checkStockLimit(userId, currentCount);
// Throws TierLimitExceededException if limit exceeded
```

### Frontend Display

```jsx
// Automatically shows progress
<TierLimitIndicator feature="stocks" currentCount={18} />
// Shows: 18/20 stocks with yellow warning at 80%
```

---

## 📊 Tier Comparison

| Feature | FREE | PREMIUM |
|---------|------|---------|
| Stocks | 20 | ∞ Unlimited |
| Categories | 5 | ∞ Unlimited |
| Policies | 2 | ∞ Unlimited |
| UPI | ✅ Free | ✅ Free |
| Loan Calc | ✅ Free | ✅ Free |

---

## 🔍 Troubleshooting

### Issue: Limits not enforced
**Solution:** Make sure SubscriptionTierService is injected:
```java
@Autowired
private SubscriptionTierService subscriptionTierService;
```

### Issue: Frontend not showing tier
**Solution:** Check TierProvider is wrapping the app in App.jsx

### Issue: Migration not running
**Solution:** Check Flyway is configured and restart the app

---

## 🎨 Customization

### Change Tier Limits

Edit `TierLimits.java`:
```java
public static final int FREE_MAX_STOCKS = 20;  // Change here
public static final int FREE_MAX_CATEGORIES = 5;
public static final int FREE_MAX_POLICIES = 2;
```

### Customize Upgrade Modal

Edit `UpgradePrompt.jsx` to change:
- Premium price
- Feature lists
- Button actions
- Styling

### Change Error Messages

Edit `TierLimitExceededException.java`:
```java
super(String.format("Your %s plan allows only %d %s. Upgrade for more!", 
    currentTier.getDisplayName(), limit, feature));
```

---

## 📚 Further Reading

- See [FREE_TIER_IMPLEMENTATION_COMPLETE.md](FREE_TIER_IMPLEMENTATION_COMPLETE.md) for full documentation
- Check Swagger UI at http://localhost:8080/swagger-ui.html for API docs

---

## ✅ Success!

You now have a complete FREE tier system that:
- ✅ Enforces limits at the backend
- ✅ Shows beautiful UI indicators  
- ✅ Encourages upgrades
- ✅ Lets users experience the platform

**Users can now explore your financial ecosystem with meaningful limitations!** 🎉
