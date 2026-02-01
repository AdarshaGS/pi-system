# 🚀 Subscription Management - Quick Reference

## 📌 Quick Start (5 Minutes)

### 1. Create Your First Subscription
```bash
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Netflix",
    "amount": 649,
    "billingCycle": "MONTHLY",
    "category": "ENTERTAINMENT",
    "startDate": "2026-01-01",
    "autoRenewal": true
  }'
```

### 2. View All Subscriptions
```bash
curl http://localhost:8080/api/v1/subscriptions \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Get Analytics Dashboard
```bash
curl http://localhost:8080/api/v1/subscriptions/analytics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 Common Use Cases

### 💰 "How much am I spending on subscriptions?"
```http
GET /api/v1/subscriptions/analytics
```
→ Returns `totalMonthlyCost` and `totalAnnualCost`

### 🔍 "Which subscriptions am I not using?"
```http
GET /api/v1/subscriptions/unused
```
→ Returns subscriptions not used in 30+ days

### 📅 "What's renewing this month?"
```http
GET /api/v1/subscriptions/expiring-soon?days=30
```
→ Returns subscriptions expiring in next 30 days

### ❌ "Cancel a subscription"
```http
POST /api/v1/subscriptions/{id}/cancel
```
→ Marks subscription as CANCELLED

### 📊 "Which category costs most?"
```http
GET /api/v1/subscriptions/analytics
```
→ Check `spendingByCategory` and `topCategory`

---

## 📋 All API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/subscriptions` | Create subscription |
| PUT | `/api/v1/subscriptions/{id}` | Update subscription |
| GET | `/api/v1/subscriptions/{id}` | Get one subscription |
| GET | `/api/v1/subscriptions` | List all (paginated) |
| GET | `/api/v1/subscriptions/active` | Active subscriptions |
| GET | `/api/v1/subscriptions/unused` | Unused subscriptions |
| GET | `/api/v1/subscriptions/expiring-soon?days=30` | Expiring soon |
| GET | `/api/v1/subscriptions/analytics` | **Full analytics** |
| GET | `/api/v1/subscriptions/status/{status}` | Filter by status |
| GET | `/api/v1/subscriptions/category/{category}` | Filter by category |
| GET | `/api/v1/subscriptions/search?serviceName=X` | Search |
| POST | `/api/v1/subscriptions/{id}/cancel` | Cancel |
| POST | `/api/v1/subscriptions/{id}/pause` | Pause |
| POST | `/api/v1/subscriptions/{id}/resume` | Resume |
| POST | `/api/v1/subscriptions/{id}/renew` | Renew |
| DELETE | `/api/v1/subscriptions/{id}` | Delete |
| GET | `/api/v1/subscriptions/categories` | List categories |
| GET | `/api/v1/subscriptions/billing-cycles` | List billing cycles |
| GET | `/api/v1/subscriptions/statuses` | List statuses |

---

## 🎨 Categories

- **ENTERTAINMENT** - Netflix, Spotify, Disney+
- **SOFTWARE** - Adobe, Microsoft 365, GitHub
- **CLOUD_STORAGE** - Google Drive, Dropbox
- **NEWS_MEDIA** - NYTimes, Medium
- **FITNESS** - Gym, Cult.fit, Peloton
- **EDUCATION** - Coursera, Udemy
- **GAMING** - PlayStation, Xbox
- **UTILITIES** - Internet, Mobile
- **FOOD_DELIVERY** - Zomato, Swiggy
- **SHOPPING** - Amazon Prime
- **OTHER** - Miscellaneous

---

## 🔄 Billing Cycles

- **WEEKLY** - Every week
- **MONTHLY** - Every month (most common)
- **QUARTERLY** - Every 3 months
- **HALF_YEARLY** - Every 6 months
- **YEARLY** - Once per year

---

## 📊 Analytics Response Structure

