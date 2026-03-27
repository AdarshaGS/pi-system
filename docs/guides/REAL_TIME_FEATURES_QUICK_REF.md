# Real-Time Features - Quick Reference

## 🚀 Quick Start

### Backend Services

```java
// Create Alert Rule
@Autowired
private AlertRuleService alertRuleService;

AlertRuleRequest request = new AlertRuleRequest();
request.setUserId(1L);
request.setType(AlertType.STOCK_PRICE);
request.setSymbol("RELIANCE");
request.setTargetPrice(new BigDecimal("2500.00"));
request.setPriceCondition("ABOVE");
request.setChannel(AlertChannel.EMAIL);
request.setEnabled(true);

AlertRule rule = alertRuleService.createAlertRule(request);
```

```java
// Send Notification
@Autowired
private NotificationService notificationService;

notificationService.sendNotification(
    userId,
    "Stock Alert",
    "RELIANCE crossed ₹2500",
    NotificationType.ALERT,
    AlertChannel.IN_APP,
    Map.of("symbol", "RELIANCE", "price", "2510.50"),
    alertRuleId
);
```

### Frontend Usage

```javascript
// Stock Price WebSocket
import stockPriceWebSocket from './websocket/StockPriceWebSocket';

// Connect
await stockPriceWebSocket.connect();

// Subscribe
stockPriceWebSocket.subscribeToSymbol('RELIANCE', (data) => {
  console.log(`${data.symbol}: ₹${data.price} (${data.changePercent}%)`);
});

// Cleanup
useEffect(() => {
  return () => stockPriceWebSocket.disconnect();
}, []);
```

```javascript
// Notification WebSocket
import notificationWebSocket from './websocket/NotificationWebSocket';

// Connect
await notificationWebSocket.connect(userId);

// Request permission
await notificationWebSocket.requestNotificationPermission();

// Auto-subscribed to /topic/notifications/{userId}
```

---

## 📡 API Endpoints

### Alerts
```bash
# Create Alert
POST /api/v1/alerts/rules
{
  "userId": 1,
  "type": "STOCK_PRICE",
  "symbol": "RELIANCE",
  "targetPrice": 2500.00,
  "priceCondition": "ABOVE",
  "channel": "EMAIL",
  "enabled": true
}

# List User Alerts
GET /api/v1/alerts/rules/user/{userId}

# Update Alert
PUT /api/v1/alerts/rules/{id}

# Toggle Alert
PATCH /api/v1/alerts/rules/{id}/toggle

# Delete Alert
DELETE /api/v1/alerts/rules/{id}?userId=1
```

### Notifications
```bash
# Get Notifications
GET /api/v1/notifications/user/{userId}

# Get Unread Count
GET /api/v1/notifications/user/{userId}/unread-count

# Mark as Read
PATCH /api/v1/notifications/{id}/read

# Mark All as Read
PATCH /api/v1/notifications/user/{userId}/read-all

# Delete Notification
DELETE /api/v1/notifications/{id}?userId=1
```

---

## ⏰ Scheduled Jobs

| Job | Cron | Frequency | Purpose |
|-----|------|-----------|---------|
| Stock Price Alerts | `0 */5 * * * *` | Every 5 min | Check price thresholds |
| EMI Due Alerts | `0 0 8 * * *` | Daily 8:00 AM | Check upcoming EMIs |
| Policy Expiry | `0 0 9 * * *` | Daily 9:00 AM | Check policy expiry |
| Premium Due | `0 30 8 * * *` | Daily 8:30 AM | Check premium payments |
| Tax Deadline | `0 0 10 * * *` | Daily 10:00 AM | Check ITR deadline |

---

## 🎯 Alert Types

| Type | Code | Configuration |
|------|------|---------------|
| Stock Price | `STOCK_PRICE` | symbol, targetPrice, priceCondition |
| Stock Volume | `STOCK_VOLUME` | symbol, volumeThreshold |
| EMI Due | `EMI_DUE` | daysBeforeDue (default: 3) |
| Policy Expiry | `POLICY_EXPIRY` | daysBeforeDue (default: 30) |
| Premium Due | `PREMIUM_DUE` | daysBeforeDue (default: 7) |
| Tax Deadline | `TAX_DEADLINE` | daysBeforeDue (default: 30) |
| Portfolio Drift | `PORTFOLIO_DRIFT` | percentageChange |
| Negative Returns | `NEGATIVE_RETURNS` | percentageChange |
| Sector Concentration | `SECTOR_CONCENTRATION` | percentageChange |

---

## 📬 Notification Channels

| Channel | Code | Status | Use Case |
|---------|------|--------|----------|
| In-App | `IN_APP` | ✅ Active | Real-time WebSocket notifications |
| Email | `EMAIL` | ✅ Active | Email delivery via SMTP |
| SMS | `SMS` | ⏳ Future | SMS via Twilio/AWS SNS |
| Push | `PUSH` | ⏳ Future | Browser push via FCM |

---

## 🔌 WebSocket Topics

| Topic | Description | Broadcast Frequency |
|-------|-------------|---------------------|
| `/topic/stock-prices/{symbol}` | Live stock prices | 15 seconds (market hours) |
| `/topic/portfolio-updates` | Portfolio value updates | 30 seconds (market hours) |
| `/topic/notifications/{userId}` | User notifications | Real-time |

---

## ⚙️ Environment Variables

```properties
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@pi-system.com
```

### Gmail Setup
1. Enable 2FA
2. Generate App Password: Google Account → Security → 2-Step Verification → App passwords
3. Use app password in `MAIL_PASSWORD`

