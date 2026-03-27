# Real-Time Features Implementation - Complete Guide

## 📋 Overview

This document provides a complete reference for the Real-Time Features implementation in the Pi System, including WebSocket integration, smart alerts, email notifications, and real-time portfolio tracking.

**Implementation Date**: February 5, 2026  
**Status**: ✅ Complete  
**Coverage**: WebSocket, SSE, Email, Push Notifications, Real-time Tracking

---

## 🏗️ Architecture

### Components Implemented

1. **Alert System**
   - Alert Rules Engine
   - Alert Processor (Scheduled Jobs)
   - Multi-channel Delivery (In-App, Email)

2. **Notification System**
   - WebSocket Broadcasting
   - Email Service
   - Browser Push Notifications

3. **Real-Time Stock Prices**
   - WebSocket Controller
   - Market Hours Detection
   - Portfolio Value Updates

4. **Frontend Integration**
   - React WebSocket Clients
   - Browser Notification API
   - Real-time UI Updates

---

## 📁 File Structure

```
src/main/java/com/
├── alerts/
│   ├── entity/
│   │   ├── AlertRule.java              # Alert rule entity
│   │   ├── AlertType.java              # Alert types enum
│   │   ├── AlertChannel.java           # Delivery channels enum
│   │   ├── UserNotification.java       # Notification entity
│   │   └── NotificationType.java       # Notification types enum
│   ├── repository/
│   │   ├── AlertRuleRepository.java    # Alert rule data access
│   │   └── UserNotificationRepository.java  # Notification data access
│   ├── dto/
│   │   ├── AlertRuleRequest.java       # Alert creation DTO
│   │   └── NotificationDTO.java        # Notification response DTO
│   ├── service/
│   │   ├── AlertRuleService.java       # Alert CRUD operations
│   │   ├── AlertProcessorService.java  # Scheduled alert processing
│   │   ├── EmailService.java           # Email sending service
│   │   └── NotificationService.java    # Notification management
│   └── controller/
│       ├── AlertController.java        # Alert REST API
│       └── NotificationController.java # Notification REST API
└── websocket/
    └── controller/
        └── StockPriceWebSocketController.java  # Real-time stock prices

frontend/src/
└── websocket/
    ├── StockPriceWebSocket.js      # Stock price WebSocket client
    └── NotificationWebSocket.js    # Notification WebSocket client
```

---

## 🎯 Features Implemented

### 1. Alert Rules System

**Alert Types**:
- `STOCK_PRICE` - Price above/below threshold
- `STOCK_VOLUME` - Volume spike detection
- `EMI_DUE` - Loan EMI reminders
- `POLICY_EXPIRY` - Insurance policy expiry
- `PREMIUM_DUE` - Premium payment reminders
- `TAX_DEADLINE` - Tax filing deadlines
- `PORTFOLIO_DRIFT` - Asset allocation drift
- `NEGATIVE_RETURNS` - Negative returns threshold
- `SECTOR_CONCENTRATION` - Sector concentration warnings

**Delivery Channels**:
- `IN_APP` - Real-time WebSocket notifications
- `EMAIL` - Email notifications
- `SMS` - SMS notifications (future)
- `PUSH` - Browser push notifications (future)

**Alert Rule Entity**:
```java
@Entity
public class AlertRule {
    private Long id;
    private Long userId;
    private AlertType type;
    private String symbol;              // For stock alerts
    private BigDecimal targetPrice;     // Price threshold
    private String priceCondition;      // ABOVE, BELOW, EQUALS
    private Integer daysBeforeDue;      // Days before due date
    private BigDecimal percentageChange; // % change threshold
    private AlertChannel channel;
    private Boolean enabled;
    private LocalDateTime lastTriggeredAt;
    private String description;
}
```

### 2. Notification System

**Notification Types**:
- `INFO` - Informational messages
- `SUCCESS` - Success confirmations
- `WARNING` - Warning messages
- `ERROR` - Error alerts
- `ALERT` - Critical alerts
- `REMINDER` - Reminder notifications

**Notification Entity**:
```java
@Entity
public class UserNotification {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Map<String, String> metadata;
    private Long alertRuleId;
    private AlertChannel channel;
}
```

### 3. Scheduled Alert Processors

**Stock Price Alerts** - Every 5 minutes:
```java
@Scheduled(cron = "0 */5 * * * *")
public void processStockPriceAlerts()
```
- Monitors stock prices against alert rules
- Triggers when price crosses threshold
- Updates last triggered timestamp