```json
{
  "totalSubscriptions": 8,
  "activeSubscriptions": 6,
  "cancelledSubscriptions": 2,
  "unusedSubscriptions": 1,
  "totalMonthlyCost": 3500.00,
  "totalAnnualCost": 42000.00,
  "potentialSavings": 499.00,
  "topCategory": "Entertainment",
  "mostExpensiveSubscription": { ... },
  "spendingByCategory": {
    "ENTERTAINMENT": {
      "count": 3,
      "monthlySpending": 1800.00,
      "percentageOfTotal": 51.43
    }
  },
  "upcomingRenewals": [ ... ],
  "unusedSubscriptionsList": [ ... ],
  "subscriptionsByBillingCycle": {
    "MONTHLY": 5,
    "YEARLY": 2
  }
}
```

---

## ⏰ Scheduled Jobs

| Job | Schedule | Purpose |
|-----|----------|---------|
| Renewal Reminders | Daily 8:00 AM | Remind before renewals |
| Unused Check | Mon 9:00 AM | Find unused subscriptions |
| Expire Check | Daily 1:00 AM | Mark expired subscriptions |

---

## 🔧 Request Examples

### Create Subscription
```json
POST /api/v1/subscriptions

{
  "serviceName": "Spotify Premium",
  "description": "Music streaming",
  "amount": 119.00,
  "billingCycle": "MONTHLY",
  "category": "ENTERTAINMENT",
  "startDate": "2026-01-15",
  "autoRenewal": true,
  "paymentMethod": "Credit Card",
  "reminderDaysBefore": 3,
  "websiteUrl": "https://spotify.com",
  "notes": "Individual plan"
}
```

### Update Subscription
```json
PUT /api/v1/subscriptions/5

{
  "amount": 149.00,
  "notes": "Upgraded to Duo plan"
}
```

### Update Last Used Date
```http
PUT /api/v1/subscriptions/5/last-used?lastUsedDate=2026-02-01
```

---

## 💡 Pro Tips

1. **Set reminderDaysBefore = 7** for expensive yearly subscriptions
2. **Update lastUsedDate** regularly to track usage
3. **Check unused subscriptions** monthly to save money
4. **Use analytics** to optimize subscription spending
5. **Set autoRenewal = false** for trial subscriptions
6. **Add notes** to remember sharing details

---

## 🏷️ Status Values

- **ACTIVE** - Currently subscribed
- **CANCELLED** - Subscription cancelled
- **PAUSED** - Temporarily paused
- **EXPIRED** - Expired without renewal

---

## 📱 Integration Examples

### Frontend React Component
```javascript
// Fetch analytics
const response = await fetch('/api/v1/subscriptions/analytics', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const analytics = await response.json();

console.log(`Monthly spending: ₹${analytics.totalMonthlyCost}`);
console.log(`Can save: ₹${analytics.potentialSavings}`);
```

### Mobile App (Flutter)
```dart
// Get unused subscriptions
final response = await http.get(
  Uri.parse('$baseUrl/api/v1/subscriptions/unused'),
  headers: {'Authorization': 'Bearer $token'},
);
final List unused = jsonDecode(response.body);
print('Found ${unused.length} unused subscriptions');
```

---

## 🔐 Authentication

All endpoints require JWT token:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🎯 Performance

- **Response Time:** < 200ms for most endpoints
- **Analytics:** < 500ms (cached recommended)
- **Pagination:** Default 20 items/page
- **Max Page Size:** 100 items

---

## 📞 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Check JWT token validity |
| 404 Not Found | Verify subscription ID exists |
| 400 Bad Request | Check required fields (serviceName, amount, billingCycle, category, startDate) |
| 500 Server Error | Check logs for details |

---

## 🚀 Next Steps

1. ✅ **Subscription Management** (You are here!)
2. 🔜 **Budget Templates** (Next recommended)
3. 🔜 **Financial Goals Tracking**
4. 🔜 **Receipt Management**

---

**For detailed documentation, see:** [SUBSCRIPTION_MANAGEMENT.md](SUBSCRIPTION_MANAGEMENT.md)
