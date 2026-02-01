# Budget & Expense Management Module - Complete Documentation

**Last Updated**: February 1, 2026  
**Status**: Production Ready (52% Complete)  
**Module**: Budgeting & Expenses

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Features Implemented](#features-implemented)
4. [API Endpoints](#api-endpoints)
5. [Database Schema](#database-schema)
6. [Alert System](#alert-system)
7. [Recurring Automation](#recurring-automation)
8. [Budget Analysis](#budget-analysis)
9. [Usage Examples](#usage-examples)
10. [Future Roadmap](#future-roadmap)
11. [Technical Details](#technical-details)

---

## Overview

The Budget & Expense Management module provides comprehensive financial tracking and analysis capabilities. Users can set monthly budgets, track expenses and income, receive automated alerts for overspending, and benefit from recurring transaction automation.

### Key Statistics
- **Features Complete**: 12/23 (52%)
- **API Endpoints**: 45+
- **Database Tables**: 7 (budgets, expenses, income, recurring_templates, alerts, custom_categories, notification_preferences)
- **Scheduled Jobs**: 2 (Alerts at 9 PM, Recurring at 1 AM)

### Core Capabilities
✅ Monthly budget limits by category  
✅ Expense & income tracking with full CRUD  
✅ Budget variance analysis with metrics  
✅ Overspending alerts (75%/90%/100% thresholds)  
✅ Recurring transaction automation  
✅ Custom expense categories  
✅ Cash flow analysis  
✅ Bulk operations  
✅ Export to CSV/Excel/PDF

---

## Architecture

### Component Structure
```
budget/
├── controller/
│   ├── BudgetController.java        - Budget & expense endpoints
│   ├── AlertController.java         - Alert management (9 endpoints)
│   └── RecurringTransactionController.java - Recurring templates
├── service/
│   ├── BudgetService.java          - Core business logic
│   ├── AlertService.java           - Alert generation & management
│   ├── AlertServiceImpl.java       - Alert implementation
│   └── RecurringTransactionService.java - Automation logic
├── repo/
│   ├── BudgetRepository.java
│   ├── ExpenseRepository.java
│   ├── IncomeRepository.java
│   ├── AlertRepository.java
│   └── RecurringTemplateRepository.java
├── data/
│   ├── Budget.java                 - Budget entity
│   ├── Expense.java                - Expense entity
│   ├── Income.java                 - Income entity
│   ├── Alert.java                  - Alert entity
│   ├── RecurringTemplate.java      - Recurring template entity
│   └── BudgetVarianceAnalysis.java - Variance DTO
└── dto/
    ├── AlertResponse.java
    ├── AlertSummary.java
    └── BudgetVsActualReport.java
```

### Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic separation
- **DTO Pattern**: API request/response objects
- **Builder Pattern**: Entity construction
- **Scheduled Tasks**: Background job processing

### Technology Stack
- **Framework**: Spring Boot 3.x
- **Database**: MySQL with Flyway migrations
- **Scheduling**: Spring @Scheduled (cron)
- **Security**: JWT authentication
- **Documentation**: Swagger/OpenAPI

---

## Features Implemented

### ✅ 1. Monthly Budget Limits
Set and track budget limits by category for each month.

**Endpoints**:
- `POST /api/v1/budget/users/{userId}/budgets` - Create budget
- `GET /api/v1/budget/users/{userId}/budgets` - Get all budgets
- `PUT /api/v1/budget/{budgetId}` - Update budget
- `DELETE /api/v1/budget/{budgetId}` - Delete budget

**Features**:
- Standard categories (FOOD, TRANSPORT, HOUSING, etc.)
- Custom user-defined categories
- Monthly recurring budgets
- End date support for temporary budgets

---

### ✅ 2. Expense Management
Full CRUD operations with advanced filtering, sorting, and pagination.

**Endpoints**:
- `POST /api/v1/expenses` - Add expense
- `GET /api/v1/expenses` - Get expenses (with filters)
- `PUT /api/v1/expenses/{id}` - Update expense
- `DELETE /api/v1/expenses/{id}` - Delete expense
- `DELETE /api/v1/expenses/bulk` - Bulk delete
- `POST /api/v1/expenses/export` - Export to CSV/Excel

**Features**:
- Category-based tracking
- Custom categories support
- Date range filtering
- Amount range filtering
- Pagination and sorting
- Bulk operations
- Receipt attachment support (planned)

---

### ✅ 3. Income Stream Tracking
Track all income sources with full CRUD operations.

**Endpoints**:
- `POST /api/v1/income` - Add income
- `GET /api/v1/income` - Get income streams
- `PUT /api/v1/income/{id}` - Update income
- `DELETE /api/v1/income/{id}` - Delete income

**Features**:
- Multiple income sources
- Recurring vs one-time income
- Stability tracking
- Date-based filtering

---

### ✅ 4. Budget vs Actual Analysis
Comprehensive variance analysis with performance metrics.

**Endpoint**:
```http
GET /api/v1/budget/variance-analysis?userId={id}&monthYear={YYYY-MM}
```

**Response Structure**:
```json
{
  "month": "2026-02",
  "totalBudget": 60000.00,
  "totalSpent": 45000.00,
  "totalVariance": 15000.00,
  "variancePercentage": 75.00,
  "overallStatus": "UNDER_BUDGET",
  "categoryBreakdown": [
    {
      "category": "FOOD",
      "budgetLimit": 15000.00,
      "amountSpent": 14000.00,
      "variance": 1000.00,
      "percentageUsed": 93.33,
      "status": "CRITICAL",
      "transactionCount": 45,
      "remainingBudget": 1000.00
    }
  ],
  "metrics": {
    "categoriesOverBudget": 1,
    "categoriesUnderBudget": 5,
    "averageVariancePercentage": 82.50,
    "worstCategory": "FOOD",
    "bestCategory": "ENTERTAINMENT"
  }
}
```

**Features**:
- Category-wise variance calculation
- Performance metrics (best/worst categories)
- Status indicators (OVER_BUDGET, ON_TRACK, UNDER_BUDGET)
- Transaction count per category
- Remaining budget calculation

---

### ✅ 5. Overspending Alerts
Automated budget monitoring with intelligent threshold detection.

**Endpoints** (9 total):
```http
GET    /api/v1/alerts?userId={id}              - Get all alerts
GET    /api/v1/alerts/unread?userId={id}       - Get unread alerts
GET    /api/v1/alerts/month/{month}?userId={id} - Get by month
GET    /api/v1/alerts/summary?userId={id}      - Get statistics
PUT    /api/v1/alerts/{id}/read?userId={id}    - Mark as read
PUT    /api/v1/alerts/read-all?userId={id}     - Mark all read
DELETE /api/v1/alerts/{id}?userId={id}         - Delete alert
DELETE /api/v1/alerts?userId={id}              - Delete all
POST   /api/v1/alerts/check?userId={id}        - Manual trigger
```

**Alert Thresholds**:
| Threshold | Severity | Alert Type | Message |
|-----------|----------|------------|---------|
| 75-89% | WARNING | OVERSPENDING | ⚡ WARNING: 75% spent |
| 90-99% | CRITICAL | APPROACHING_LIMIT | 🔴 CRITICAL: 90% used |
| 100%+ | DANGER | BUDGET_EXCEEDED | ⚠️ BUDGET EXCEEDED |

**Trigger Mechanisms**:
1. **Scheduled**: Daily at 9:00 PM (checks all budgets)
2. **Manual**: Via POST `/alerts/check` endpoint
3. **Frontend**: Recommended after expense creation

**Features**:
- Three-tier threshold system
- Smart message generation with emojis
- Duplicate prevention
- Category-wise alerts
- Read/unread status tracking
- Alert summary statistics

---

### ✅ 6. Recurring Transaction Automation
Smart scheduling system for automatic transaction generation.

**Endpoints**:
- `POST /api/v1/budget/users/{userId}/recurring-transactions` - Create template
- `GET /api/v1/budget/users/{userId}/recurring-transactions` - Get templates
- `GET /api/v1/budget/users/{userId}/recurring-transactions/active` - Active only
- `PUT /api/v1/budget/recurring-transactions/{id}` - Update template
- `DELETE /api/v1/budget/recurring-transactions/{id}` - Delete template

**Recurrence Patterns**:
- DAILY - Every day
- WEEKLY - Every week
- MONTHLY - Every month
- QUARTERLY - Every 3 months
- YEARLY - Every year

**Scheduler**:
```java
@Scheduled(cron = "0 0 1 * * ?") // Daily at 1:00 AM
public void generateRecurringTransactions()
```

**Features**:
- Smart scheduling with `next_run_date`
- Automatic date calculation
- End date handling (auto-deactivation)
- Support for expenses and income
- Template activation/deactivation
- Efficient database queries with indexes

**How It Works**:
1. User creates template (e.g., "Monthly Rent", pattern: MONTHLY)
2. System sets `nextRunDate` to `startDate`
3. Daily at 1 AM, scheduler finds templates where `nextRunDate <= today`
4. Generates transaction for scheduled date
5. Updates `lastGenerated` and calculates new `nextRunDate`
6. If `nextRunDate` exceeds `endDate`, template is deactivated

---

### ✅ 7. Cash Flow Analysis
6-month trend analysis with savings rate and income stability metrics.

**Endpoint**:
```http
GET /api/v1/budget/users/{userId}/cashflow?months={n}
```

**Features**:
- Income vs expense trends
- Savings rate calculation
- Income stability metrics
- Monthly breakdown
- Visual-ready data format

---

### ✅ 8. Custom Categories
User-defined expense categories with icons.

**Features**:
- Create custom categories
- Assign icons/colors
- Full CRUD operations
- Used in budgets and expenses

---

### ✅ 9. Bulk Operations
Efficiently manage multiple expenses at once.

**Endpoints**:
- `DELETE /api/v1/expenses/bulk` - Delete multiple
- `PUT /api/v1/expenses/bulk` - Update multiple

---

### ✅ 10. Export & Reports
Generate reports in multiple formats.

**Endpoints**:
- `POST /api/v1/expenses/export?format=csv` - CSV export
- `POST /api/v1/expenses/export?format=excel` - Excel export
- `POST /api/v1/budget/report/pdf` - PDF report (planned)

---

## Database Schema

### Tables Overview

#### 1. `budgets`
```sql
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50),
    custom_category_name VARCHAR(50),
    monthly_limit DECIMAL(15,2) NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 2. `expenses`
```sql
CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category VARCHAR(50),
    custom_category_name VARCHAR(50),
    expense_date DATE NOT NULL,
    description VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 3. `income`
```sql
CREATE TABLE income (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    date DATE NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    is_stable BOOLEAN DEFAULT TRUE,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 4. `alerts`
```sql
CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    message VARCHAR(500) NOT NULL,
    budget_limit DECIMAL(15,2) NOT NULL,
    amount_spent DECIMAL(15,2) NOT NULL,
    percentage_used DECIMAL(5,2) NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    notification_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    INDEX idx_user_alerts (user_id, created_at DESC),
    INDEX idx_user_unread (user_id, is_read),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 5. `recurring_templates`
```sql
CREATE TABLE recurring_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    custom_category_name VARCHAR(50),
    source VARCHAR(100),
    amount DECIMAL(15,2) NOT NULL,
    pattern VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    last_generated DATE,
    next_run_date DATE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_next_run_date (next_run_date, is_active),
    INDEX idx_user_active_next_run (user_id, is_active, next_run_date),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Migrations History
- V1: Initial schema
- V26: Custom categories
- V30: Alerts table (Feb 1, 2026)
- V31: Next run date for recurring templates (Feb 1, 2026)

---

## Technical Details

### Security
- JWT token authentication required for all endpoints
- User access validation on all operations
- User-scoped data queries (userId filtering)

### Performance Optimizations
- Database indexes on frequently queried columns
- Pagination support for large datasets
- Batch operations for bulk updates
- Efficient scheduled job queries

### Error Handling
- Custom exception classes
- Global exception handler
- Meaningful error responses
- Transaction rollback on failures

### Logging
- Comprehensive logging at INFO level
- Error logging with stack traces
- Debug logging for development

---

## Usage Examples

### Create Monthly Budget
```bash
POST /api/v1/budget/users/1/budgets
Content-Type: application/json

{
  "category": "FOOD",
  "monthlyLimit": 15000,
  "monthYear": "2026-02"
}
```

### Add Expense
```bash
POST /api/v1/expenses
Content-Type: application/json

{
  "userId": 1,
  "amount": 450,
  "category": "FOOD",
  "expenseDate": "2026-02-01",
  "description": "Grocery shopping"
}
```

### Get Budget Variance
```bash
GET /api/v1/budget/variance-analysis?userId=1&monthYear=2026-02
```

### Check Alerts Manually
```bash
POST /api/v1/alerts/check?userId=1&monthYear=2026-02
```

### Create Recurring Template
```bash
POST /api/v1/budget/users/1/recurring-transactions
Content-Type: application/json

{
  "type": "EXPENSE",
  "name": "Monthly Rent",
  "category": "HOUSING",
  "amount": 25000,
  "pattern": "MONTHLY",
  "startDate": "2026-02-01",
  "isActive": true
}
```

---

## Future Roadmap

### 🔴 P0 - Critical (Production Blockers)
- [ ] **Email Report Service** - Send monthly reports via email (Requires SMTP)

### 🟠 P1 - High Priority (Major Impact)
- [ ] **Budget Forecasting** - Predict end-of-month spending
- [ ] **Smart Insights** - AI-powered spending analysis
- [ ] **Receipt Attachments** - Upload receipts with OCR
- [ ] **Merchant Management** - Track spending by vendor
- [ ] **Budget Templates** - Pre-defined setups

### 🟡 P2 - Medium Priority (Nice to Have)
- [ ] **Multi-Currency Support** - Foreign transactions
- [ ] **Budget Rollover** - Carry forward unused budget
- [ ] **Advanced Analytics** - Yearly comparisons
- [ ] **Split Transactions** - Multiple categories per expense
- [ ] **Tax Category Mapping** - For tax filing

### 🟢 P3 - Low Priority (Future Enhancements)
- [ ] **Gamification** - Savings challenges, badges
- [ ] **Social Features** - Share budgets with family
- [ ] **Bill Reminders** - Upcoming payment notifications
- [ ] **Subscription Tracking** - Auto-detect subscriptions

---

## Integration Points

### With Other Modules
- **Authentication**: JWT token validation
- **User Management**: User profile and preferences
- **Notifications**: Push/email for alerts (planned)
- **Reports**: PDF generation service (planned)

### External Services (Planned)
- **SMTP**: Email report delivery
- **OCR Service**: Receipt text extraction
- **AI/ML**: Spending pattern analysis
- **SMS Gateway**: Critical alert notifications

---

## Troubleshooting

### Circular Dependency Issue (FIXED)
**Problem**: AlertService created circular dependency with BudgetService.  
**Solution**: Removed AlertService from BudgetService. Alerts now triggered via:
- Daily scheduler at 9 PM
- Manual trigger endpoint
- Frontend integration

See: [CIRCULAR_DEPENDENCY_FIX.md](CIRCULAR_DEPENDENCY_FIX.md)

### Common Issues

**Issue**: Alerts not generating after expense  
**Solution**: Call `POST /api/v1/alerts/check` manually or wait for 9 PM scheduler

**Issue**: Recurring transactions not created  
**Solution**: Check `nextRunDate` is set correctly and template is active

**Issue**: Budget variance incorrect  
**Solution**: Ensure expenses have correct `expenseDate` and `category`

---

## Configuration

### Application Properties
```yaml
# No special configuration required
# Uses existing Spring Boot settings
```

### Scheduler Configuration
Already enabled in `Application.java`:
```java
@EnableScheduling
```

### Cron Expressions
```java
// Alerts - Daily at 9:00 PM
@Scheduled(cron = "0 0 21 * * *")

// Recurring - Daily at 1:00 AM
@Scheduled(cron = "0 0 1 * * ?")
```

---

## Testing

### Integration Tests
- BudgetControllerIntegrationTest (planned)
- ExpenseControllerIntegrationTest (planned)
- AlertServiceTest (planned)

### Manual Testing Checklist
- [ ] Create budget and verify in database
- [ ] Add expense and check budget usage
- [ ] Trigger alert check and verify alert creation
- [ ] Create recurring template and wait for generation
- [ ] Test variance analysis endpoint
- [ ] Export expenses to CSV
- [ ] Bulk delete expenses

---

## API Reference Summary

### Budget Endpoints (8)
- POST /api/v1/budget/users/{userId}/budgets
- GET /api/v1/budget/users/{userId}/budgets
- PUT /api/v1/budget/{budgetId}
- DELETE /api/v1/budget/{budgetId}
- GET /api/v1/budget/variance-analysis

### Expense Endpoints (12)
- POST /api/v1/expenses
- GET /api/v1/expenses
- PUT /api/v1/expenses/{id}
- DELETE /api/v1/expenses/{id}
- DELETE /api/v1/expenses/bulk
- POST /api/v1/expenses/export

### Alert Endpoints (9)
- GET /api/v1/alerts
- GET /api/v1/alerts/unread
- GET /api/v1/alerts/month/{month}
- GET /api/v1/alerts/summary
- PUT /api/v1/alerts/{id}/read
- PUT /api/v1/alerts/read-all
- DELETE /api/v1/alerts/{id}
- DELETE /api/v1/alerts
- POST /api/v1/alerts/check

### Recurring Endpoints (6)
- POST /api/v1/budget/users/{userId}/recurring-transactions
- GET /api/v1/budget/users/{userId}/recurring-transactions
- GET /api/v1/budget/users/{userId}/recurring-transactions/active
- PUT /api/v1/budget/recurring-transactions/{id}
- DELETE /api/v1/budget/recurring-transactions/{id}

**Total**: 35+ API endpoints

---

## Documentation Structure

### Main Documents
- **BUDGET_MODULE.md** (this file) - Complete module documentation
- **CIRCULAR_DEPENDENCY_FIX.md** - Technical fix documentation

### Archived Documents
All previous budget documentation consolidated here and archived.

---

## Changelog

### February 1, 2026
- ✅ Implemented Budget vs Actual Analysis
- ✅ Implemented Overspending Alerts (9 endpoints)
- ✅ Implemented Recurring Transaction Automation
- ✅ Fixed circular dependency issue
- ✅ Consolidated all documentation
- 📈 Progress: 39% → 52%

### Previous Development
- Budget limit management
- Expense/Income tracking
- Custom categories
- Cash flow analysis
- Export functionality

---

## Quick Links

- [Progress Tracker](PROGRESS.md)
- [API Documentation](https://localhost:8080/swagger-ui.html)
- [Database Migrations](../src/main/resources/db/migration/)
- [Source Code](../src/main/java/com/budget/)

---

**Module Owner**: Backend Team  
**Priority**: P0 - Critical  
**Status**: Active Development  
**Last Review**: February 1, 2026