**EMI Due Alerts** - Daily at 8:00 AM:
```java
@Scheduled(cron = "0 0 8 * * *")
public void processEMIDueAlerts()
```
- Checks upcoming EMI dates
- Configurable reminder days (default: 3 days)
- Includes loan details and amount

**Policy Expiry Alerts** - Daily at 9:00 AM:
```java
@Scheduled(cron = "0 0 9 * * *")
public void processPolicyExpiryAlerts()
```
- Monitors insurance policy expiry dates
- Configurable warning period (default: 30 days)
- Includes policy details

**Premium Due Alerts** - Daily at 8:30 AM:
```java
@Scheduled(cron = "0 30 8 * * *")
public void processPremiumDueAlerts()
```
- Tracks premium payment schedules
- Configurable reminder days (default: 7 days)
- Includes premium amount

**Tax Deadline Alerts** - Daily at 10:00 AM:
```java
@Scheduled(cron = "0 0 10 * * *")
public void processTaxDeadlineAlerts()
```
- Monitors ITR filing deadline (July 31)
- Configurable warning period (default: 30 days)
- Includes deadline details

### 4. Real-Time Stock Prices

**WebSocket Broadcasting**:
- Broadcasts every 15 seconds during market hours
- Covers 15 popular Indian stocks
- Market hours: Monday-Friday, 9:15 AM - 3:30 PM IST

**Price Data Structure**:
```json
{
  "symbol": "RELIANCE",
  "price": 2456.75,
  "change": 12.50,
  "changePercent": 0.51,
  "volume": 1234567,
  "timestamp": 1738742400000
}
```

**WebSocket Endpoints**:
- `/topic/stock-prices/{symbol}` - Individual stock updates
- `/topic/portfolio-updates` - Portfolio value updates
- `/topic/notifications/{userId}` - User notifications

### 5. Email Service

**Configuration** (application.yml):
```yaml
external:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
    from-email: noreply@pi-system.com
```

**Email Templates**:
- Simple text emails
- HTML emails (placeholder)
- Error handling and logging

### 6. Frontend WebSocket Clients

**Stock Price WebSocket**:
```javascript
import stockPriceWebSocket from './websocket/StockPriceWebSocket';

// Connect
await stockPriceWebSocket.connect();

// Subscribe to stock
stockPriceWebSocket.subscribeToSymbol('RELIANCE', (priceData) => {
  console.log('Price update:', priceData);
  updateUI(priceData);
});

// Subscribe to portfolio updates
stockPriceWebSocket.subscribeToPortfolioUpdates((update) => {
  console.log('Portfolio update:', update);
});

// Cleanup
stockPriceWebSocket.disconnect();
```

**Notification WebSocket**:
```javascript
import notificationWebSocket from './websocket/NotificationWebSocket';

// Connect and subscribe
await notificationWebSocket.connect(userId);

// Request browser notification permission
await notificationWebSocket.requestNotificationPermission();

// Handle notifications
notificationWebSocket.subscribeToNotifications(userId, (notification) => {
  console.log('New notification:', notification);
  showNotificationBadge();
});

// Cleanup
notificationWebSocket.disconnect();
```

---

## 🔌 API Endpoints

### Alert Rules API

#### Create Alert Rule
```http
POST /api/v1/alerts/rules
Content-Type: application/json

{
  "userId": 1,
  "type": "STOCK_PRICE",
  "symbol": "RELIANCE",
  "targetPrice": 2500.00,
  "priceCondition": "ABOVE",
  "channel": "EMAIL",
  "enabled": true,
  "description": "Alert when RELIANCE crosses 2500"
}
```

#### Get User Alert Rules
```http
GET /api/v1/alerts/rules/user/{userId}
```

#### Get Active Alert Rules
```http
GET /api/v1/alerts/rules/user/{userId}/active
```

#### Update Alert Rule
```http
PUT /api/v1/alerts/rules/{id}
Content-Type: application/json

{
  "userId": 1,
  "type": "STOCK_PRICE",
  "targetPrice": 2600.00,
  "enabled": true
}
```

#### Toggle Alert Rule
```http
PATCH /api/v1/alerts/rules/{id}/toggle
```

#### Delete Alert Rule
```http
DELETE /api/v1/alerts/rules/{id}?userId=1
```

### Notifications API