---

## 🧪 Testing Commands

```bash
# Test Alert Creation
curl -X POST http://localhost:8080/api/v1/alerts/rules \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "STOCK_PRICE",
    "symbol": "RELIANCE",
    "targetPrice": 2500.00,
    "priceCondition": "ABOVE",
    "channel": "IN_APP",
    "enabled": true
  }'

# Test Notification Listing
curl http://localhost:8080/api/v1/notifications/user/1

# Test Unread Count
curl http://localhost:8080/api/v1/notifications/user/1/unread-count
```

---

## 🎨 Frontend Components

### Stock Price Widget
```jsx
function StockPrice({ symbol }) {
  const [price, setPrice] = useState(null);

  useEffect(() => {
    const connect = async () => {
      await stockPriceWebSocket.connect();
      stockPriceWebSocket.subscribeToSymbol(symbol, setPrice);
    };
    connect();
  }, [symbol]);

  return <div>₹{price?.price}</div>;
}
```

### Notification Bell
```jsx
function NotificationBell({ userId }) {
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    const fetchCount = async () => {
      const res = await fetch(`/api/v1/notifications/user/${userId}/unread-count`);
      const data = await res.json();
      setUnreadCount(data.unreadCount);
    };
    
    notificationWebSocket.connect(userId);
    notificationWebSocket.subscribeToNotifications(userId, fetchCount);
    fetchCount();
  }, [userId]);

  return (
    <div>
      <i className="fa fa-bell"></i>
      {unreadCount > 0 && <span>{unreadCount}</span>}
    </div>
  );
}
```

---

## 🔧 Common Patterns

### Create Price Alert
```java
AlertRuleRequest request = AlertRuleRequest.builder()
    .userId(userId)
    .type(AlertType.STOCK_PRICE)
    .symbol("RELIANCE")
    .targetPrice(new BigDecimal("2500.00"))
    .priceCondition("ABOVE")
    .channel(AlertChannel.EMAIL)
    .enabled(true)
    .description("Alert when RELIANCE crosses 2500")
    .build();

alertRuleService.createAlertRule(request);
```

### Send Multi-Channel Notification
```java
// Sends both in-app (WebSocket) and email
notificationService.sendNotification(
    userId,
    "Payment Due",
    "Your EMI of ₹15,000 is due in 3 days",
    NotificationType.REMINDER,
    AlertChannel.EMAIL, // Also triggers in-app
    Map.of("loanId", "123", "amount", "15000"),
    alertRuleId
);
```

### Process Alert Rule
```java
@Scheduled(cron = "0 */5 * * * *")
public void processAlerts() {
    List<AlertRule> rules = alertRuleRepository
        .findByTypeAndEnabled(AlertType.STOCK_PRICE, true);
    
    for (AlertRule rule : rules) {
        BigDecimal currentPrice = stockService.getLatestPrice(rule.getSymbol());
        
        if (shouldTrigger(currentPrice, rule)) {
            notificationService.sendNotification(
                rule.getUserId(),
                "Price Alert",
                formatMessage(rule, currentPrice),
                NotificationType.ALERT,
                rule.getChannel(),
                Map.of("symbol", rule.getSymbol()),
                rule.getId()
            );
            
            alertRuleService.updateLastTriggered(rule.getId());
        }
    }
}
```

---

## 📊 Database Queries

```java
// Find active alerts for user
List<AlertRule> alerts = alertRuleRepository
    .findByUserIdAndEnabled(userId, true);

// Find unread notifications
List<UserNotification> notifications = userNotificationRepository
    .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);

// Count unread
long count = userNotificationRepository
    .countByUserIdAndIsRead(userId, false);

// Recent notifications (last 7 days)
LocalDateTime since = LocalDateTime.now().minusDays(7);
List<UserNotification> recent = userNotificationRepository
    .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);
```

---

## 🚨 Troubleshooting

### WebSocket Not Connecting
```
✓ Check WebSocketConfig is present
✓ Verify @EnableWebSocketMessageBroker
✓ Check CORS settings
✓ Confirm server port (default: 8080)
```

### Emails Not Sending
```
✓ Verify MAIL_USERNAME and MAIL_PASSWORD
✓ Use App Password (not account password)
✓ Check SMTP settings (host, port, auth)
✓ Review application logs for errors
```

### Scheduled Jobs Not Running
```
✓ Verify @EnableScheduling annotation
✓ Check cron expressions are valid
✓ Ensure AlertProcessorService is @Service
✓ Review application startup logs
```

### Stock Prices Not Updating
```
✓ Check market hours (9:15 AM - 3:30 PM IST)
✓ Verify StockService returns valid prices
✓ Check WebSocket broadcast logs
✓ Confirm frontend is subscribed
```

---

## 📦 Dependencies Required

```gradle
// WebSocket
implementation 'org.springframework.boot:spring-boot-starter-websocket'

// Email
implementation 'org.springframework.boot:spring-boot-starter-mail'

// Frontend
npm install sockjs-client @stomp/stompjs
```

---

## 📚 Related Documentation

- [REAL_TIME_FEATURES_IMPLEMENTATION.md](REAL_TIME_FEATURES_IMPLEMENTATION.md) - Complete guide
- [REAL_TIME_FEATURES_SUMMARY.md](REAL_TIME_FEATURES_SUMMARY.md) - Implementation summary
- [WEEK_1_ENHANCEMENTS.md](planning/WEEK_1_ENHANCEMENTS.md) - Week 1 plan

---

**Last Updated**: February 5, 2026  
**Version**: 1.0.0  
**Status**: Production Ready ✅