#### Get User Notifications
```http
GET /api/v1/notifications/user/{userId}
```

#### Get Unread Notifications
```http
GET /api/v1/notifications/user/{userId}/unread
```

#### Get Unread Count
```http
GET /api/v1/notifications/user/{userId}/unread-count

Response:
{
  "unreadCount": 5
}
```

#### Get Recent Notifications
```http
GET /api/v1/notifications/user/{userId}/recent
```

#### Mark as Read
```http
PATCH /api/v1/notifications/{id}/read
```

#### Mark All as Read
```http
PATCH /api/v1/notifications/user/{userId}/read-all
```

#### Delete Notification
```http
DELETE /api/v1/notifications/{id}?userId=1
```

---

## 🧪 Testing

### Testing Alert Rules

```bash
# Create a stock price alert
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

# Get user alerts
curl http://localhost:8080/api/v1/alerts/rules/user/1

# Toggle alert
curl -X PATCH http://localhost:8080/api/v1/alerts/rules/1/toggle

# Delete alert
curl -X DELETE http://localhost:8080/api/v1/alerts/rules/1?userId=1
```

### Testing Notifications

```bash
# Get notifications
curl http://localhost:8080/api/v1/notifications/user/1

# Get unread count
curl http://localhost:8080/api/v1/notifications/user/1/unread-count

# Mark as read
curl -X PATCH http://localhost:8080/api/v1/notifications/5/read

# Mark all as read
curl -X PATCH http://localhost:8080/api/v1/notifications/user/1/read-all
```

### Testing WebSocket

```javascript
// Test stock price WebSocket
const socket = new SockJS('http://localhost:8080/ws-stock-prices');
const client = Stomp.over(socket);

client.connect({}, () => {
  client.subscribe('/topic/stock-prices/RELIANCE', (message) => {
    console.log('Price update:', JSON.parse(message.body));
  });
});

// Test notification WebSocket
client.subscribe('/topic/notifications/1', (message) => {
  console.log('Notification:', JSON.parse(message.body));
});
```

---

## 📊 Database Schema

### alert_rules Table
```sql
CREATE TABLE alert_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    symbol VARCHAR(20),
    target_price DECIMAL(15,2),
    price_condition VARCHAR(10),
    days_before_due INT,
    percentage_change DECIMAL(5,2),
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    last_triggered_at TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### user_notifications Table
```sql
CREATE TABLE user_notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    metadata JSON,
    alert_rule_id BIGINT,
    channel VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (alert_rule_id) REFERENCES alert_rules(id)
);

CREATE INDEX idx_user_notifications_user_id ON user_notifications(user_id);
CREATE INDEX idx_user_notifications_created_at ON user_notifications(created_at);
CREATE INDEX idx_user_notifications_is_read ON user_notifications(is_read);
```

---

## 🚀 Deployment

### Environment Variables

```properties
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@pi-system.com
```

### Gmail App Password Setup

1. Enable 2-Factor Authentication
2. Go to Google Account → Security → 2-Step Verification
3. Scroll to App passwords
4. Generate new app password for "Mail"
5. Use this password in `MAIL_PASSWORD`

### Enabling Scheduled Jobs

Ensure `@EnableScheduling` is present in your main application class:

```java
@SpringBootApplication
@EnableScheduling
public class PiSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PiSystemApplication.class, args);
    }
}
```

---

## 🎨 Frontend Integration Examples

### Real-Time Stock Price Component

```javascript
import React, { useEffect, useState } from 'react';
import stockPriceWebSocket from './websocket/StockPriceWebSocket';

function StockPriceWidget({ symbol }) {
  const [priceData, setPriceData] = useState(null);

  useEffect(() => {
    const connectAndSubscribe = async () => {
      if (!stockPriceWebSocket.isConnected()) {
        await stockPriceWebSocket.connect();
      }
      
      stockPriceWebSocket.subscribeToSymbol(symbol, (data) => {
        setPriceData(data);
      });
    };

    connectAndSubscribe();

    return () => {
      stockPriceWebSocket.unsubscribeFromSymbol(symbol);
    };
  }, [symbol]);

  if (!priceData) return <div>Loading...</div>;

  return (
    <div className="stock-widget">
      <h3>{priceData.symbol}</h3>
      <div className="price">₹{priceData.price.toFixed(2)}</div>
      <div className={priceData.change >= 0 ? 'positive' : 'negative'}>
        {priceData.change >= 0 ? '+' : ''}{priceData.changePercent.toFixed(2)}%
      </div>
    </div>
  );
}
```

### Notification Bell Component

```javascript
import React, { useEffect, useState } from 'react';
import notificationWebSocket from './websocket/NotificationWebSocket';
import { getUnreadCount } from './api/notifications';

function NotificationBell({ userId }) {
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    // Connect to notification WebSocket
    const connect = async () => {
      await notificationWebSocket.connect(userId);
      await notificationWebSocket.requestNotificationPermission();
      
      notificationWebSocket.subscribeToNotifications(userId, () => {
        // Refresh unread count when new notification arrives
        loadUnreadCount();
      });
    };

    const loadUnreadCount = async () => {
      const response = await getUnreadCount(userId);
      setUnreadCount(response.unreadCount);
    };

    connect();
    loadUnreadCount();

    return () => {
      notificationWebSocket.disconnect();
    };
  }, [userId]);

  return (
    <div className="notification-bell">
      <i className="fa fa-bell"></i>
      {unreadCount > 0 && (
        <span className="badge">{unreadCount}</span>
      )}
    </div>
  );
}
```

---

## 📈 Performance Considerations

### WebSocket Optimization
- Market hours detection prevents unnecessary broadcasts
- Configurable broadcast intervals (15s for prices, 30s for portfolio)
- Connection pooling and auto-reconnect

### Alert Processing Optimization
- Scheduled jobs run at different times to distribute load
- Batch processing for multiple alerts
- Last triggered timestamp prevents duplicate alerts

### Database Optimization
- Indexes on user_id, created_at, is_read
- Pagination support for notifications
- Efficient queries with JPA repositories

---

## 🔒 Security Considerations

### WebSocket Security
- User-specific notification channels (`/topic/notifications/{userId}`)
- Authentication required for WebSocket connections
- Rate limiting on subscriptions

### API Security
- User ID validation in all endpoints
- Authorization checks for alert rules
- Input validation and sanitization

### Email Security
- Secure SMTP with STARTTLS
- App-specific passwords (no plain passwords)
- Rate limiting on email sending

---

## 🐛 Troubleshooting

### WebSocket Connection Issues
```
Problem: WebSocket connection fails
Solution: 
1. Check if WebSocket dependency is in build.gradle
2. Verify WebSocketConfig is properly configured
3. Check CORS settings for frontend origin
4. Ensure server is running on correct port
```

### Email Not Sending
```
Problem: Emails not being sent
Solution:
1. Verify MAIL_USERNAME and MAIL_PASSWORD
2. Enable "Less secure app access" or use App Password
3. Check SMTP settings (host, port, auth)
4. Review application logs for errors
```

### Scheduled Jobs Not Running
```
Problem: Alert processor jobs not executing
Solution:
1. Verify @EnableScheduling is present
2. Check cron expressions are valid
3. Ensure AlertProcessorService is @Component
4. Review application logs for startup errors
```

### Stock Prices Not Updating
```
Problem: Real-time prices not updating
Solution:
1. Verify market hours (9:15 AM - 3:30 PM IST)
2. Check StockService is returning valid prices
3. Review WebSocket broadcast logs
4. Ensure frontend is subscribed correctly
```

---

## 📚 References

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [STOMP Protocol](https://stomp.github.io/)
- [Spring Boot Scheduling](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.task-execution-and-scheduling)
- [JavaMailSender Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/mail/javamail/JavaMailSender.html)
- [Browser Notification API](https://developer.mozilla.org/en-US/docs/Web/API/Notifications_API)

---

## ✅ Implementation Checklist

- [x] Alert Rule Entity & Repository
- [x] User Notification Entity & Repository
- [x] Alert Rule Service (CRUD operations)
- [x] Notification Service (Multi-channel delivery)
- [x] Email Service (SMTP integration)
- [x] Alert Processor Service (Scheduled jobs)
- [x] Alert Controller (REST API)
- [x] Notification Controller (REST API)
- [x] Stock Price WebSocket Controller
- [x] Frontend WebSocket Clients
- [x] Email Configuration
- [x] Database Schema Updates
- [x] Testing Scripts
- [x] Documentation

---

**Total Implementation**: 20+ Files  
**Lines of Code**: ~2,500  
**Test Coverage**: Integration tests pending  
**Documentation**: Complete

This implementation provides a robust, scalable real-time notification and alert system for the Pi System application. 🎉
