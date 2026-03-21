# FEATURE AUDIT REPORT
**PI-System Phase-1 Architecture Compliance**  
**Audit Date:** March 12, 2026  
**Auditor:** Senior Software Architect  

---

## EXECUTIVE SUMMARY

**Overall Phase-1 Completion: 72%**

The PI-System fintech application has a solid foundation with excellent implementation of budget management, networth calculation, and financial insights. However, critical Phase-1 features including SMS transaction parsing, phone OTP verification, and comprehensive reminder systems are missing or incomplete.

**Key Findings:**
- ✅ Budget System: 100% Complete (Production Ready)
- ✅ Networth Engine: 100% Complete (Production Ready)
- ✅ Insights System: 90% Complete
- ⚠️ Transaction Engine: 62.5% Complete (Missing SMS parsing)
- ⚠️ User System: 80% Complete (Missing OTP verification)
- ⚠️ Reminder System: 40% Complete (Missing bill/EMI reminders)

---

## ✅ IMPLEMENTED FEATURES

### 1. USER SYSTEM (75% Complete)

#### ✅ Implemented Components:

**Authentication & Authorization:**
- **User Registration** 
  - Location: `src/main/java/com/auth/controller/AuthController.java`
  - Method: `register()`
  - Features: Email/password registration with validation
  - Database: Users table from V1__Initial_Schema.sql

- **Login with JWT Authentication**
  - Location: `src/main/java/com/auth/controller/AuthController.java`
  - Method: `login()`
  - Features: Email/password authentication, JWT token generation, refresh token support
  - Security: BCrypt password hashing
  - Activity logging on successful/failed login

- **Profile Management**
  - Location: `src/main/java/com/users/controller/UserProfileController.java`
  - Service: `UserProfileServiceImpl.java`
  - Features: Financial profile (income, dependents, risk tolerance)
  - API Endpoints:
    - GET `/api/v1/users/{userId}/profile`
    - POST `/api/v1/users/{userId}/profile`

- **Password Reset/Forgot Password**
  - Location: `src/main/java/com/auth/controller/AuthController.java`
  - Method: `forgotPassword()`
  - Data Model: `ForgotPasswordRequest.java`

- **Refresh Token Mechanism**
  - Service: `src/main/java/com/auth/service/RefreshTokenService.java`
  - Implementation: `InMemoryRefreshTokenService.java`
  - Features: Token refresh without re-authentication

- **Activity Logging**
  - Service: `src/main/java/com/audit/service/ActivityLogService.java`
  - Features: Login attempts, user actions, audit trail

- **User Profile with Financial Data**
  - Service: `UserProfileServiceImpl.java`
  - Features: Income tracking, dependents, risk tolerance, complete profile check

#### ❌ Missing Components:

1. **Phone OTP Verification** ⚠️ HIGH PRIORITY
   - Status: Documentation exists (`docs/OTP_MODULE_IMPLEMENTATION.md`) but NO implementation
   - Missing: OTP generation service, SMS gateway integration, OTP session management
   - Required Database: `otp_sessions` table
   - Required Columns: `users.phone_number`, `users.phone_verified`

2. **Multi-factor Authentication (MFA)**
   - Status: Not implemented
   - Required: TOTP/SMS-based 2FA

#### Database Status:
- ✅ Users table exists
- ✅ User profiles table exists
- ✅ Refresh tokens (in-memory)
- ❌ OTP sessions table missing
- ❌ Phone verification fields missing

---

### 2. TRANSACTION ENGINE (62.5% Complete)

#### ✅ Implemented Components:

**Manual Transaction Management:**
- **Manual Expense Entry**
  - Controller: `BudgetController.java`
  - Method: `addExpense()`
  - Endpoint: POST `/api/v1/budget/expense`
  - Validation: Amount, category, date required
  - Service: `BudgetService.addExpense()`

- **Edit Transaction**
  - Controller: `BudgetController.java`
  - Method: `updateExpense()`
  - Endpoint: PUT `/api/v1/budget/expense/{id}`
  - Features: Full update of expense details

- **Delete Transaction**
  - Controller: `BudgetController.java`
  - Method: `deleteExpense()`
  - Endpoint: DELETE `/api/v1/budget/expense/{id}`
  - Features: Soft delete support

- **Bulk Transaction Operations**
  - Features:
    - Bulk delete expenses
    - Bulk update category
  - Service: `BudgetService.bulkDeleteExpenses()`
  - Endpoint: POST `/api/v1/budget/expense/bulk-delete`

**Transaction Categorization:**
- **Predefined Categories**
  - Location: `src/main/java/com/budget/data/ExpenseCategory.java`
  - Categories: FOOD, RENT, TRANSPORT, ENTERTAINMENT, SHOPPING, UTILITIES, HEALTH, EDUCATION, INVESTMENT, INSURANCE, OTHERS, TOTAL
  
- **Custom Categories**
  - Entity: `CustomCategory.java`
  - Features: User-defined categories with icons
  - Full CRUD operations
  - Endpoints:
    - POST `/api/v1/budget/category/custom`
    - GET `/api/v1/budget/category/custom/{userId}`
    - PUT `/api/v1/budget/category/custom/{id}`
    - DELETE `/api/v1/budget/category/custom/{id}`

**Recurring Transaction Templates:**
- **Implementation**
  - Entity: `RecurringTransaction.java` (Investment module)
  - Entity: `RecurringTemplate.java` (Budget module)
  - Service: `InvestmentRecurringTransactionService.java`
  - Service: `BudgetRecurringTransactionService.java`
  
- **Features**
  - 8 transaction types supported
  - 7 frequency options: DAILY, WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL, CUSTOM
  - Automatic execution via scheduler
  - Execution history tracking
  - Pause/Resume/Cancel operations
  - Reminder support
  - Smart next execution date calculation
  
- **Scheduler**
  - Location: `RecurringTransactionScheduler.java`
  - Schedule: Daily at 1:00 AM
  - Job: INVESTMENT_RECURRING_TRANSACTIONS

**Transaction Tags:**
- **Implementation**
  - Entity: `Tag.java`
  - Database: `tags` table, `expense_tags` junction table
  - Features: Tag expenses for better organization
  - Color coding support
  
**UPI Payment Structure (Partial):**
- **Implementation**
  - Service: `UPITransactionService.java`
  - Models: `Transaction.java`, `UpiId.java`, `BankAccount.java`
  - Features:
    - P2P transactions
    - P2M transactions (merchant payments)
    - PIN verification
    - Transaction history
  - Database: V57__Create_UPI_Module_Tables.sql

#### ❌ Missing Components:

1. **SMS Parsing for Transactions** ⚠️ CRITICAL
   - Status: NOT FOUND - No implementation
   - Impact: Core Phase-1 feature completely missing
   
   **Required Components:**
   - SMS permission handling (Android/iOS)
   - SMS reader service with background monitoring
   - Transaction pattern matching (regex-based)
   - Bank-specific SMS format parsers
   - Automatic expense creation from parsed SMS
   - SMS transaction history and audit log
   
   **Required Database Tables:**
   ```sql
   CREATE TABLE sms_transactions (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL,
       raw_sms_text TEXT NOT NULL,
       sender VARCHAR(50) NOT NULL,
       received_at TIMESTAMP NOT NULL,
       parsed_amount DECIMAL(15,2),
       parsed_merchant VARCHAR(255),
       parsed_type VARCHAR(20), -- DEBIT/CREDIT
       account_last4 VARCHAR(4),
       transaction_date TIMESTAMP,
       is_parsed BOOLEAN DEFAULT FALSE,
       parse_confidence DECIMAL(5,2),
       created_expense_id BIGINT,
       status VARCHAR(20) -- PENDING, PROCESSED, FAILED, IGNORED
   );
   
   CREATE TABLE bank_sms_patterns (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       bank_name VARCHAR(100) NOT NULL,
       sender_id VARCHAR(50) NOT NULL,
       sms_pattern TEXT NOT NULL,
       amount_regex VARCHAR(255),
       merchant_regex VARCHAR(255),
       date_regex VARCHAR(255),
       type_regex VARCHAR(255),
       is_active BOOLEAN DEFAULT TRUE
   );
   ```

2. **Unknown Transaction Review System** ⚠️ HIGH PRIORITY
   - Status: NOT FOUND
   
   **Required Components:**
   - Unknown/uncategorized transactions queue
   - Review workflow UI
   - Bulk categorization
   - Smart suggestions for categorization
   - Transaction matching algorithms
   - Learning from user corrections
   
   **Required Database:**
   ```sql
   ALTER TABLE expenses ADD COLUMN status VARCHAR(20) 
       DEFAULT 'CATEGORIZED' -- CATEGORIZED, PENDING_REVIEW, UNKNOWN
   
   CREATE TABLE transaction_review_queue (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       expense_id BIGINT NOT NULL,
       suggested_category VARCHAR(50),
       confidence_score DECIMAL(5,2),
       review_status VARCHAR(20), -- PENDING, APPROVED, REJECTED
       reviewed_by BIGINT,
       reviewed_at TIMESTAMP
   );
   ```

3. **Transfer Between Accounts** ⚠️ HIGH PRIORITY
   - Status: NOT FOUND
   
   **Problem:** Currently, transfers would be counted as both expense and income
   
   **Required Components:**
   - Account-to-account transfer API
   - Internal transfer tracking (excluded from budget)
   - Transfer reconciliation
   - Transfer history
   
   **Required Implementation:**
   ```java
   public enum TransactionType {
       EXPENSE,
       INCOME,
       TRANSFER  // NEW - Not counted in budget
   }
   
   @Entity
   public class AccountTransfer {
       private Long id;
       private Long userId;
       private Long sourceAccountId;
       private Long destinationAccountId;
       private BigDecimal amount;
       private LocalDate transferDate;
       private String notes;
       private String status; // PENDING, COMPLETED, FAILED
   }
   ```

4. **Automatic Categorization from SMS**
   - Status: NOT FOUND
   - Required: ML-based or rule-based categorization
   - Features: Learn from user patterns, merchant mapping

#### Database Status:
- ✅ Expenses table (V15__Budget_and_Expenses.sql)
- ✅ Recurring templates table (V28__Create_Recurring_Tags_Receipts.sql)
- ✅ Tags and expense_tags tables (V28)
- ✅ Receipts table (V28)
- ✅ UPI transactions table (V57__Create_UPI_Module_Tables.sql)
- ❌ SMS transactions table - MISSING
- ❌ Bank SMS patterns table - MISSING
- ❌ Unknown transactions queue - MISSING
- ❌ Account transfers table - MISSING

---

### 3. BUDGET SYSTEM (100% Complete) 🎉

#### ✅ Fully Implemented Components:

**Budget Management:**
- **Overall Budget**
  - Service: `BudgetService.java`
  - Method: `setBudget()`
  - Endpoint: POST `/api/v1/budget`
  - Features: Set total monthly budget limit

- **Category Budget**
  - Features: Per-category budget limits
  - Supports: Standard + custom categories
  - Auto-calculation: Total budget from category budgets
  
- **Monthly Budget Calculation**
  - Service: `BudgetService.calculateTotalMonthlyBudget()`
  - Method: `updateTotalBudget()`
  - Features: Auto-sum all category budgets

**Budget Alerts (Comprehensive):**
- **Implementation**
  - Service: `AlertServiceImpl.java`
  - Method: `checkBudgetsAndGenerateAlerts()`
  
- **Alert Thresholds**
  - 75% - WARNING (Yellow)
  - 90% - CRITICAL (Orange)
  - 100% - DANGER (Red)
  
- **Alert Features**
  - Category-wise budget monitoring
  - Automatic alert generation
  - Duplicate alert prevention
  - Severity classification
  - Real-time notifications
  
- **Scheduler**
  - Location: `AlertScheduler.java`
  - Schedule: Daily at 9:00 PM
  - Job: BUDGET_ALERTS
  - Features: Auto-check all user budgets

**Expense & Income Calculation:**
- **Expense Calculation**
  - Automatic from expense transactions
  - Category-wise aggregation
  - Date range filtering
  - Real-time balance calculation
  
- **Income Calculation**
  - Entity: `Income.java`
  - Features: Multiple income sources
  - Category tracking
  - Monthly aggregation

**Export & Reports:**
- **CSV Export**
  - Service: `ExportService.java`
  - Endpoints:
    - GET `/api/v1/budget/expense/{userId}/export/csv`
    - GET `/api/v1/budget/income/{userId}/export/excel`
  
- **Excel Export**
  - Format: XLSX with multiple sheets
  - Features: Formatted spreadsheets, formulas
  
- **PDF Export**
  - Service: `ReportGenerationService.java`
  - Method: `generateMonthlyReport()`
  - Endpoint: GET `/api/v1/budget/report/{userId}/pdf`
  - Features: Professional PDF with charts
  
- **Email Reports**
  - Controller: `BudgetController.emailReport()`
  - Endpoint: POST `/api/v1/budget/report/{userId}/email`
  - Features: Scheduled reports, custom recipient

**Budget Analysis:**
- **Budget vs Actual Analysis**
  - DTO: `BudgetReportDTO.java`
  - Features:
    - Category breakdown
    - Percentage used
    - Variance calculation
    - Remaining budget
  
- **Cash Flow Analysis**
  - DTO: `CashFlowDTO.java`
  - Features:
    - Income vs expense trends
    - Savings rate calculation
    - 6-month trend analysis
    - Income stability metrics
  
- **Variance Analysis**
  - DTO: `BudgetVarianceAnalysis.java`
  - Features:
    - Overall budget status
    - Category-wise variance
    - Over/under budget tracking

**Additional Features:**
- Pagination and filtering
- Search functionality
- Date range queries
- Sorting options
- Bulk operations
- Custom categories support

#### Database Status:
- ✅ Budgets table (V15__Budget_and_Expenses.sql)
- ✅ Expenses table (V15)
- ✅ Income table (V15)
- ✅ Alerts table (V30__Create_Alerts_Table.sql)
- ✅ Custom categories table
- ✅ Tags table (V28)
- ✅ Receipts table (V28)

#### API Endpoints (Complete):
```
POST   /api/v1/budget/expense              - Add expense
GET    /api/v1/budget/expense/{userId}     - Get expenses (paginated)
GET    /api/v1/budget/expense/detail/{id}  - Get expense by ID
PUT    /api/v1/budget/expense/{id}         - Update expense
DELETE /api/v1/budget/expense/{id}         - Delete expense
POST   /api/v1/budget                      - Set budget
GET    /api/v1/budget/{userId}             - Get budgets
GET    /api/v1/budget/report/{userId}      - Monthly report
GET    /api/v1/budget/cashflow/{userId}    - Cash flow analysis
GET    /api/v1/budget/expense/{userId}/export/csv - Export CSV
GET    /api/v1/budget/report/{userId}/pdf  - Export PDF
POST   /api/v1/budget/report/{userId}/email - Email report
```

---

### 4. NETWORTH ENGINE (100% Complete) 🎉

#### ✅ Fully Implemented Components:

**Assets Calculation:**
Location: `NetWorthReadServiceImpl.java`

1. **Bank Accounts**
   - Model: `BankAccount.java`
   - Service: `BankAccountService.java`
   - Features: Balance tracking, account details

2. **Cash (Savings Accounts)**
   - Model: `SavingsAccount.java`
   - Service: `SavingsAccountService.java`
   - Features: Interest calculation, balance management

3. **Fixed Deposits**
   - Service: `FixedDepositService.java`
   - Entity: `FixedDeposit.java`
   - Features:
     - Automatic maturity calculation
     - Compound interest (quarterly)
     - Tenure tracking
     - Maturity date calculation
   - Formula: A = P(1 + r/n)^(nt)
   - Controller: `FixedDepositController.java`

4. **Recurring Deposits**
   - Service: `RecurringDepositService.java`
   - Entity: `RecurringDeposit.java`
   - Features:
     - Monthly installment tracking
     - Maturity amount calculation
     - RD-specific interest formula
   - Formula: M = P × n × [1 + (n+1) × r / (2 × 12)]

5. **Provident Fund (PF)**
   - Via: `UserAsset.java`
   - Type: EntityType.PF
   - Features: Current value tracking

6. **Lendings**
   - Service: `LendingService.java`
   - Entity: `LendingRecord.java`
   - Features:
     - Amount lent tracking
     - Outstanding amount
     - Due date monitoring
     - Repayment tracking
     - Status: ACTIVE, PARTIALLY_PAID, PAID, DEFAULTED

7. **Stock Portfolio**
   - Service: `PortfolioReadService.java`
   - Features:
     - Real-time valuation
     - Holdings tracking
     - Current market value

8. **Mutual Funds**
   - Service: `MutualFundService.java`
   - Features: NAV-based valuation

9. **ETFs**
   - Service: `EtfService.java`
   - Features: Market value tracking

10. **Gold & Other Assets**
    - Via: `UserAsset.java`
    - Types: GOLD, REAL_ESTATE, VEHICLE, JEWELRY, OTHER
    - Features: Current value tracking

**Liabilities Calculation:**

1. **Credit Cards**
   - Via: `UserLiability.java`
   - Type: EntityType.CREDIT_CARD
   - Features: Outstanding amount tracking

2. **Loans**
   - Service: `LoanService.java`
   - Entity: `Loan.java`
   - Features:
     - Principal tracking
     - Outstanding amount
     - Interest calculation
     - EMI tracking
     - Multiple loan types: HOME, CAR, PERSONAL, EDUCATION, BUSINESS

3. **Tax Liabilities**
   - Service: `TaxService.java`
   - Method: `getOutstandingTaxLiability()`
   - Features: Calculated tax obligations

**Dashboard Support (Complete):**

- ✅ **Total Networth**: Assets - Liabilities
- ✅ **Total Assets**: Sum of all asset categories
- ✅ **Total Liabilities**: Sum of all liability types
- ✅ **Networth Pre-Tax**: Before tax obligations
- ✅ **Networth Post-Tax**: After tax liabilities
- ✅ **Asset Breakdown by Entity Type**: Map<EntityType, BigDecimal>
- ✅ **Liability Breakdown by Type**: Map<EntityType, BigDecimal>
- ✅ **Portfolio Value**: Stock holdings value
- ✅ **Savings Value**: FD + RD + Savings accounts
- ✅ **Outstanding Loans**: Total loan liabilities
- ✅ **Outstanding Lendings**: Money lent to others
- ✅ **Outstanding Tax**: Tax liabilities

**API Endpoint:**
```
GET /api/v1/net-worth/{userId}
```

**Response DTO:**
```json
{
  "totalAssets": 950000.00,
  "totalLiabilities": 350000.00,
  "netWorth": 600000.00,
  "netWorthAfterTax": 550000.00,
  "portfolioValue": 500000.00,
  "savingsValue": 200000.00,
  "outstandingLoans": 300000.00,
  "outstandingTaxLiability": 50000.00,
  "outstandingLendings": 50000.00,
  "assetBreakdown": {
    "STOCK": 500000.00,
    "SAVINGS": 80000.00,
    "FD": 110000.00,
    "RD": 60000.00,
    "GOLD": 200000.00
  },
  "liabilityBreakdown": {
    "LOAN": 300000.00,
    "CREDIT_CARD": 50000.00
  }
}
```

#### Database Status:
- ✅ Savings accounts table (V4__Savings_Account.sql)
- ✅ Fixed deposits table (V7__FD_RD_Tables.sql)
- ✅ Recurring deposits table (V7__FD_RD_Tables.sql)
- ✅ Loans table (V8__Loans_and_Insurance.sql)
- ✅ Lending table (V13__Lending_Money.sql)
- ✅ User assets table (V3__NetWorth_Tables.sql)
- ✅ User liabilities table (V3__NetWorth_Tables.sql)
- ✅ Portfolio holdings table (V1__Initial_Schema.sql)

#### Testing:
- ✅ Unit tests: `NetWorthReadServiceImplTest.java`
- ✅ Test coverage: Comprehensive
- ✅ Edge cases: No portfolio, service exceptions

---

### 5. INSIGHTS SYSTEM (90% Complete)

#### ✅ Implemented Components:

**Monthly Spending Report:**
- **Implementation**
  - DTO: `BudgetReportDTO.java`
  - Service: `BudgetService.getMonthlyReport()`
  - Endpoint: GET `/api/v1/budget/report/{userId}`
  
- **Features**
  - Total income, expenses, savings
  - Category-wise breakdown
  - Budget vs actual comparison
  - Recent transactions list
  - Month-over-month comparison

**Category Spending Analysis:**
- **Features**
  - Category breakdown: Map<ExpenseCategory, CategorySummary>
  - Per-category spending totals
  - Per-category budget limits
  - Remaining budget per category
  - Percentage used calculation
  - Top spending categories

**Budget Exceeded Alerts:**
- **Implementation**
  - Service: `AlertServiceImpl.java`
  - Alert Types:
    - BUDGET_WARNING (75%)
    - BUDGET_CRITICAL (90%)
    - BUDGET_EXCEEDED (100%)
  
- **Features**
  - Real-time alert generation
  - Category-specific alerts
  - Severity classification
  - Automatic notification delivery
  - Alert history tracking

**Spending Trend Tracking:**
- **Features**
  - Historical spending data
  - Month-over-month trends
  - Category trend analysis
  - Visual-ready data format

**Cash Flow Insights:**
- **Implementation**
  - DTO: `CashFlowDTO.java`
  - Service: `BudgetService.getCashFlowAnalysis()`
  - Endpoint: GET `/api/v1/budget/cashflow/{userId}`
  
- **Features**
  - Income vs expense trends (6 months)
  - Savings rate calculation
  - Income stability metrics
  - Monthly breakdown
  - Net cash flow tracking
  - Average monthly income/expense

**Variance Analysis:**
- **Implementation**
  - DTO: `BudgetVarianceAnalysis.java`
  - Features:
    - Total budget vs total spent
    - Variance amount
    - Variance percentage
    - Overall status (UNDER_BUDGET, ON_TRACK, OVER_BUDGET)
    - Category-wise variance

**Additional Insights:**
- Top categories by spending
- Recent expenses list
- Recent incomes list
- Budget utilization percentage
- Savings vs spending ratio

#### ❌ Minor Gaps:

1. **Predictive Analytics**
   - Status: Not implemented
   - Recommended: Forecast next month spending based on trends

2. **Anomaly Detection**
   - Status: Not implemented
   - Recommended: Flag unusual spending patterns

#### Database Status:
- ✅ All required tables exist
- ✅ Historical data available
- ✅ Efficient indexing for queries

---

### 6. REMINDER SYSTEM (40% Complete)

#### ✅ Implemented Components:

**1. Lending Repayment Reminders (COMPLETE):**
- **Scheduler**
  - Location: `LendingDueDateScheduler.java`
  - Schedule: Daily at 10:00 AM
  - Job: LENDING_DUE_DATE_CHECK
  
- **Features**
  - Overdue lending detection
  - Due-today lending alerts
  - Automatic notification generation
  - Notification channels: IN_APP, EMAIL
  - Notification types: LENDING_OVERDUE, LENDING_DUE_TODAY
  
- **Logic**
  ```java
  // Check overdue: Status != PAID AND DueDate < Today
  // Check due today: Status != PAID AND DueDate == Today
  ```

**2. Subscription Renewal Reminders (COMPLETE):**
- **Scheduler**
  - Location: `SubscriptionReminderScheduler.java`
  - Features:
    - Configurable reminder days before renewal
    - Subscription tracking
    - Renewal date calculation
  
- **Database**
  - Table: `subscriptions`
  - Fields: `reminder_days_before`, `next_renewal_date`

**3. Investment Recurring Transaction Reminders (PARTIAL):**
- **Implementation**
  - Entity: `RecurringTransaction.java`
  - Fields:
    - `sendReminder`: Boolean flag
    - `reminderDaysBefore`: Integer (days before execution)
  
- **Status**: Structure exists but reminder delivery not fully implemented

**4. Notification Delivery System (COMPLETE):**
- **Service**
  - Location: `NotificationService.java`
  - Features:
    - Multi-channel delivery
    - WebSocket real-time notifications
    - Email notifications
    - Notification history
    - Read/unread tracking
    - Metadata support
  
- **Notification Channels**
  - ✅ IN_APP (WebSocket)
  - ✅ EMAIL
  - ⚠️ SMS (Defined but not implemented)
  - ❌ PUSH (Not implemented)

- **Notification Types**
  - LENDING_OVERDUE
  - LENDING_DUE_TODAY
  - BUDGET_WARNING
  - BUDGET_CRITICAL
  - BUDGET_EXCEEDED
  - SUBSCRIPTION_RENEWAL
  - (More types available)

#### ❌ Missing Components:

**1. Bill Reminders** ⚠️ HIGH PRIORITY
- **Status**: NOT FOUND - No implementation
- **Impact**: Core Phase-1 requirement

**Required Components:**
```java
@Entity
public class Bill {
    private Long id;
    private Long userId;
    private String billName;
    private String category; // ELECTRICITY, WATER, PHONE, INTERNET, RENT
    private BigDecimal amount;
    private LocalDate dueDate;
    private Integer dueDayOfMonth; // e.g., 5th of every month
    private String frequency; // MONTHLY, QUARTERLY, ANNUAL
    private String status; // PENDING, PAID, OVERDUE
    private LocalDate lastPaidDate;
    private Boolean autoReminder;
    private Integer reminderDaysBefore;
}

@Component
public class BillReminderScheduler {
    @Scheduled(cron = "0 0 9 * * *") // 9 AM daily
    public void checkBillReminders() {
        // Check bills due in next N days
        // Send reminders
    }
}
```

**Database Required:**
```sql
CREATE TABLE bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bill_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2),
    due_day_of_month INT NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    last_paid_date DATE,
    auto_reminder BOOLEAN DEFAULT TRUE,
    reminder_days_before INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bill_payment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_id BIGINT NOT NULL,
    amount_paid DECIMAL(15,2) NOT NULL,
    paid_date DATE NOT NULL,
    payment_method VARCHAR(50),
    notes TEXT,
    FOREIGN KEY (bill_id) REFERENCES bills(id)
);
```

**2. EMI Reminders** ⚠️ HIGH PRIORITY
- **Status**: NOT FOUND - No dedicated scheduler

**Current Status:**
- Loan entity exists but no EMI reminder scheduler
- EMI calculation logic exists in loan service

**Required Components:**
```java
@Component
public class EMIReminderScheduler {
    @Scheduled(cron = "0 0 9 * * *") // 9 AM daily
    public void checkEMIReminders() {
        LocalDate today = LocalDate.now();
        
        // Find loans with EMI due today or in next N days
        List<Loan> upcomingEMIs = loanService.getLoansWithEMIDue(today, 3);
        
        for (Loan loan : upcomingEMIs) {
            String title = "EMI Reminder: " + loan.getLoanType();
            String message = String.format(
                "Your EMI of ₹%.2f for %s loan is due on %s. " +
                "Outstanding: ₹%.2f",
                loan.getEmiAmount(),
                loan.getLoanType(),
                loan.getNextEmiDate(),
                loan.getOutstandingAmount()
            );
            
            notificationService.sendNotification(
                loan.getUserId(),
                title,
                message,
                NotificationType.EMI_REMINDER,
                AlertChannel.IN_APP
            );
        }
    }
}
```

**Database Enhancement Required:**
```sql
ALTER TABLE loans ADD COLUMN next_emi_date DATE;
ALTER TABLE loans ADD COLUMN emi_day_of_month INT;
ALTER TABLE loans ADD COLUMN auto_reminder BOOLEAN DEFAULT TRUE;
ALTER TABLE loans ADD COLUMN reminder_days_before INT DEFAULT 3;

CREATE TABLE emi_payment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    emi_amount DECIMAL(15,2) NOT NULL,
    principal_component DECIMAL(15,2),
    interest_component DECIMAL(15,2),
    payment_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20), -- ON_TIME, LATE, PARTIAL
    FOREIGN KEY (loan_id) REFERENCES loans(id)
);
```

**3. Push Notifications** ⚠️ MEDIUM PRIORITY
- **Status**: Not implemented
- **Current**: Only in-app (WebSocket) and email available

**Required Components:**
- Firebase Cloud Messaging (FCM) integration
- Apple Push Notification Service (APNs) integration
- Device token management
- Push notification templates
- Silent/background notifications

**4. SMS Notifications** ⚠️ LOW PRIORITY
- **Status**: Defined in `AlertChannel.SMS` but not implemented
- **Required**: SMS gateway integration (Twilio/MSG91)

#### Database Status:
- ✅ User notifications table exists
- ✅ Alert rules table exists (V56__creating_alert_rules_table.sql)
- ✅ Recurring transactions with reminder fields
- ✅ Subscription reminders configured
- ❌ Bills table - MISSING
- ❌ Bill payment history - MISSING
- ❌ EMI payment history - MISSING
- ❌ Device tokens table (for push) - MISSING

#### Scheduler Jobs Status:
- ✅ LENDING_DUE_DATE_CHECK (Daily 10 AM)
- ✅ BUDGET_ALERTS (Daily 9 PM)
- ✅ Subscription renewal reminders
- ❌ BILL_REMINDER - MISSING
- ❌ EMI_REMINDER - MISSING

---

## ⚠️ PARTIALLY IMPLEMENTED FEATURES

### 1. UPI Transactions (40% Complete)

**Location:** `src/main/java/com/upi`

#### ✅ Implemented:
- **Transaction Model**
  - Entity: `Transaction.java`
  - Fields: transactionId, sender/receiver UPI IDs, amount, status, type
  - Database: V57__Create_UPI_Module_Tables.sql

- **UPI ID Management**
  - Entity: `UpiId.java`
  - Features: UPI ID creation, merchant flag
  - Service: `UpiIdService.java`

- **Bank Account Linking**
  - Entity: `BankAccount.java`
  - Features: Account balance, IFSC, account number
  - Service: `BankAccountService.java`

- **PIN Verification**
  - Entity: `UpiPin.java`
  - Service: `UpiPinService.java`
  - Security: BCrypt hashed PINs

- **Transaction Types**
  - P2P (Peer-to-Peer)
  - P2M (Peer-to-Merchant) with MDR (1.5%)
  - Collect requests

- **Transaction History**
  - Repository: `TransactionRepository.java`
  - Query support for transaction history

#### ❌ Missing:
- **Integration with actual UPI Payment Gateway**
  - NPCI integration
  - Bank API integration
  - Real-time settlement

- **SMS Parsing for UPI Transactions**
  - No automatic capture from UPI SMS alerts
  
- **Automatic Expense Sync from UPI**
  - UPI transactions not auto-creating expenses
  
- **QR Code Payments**
  - QR code generation service exists but incomplete
  
- **Real-time Transaction Notifications**
  - No webhook handling for transaction status updates

### 2. Transaction Categorization (60% Complete)

#### ✅ Implemented:
- **Predefined Categories**
  - Enum: `ExpenseCategory.java`
  - 12 standard categories
  
- **Custom Category Creation**
  - Full CRUD operations
  - Icon/color support
  
- **Manual Category Assignment**
  - User can select category during expense creation
  
- **Category-based Budgeting**
  - Per-category limits
  - Category spending tracking

#### ❌ Missing:
- **Automatic Categorization from SMS**
  - No SMS-to-category mapping
  
- **ML-based Smart Categorization**
  - No learning algorithm
  - No merchant-to-category mapping
  
- **Category Suggestions**
  - No predictive suggestions based on merchant/description
  - No learning from user corrections

**Recommendation:**
```java
@Service
public class SmartCategorizationService {
    
    public ExpenseCategory suggestCategory(String description, String merchant) {
        // Rule-based categorization
        // Merchant mapping
        // ML model integration
    }
    
    public void learnFromUserCorrection(Long expenseId, 
                                        ExpenseCategory suggestedCategory,
                                        ExpenseCategory actualCategory) {
        // Update learning model
    }
}
```

---

## ❌ MISSING FEATURES (Critical)

### 1. SMS Parsing for Transactions ⚠️ CRITICAL PRIORITY

**Status:** NOT FOUND - No implementation  
**Impact:** Core Phase-1 feature completely missing  
**Effort:** HIGH (2-3 weeks)

#### Required Architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    SMS Transaction Pipeline                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  1. SMS Receiver (Android/iOS)                               │
│     ↓ Permissions: READ_SMS, RECEIVE_SMS                     │
│                                                               │
│  2. SMS Parser Service                                       │
│     ↓ Pattern matching, regex extraction                     │
│                                                               │
│  3. Transaction Extractor                                    │
│     ↓ Amount, merchant, date, type (debit/credit)           │
│                                                               │
│  4. Auto-Categorizer                                         │
│     ↓ Merchant mapping, ML categorization                    │
│                                                               │
│  5. Expense Creator                                          │
│     ↓ Create expense with source=SMS_PARSED                 │
│                                                               │
│  6. Notification                                             │
│     ↓ Notify user of new transaction                         │
│                                                               │
│  7. Review Queue (if confidence < threshold)                 │
│     ↓ Manual review for uncertain transactions               │
└─────────────────────────────────────────────────────────────┘
```

#### Required Components:

**1. SMS Parser Service**
```java
@Service
public class SMSParserService {
    
    public ParsedTransaction parseSMS(String smsText, String sender) {
        // Match against bank patterns
        BankSmsPattern pattern = getBankPattern(sender);
        
        return ParsedTransaction.builder()
            .amount(extractAmount(smsText, pattern))
            .merchant(extractMerchant(smsText, pattern))
            .transactionType(extractType(smsText, pattern))
            .transactionDate(extractDate(smsText, pattern))
            .accountLast4(extractAccount(smsText, pattern))
            .confidence(calculateConfidence())
            .build();
    }
    
    private BigDecimal extractAmount(String sms, BankSmsPattern pattern) {
        Pattern p = Pattern.compile(pattern.getAmountRegex());
        Matcher m = p.matcher(sms);
        if (m.find()) {
            String amount = m.group(1).replaceAll("[^0-9.]", "");
            return new BigDecimal(amount);
        }
        return null;
    }
}
```

**2. Bank SMS Patterns Configuration**
```java
@Entity
@Table(name = "bank_sms_patterns")
public class BankSmsPattern {
    private Long id;
    private String bankName;
    private String senderId; // e.g., "HDFCBK", "ICICIB"
    private String smsPattern;
    private String amountRegex; // e.g., "Rs\\.?\\s*([\\d,]+\\.?\\d*)"
    private String merchantRegex; // e.g., "at ([A-Z\\s]+)"
    private String dateRegex;
    private String typeRegex; // "debited|credited"
    private Boolean isActive;
    
    // Sample patterns
    // HDFC: "Rs 500.00 debited from A/c **1234 at AMAZON on 12-Mar-26"
    // ICICI: "Your A/c XX5678 is debited with Rs.1000.00 on 12-MAR-26 at FLIPKART"
    // SBI: "Dear Customer, Rs.750.00 debited from A/c X1234 for UPI/PAYTM"
}
```

**3. Database Schema**
```sql
CREATE TABLE sms_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    raw_sms_text TEXT NOT NULL,
    sender VARCHAR(50) NOT NULL,
    received_at TIMESTAMP NOT NULL,
    parsed_amount DECIMAL(15,2),
    parsed_merchant VARCHAR(255),
    parsed_type VARCHAR(20) COMMENT 'DEBIT/CREDIT',
    account_last4 VARCHAR(4),
    transaction_date TIMESTAMP,
    is_parsed BOOLEAN DEFAULT FALSE,
    parse_confidence DECIMAL(5,2) COMMENT 'Confidence score 0-100',
    created_expense_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSED, FAILED, IGNORED',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_status (user_id, status),
    INDEX idx_sender (sender),
    INDEX idx_received_at (received_at),
    FOREIGN KEY (created_expense_id) REFERENCES expenses(id)
);

CREATE TABLE bank_sms_patterns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_name VARCHAR(100) NOT NULL,
    sender_id VARCHAR(50) NOT NULL,
    sms_pattern TEXT NOT NULL,
    amount_regex VARCHAR(255) NOT NULL,
    merchant_regex VARCHAR(255),
    date_regex VARCHAR(255),
    type_regex VARCHAR(255) NOT NULL,
    account_regex VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0 COMMENT 'Higher priority patterns checked first',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_sender (sender_id),
    INDEX idx_active (is_active)
);

-- Seed common bank patterns
INSERT INTO bank_sms_patterns (bank_name, sender_id, sms_pattern, amount_regex, merchant_regex, type_regex) VALUES
('HDFC Bank', 'HDFCBK', '%', 'Rs\\.?\\s*([\\d,]+\\.?\\d*)', 'at\\s+([A-Z0-9\\s]+)', 'debited|credited'),
('ICICI Bank', 'ICICIB', '%', 'Rs\\.?\\s*([\\d,]+\\.?\\d*)', '(at|to)\\s+([A-Z0-9\\s]+)', 'debited|credited'),
('SBI', 'SBIIN', '%', 'Rs\\.?\\s*([\\d,]+\\.?\\d*)', 'for\\s+([A-Z0-9\\s/]+)', 'debited|credited'),
('Axis Bank', 'AXISBK', '%', 'INR\\s*([\\d,]+\\.?\\d*)', 'at\\s+([A-Z0-9\\s]+)', 'debited|credited');
```

**4. Transaction Source Tracking**
```java
// Add to Expense entity
public enum TransactionSource {
    MANUAL,          // User-entered
    SMS_PARSED,      // From SMS
    UPI_SYNC,        // From UPI
    BANK_API,        // From bank API
    ACCOUNT_AGGREGATOR // From AA framework
}

@Entity
public class Expense {
    // ... existing fields
    
    @Enumerated(EnumType.STRING)
    private TransactionSource source;
    
    private Long smsTransactionId; // Link to SMS if parsed from SMS
    
    private BigDecimal parseConfidence; // Confidence score for auto-parsed
}
```

**5. Processing Flow**
```java
@Service
public class SMSTransactionProcessor {
    
    @Autowired
    private SMSParserService parserService;
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private SmartCategorizationService categorizationService;
    
    public void processSMS(String smsText, String sender, Long userId) {
        // 1. Save raw SMS
        SMSTransaction smsTransaction = saveSMS(smsText, sender, userId);
        
        // 2. Parse SMS
        ParsedTransaction parsed = parserService.parseSMS(smsText, sender);
        
        if (parsed == null || parsed.getAmount() == null) {
            smsTransaction.setStatus("FAILED");
            smsTransaction.setErrorMessage("Unable to parse SMS");
            return;
        }
        
        // 3. Update parsed fields
        smsTransaction.setParsedAmount(parsed.getAmount());
        smsTransaction.setParsedMerchant(parsed.getMerchant());
        smsTransaction.setParsedType(parsed.getType());
        smsTransaction.setParseConfidence(parsed.getConfidence());
        smsTransaction.setIsParsed(true);
        
        // 4. Auto-categorize
        ExpenseCategory category = categorizationService
            .suggestCategory(parsed.getMerchant(), parsed.getAmount());
        
        // 5. Create expense if DEBIT and confidence > 70%
        if ("DEBIT".equals(parsed.getType()) && parsed.getConfidence() > 70) {
            Expense expense = Expense.builder()
                .userId(userId)
                .amount(parsed.getAmount())
                .category(category)
                .description(parsed.getMerchant())
                .expenseDate(parsed.getTransactionDate())
                .source(TransactionSource.SMS_PARSED)
                .smsTransactionId(smsTransaction.getId())
                .parseConfidence(parsed.getConfidence())
                .build();
            
            Expense created = expenseService.addExpense(expense);
            smsTransaction.setCreatedExpenseId(created.getId());
            smsTransaction.setStatus("PROCESSED");
            
            // 6. Notify user
            notifyUser(userId, created);
        } else {
            // Low confidence - add to review queue
            smsTransaction.setStatus("PENDING_REVIEW");
        }
    }
}
```

#### Implementation Checklist:
- [ ] Create `sms_transactions` table migration
- [ ] Create `bank_sms_patterns` table migration
- [ ] Implement `SMSParserService`
- [ ] Add `TransactionSource` enum to `Expense`
- [ ] Implement `SMSTransactionProcessor`
- [ ] Create SMS receiver (Android/iOS native code)
- [ ] Implement review queue for low-confidence transactions
- [ ] Add SMS transaction history API
- [ ] Create admin panel for bank pattern management
- [ ] Add unit tests for SMS parsing patterns
- [ ] Document supported bank formats

---

### 2. Phone OTP Verification ⚠️ CRITICAL PRIORITY

**Status:** Documentation exists but NO implementation  
**Documentation:** `docs/OTP_MODULE_IMPLEMENTATION.md`  
**Impact:** Security and user verification requirement  
**Effort:** MEDIUM (1-2 weeks)

#### Current Status Analysis:
- ✅ Complete documentation exists
- ✅ API design documented
- ✅ Frontend component design documented
- ❌ Backend implementation MISSING
- ❌ Database tables MISSING
- ❌ SMS gateway NOT integrated

#### Required Components:

**1. OTP Session Entity**
```java
@Entity
@Table(name = "otp_sessions")
public class OtpSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String phoneNumber; // +91XXXXXXXXXX format
    
    @Column(nullable = false)
    private String otpHash; // BCrypt hashed OTP
    
    @Column(nullable = false)
    private String sessionId; // UUID for session tracking
    
    @Column(nullable = false)
    private LocalDateTime expiresAt; // 5 minutes from creation
    
    @Column(nullable = false)
    private Integer attemptCount = 0; // Max 3 attempts
    
    @Column(nullable = false)
    private Boolean isVerified = false;
    
    @Column(nullable = false)
    private Boolean isExpired = false;
    
    private String ipAddress; // For security tracking
    
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
}
```

**2. OTP Service**
```java
@Service
public class OtpService {
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    
    @Autowired
    private OtpSessionRepository otpSessionRepository;
    
    @Autowired
    private SmsGatewayService smsGatewayService;
    
    public OtpResponse sendOtp(String phoneNumber, String ipAddress) {
        // 1. Validate phone number
        validatePhoneNumber(phoneNumber);
        
        // 2. Invalidate any existing active sessions
        invalidateExistingSessions(phoneNumber);
        
        // 3. Generate 6-digit OTP
        String otp = generateOtp();
        
        // 4. Create session
        OtpSession session = OtpSession.builder()
            .phoneNumber(normalizePhoneNumber(phoneNumber))
            .otpHash(hashOtp(otp))
            .sessionId(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .ipAddress(ipAddress)
            .build();
        
        otpSessionRepository.save(session);
        
        // 5. Send OTP via SMS
        smsGatewayService.sendOtp(phoneNumber, otp);
        
        // 6. Return response (DO NOT include OTP in response)
        return OtpResponse.builder()
            .sessionId(session.getSessionId())
            .message("OTP sent successfully")
            .expiresIn(OTP_EXPIRY_MINUTES * 60) // seconds
            .build();
    }
    
    public boolean verifyOtp(String phoneNumber, String otp, String sessionId) {
        // 1. Find session
        OtpSession session = otpSessionRepository
            .findBySessionIdAndPhoneNumber(sessionId, phoneNumber)
            .orElseThrow(() -> new OtpException("Invalid session"));
        
        // 2. Check if already verified
        if (session.getIsVerified()) {
            throw new OtpException("OTP already verified");
        }
        
        // 3. Check if expired
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            session.setIsExpired(true);
            otpSessionRepository.save(session);
            throw new OtpException("OTP expired");
        }
        
        // 4. Check attempt count
        if (session.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new OtpException("Maximum attempts exceeded");
        }
        
        // 5. Verify OTP
        session.setAttemptCount(session.getAttemptCount() + 1);
        
        if (!BCrypt.checkpw(otp, session.getOtpHash())) {
            otpSessionRepository.save(session);
            int remaining = MAX_ATTEMPTS - session.getAttemptCount();
            throw new OtpException("Invalid OTP. " + remaining + " attempts remaining.");
        }
        
        // 6. Mark as verified
        session.setIsVerified(true);
        session.setVerifiedAt(LocalDateTime.now());
        otpSessionRepository.save(session);
        
        return true;
    }
    
    private String generateOtp() {
        Random random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 100000 to 999999
        return String.valueOf(otp);
    }
    
    private String hashOtp(String otp) {
        return BCrypt.hashpw(otp, BCrypt.gensalt(10));
    }
    
    private String normalizePhoneNumber(String phone) {
        phone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        if (!phone.startsWith("+")) {
            if (phone.startsWith("91")) {
                phone = "+" + phone;
            } else {
                phone = "+91" + phone;
            }
        }
        return phone;
    }
    
    private void validatePhoneNumber(String phone) {
        String normalized = normalizePhoneNumber(phone);
        if (!normalized.matches("\\+91[6-9]\\d{9}")) {
            throw new OtpException("Invalid Indian mobile number");
        }
    }
}
```

**3. SMS Gateway Service**
```java
@Service
public class SmsGatewayService {
    
    @Value("${sms.gateway.provider}") // TWILIO or MSG91
    private String provider;
    
    @Value("${sms.gateway.api.key}")
    private String apiKey;
    
    @Value("${sms.gateway.sender.id}")
    private String senderId;
    
    public void sendOtp(String phoneNumber, String otp) {
        String message = String.format(
            "Your PI System OTP is %s. Valid for 5 minutes. Do not share with anyone.",
            otp
        );
        
        if ("TWILIO".equals(provider)) {
            sendViaTwilio(phoneNumber, message);
        } else if ("MSG91".equals(provider)) {
            sendViaMsg91(phoneNumber, message);
        } else {
            // Development mode - log OTP
            log.info("📱 OTP for {}: {}", phoneNumber, otp);
        }
    }
    
    private void sendViaTwilio(String phone, String message) {
        // Twilio implementation
        Twilio.init(accountSid, authToken);
        Message.creator(
            new PhoneNumber(phone),
            new PhoneNumber(twilioPhoneNumber),
            message
        ).create();
    }
    
    private void sendViaMsg91(String phone, String message) {
        // MSG91 implementation
        // Use MSG91 REST API
    }
}
```

**4. Database Migration**
```sql
-- V65__Create_OTP_Sessions_Table.sql

CREATE TABLE otp_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number VARCHAR(20) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    attempt_count INT DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    is_expired BOOLEAN DEFAULT FALSE,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    
    INDEX idx_phone (phone_number),
    INDEX idx_session (session_id),
    INDEX idx_expires (expires_at),
    INDEX idx_phone_verified (phone_number, is_verified)
);

-- Add phone fields to users table
ALTER TABLE users 
    ADD COLUMN phone_number VARCHAR(20) UNIQUE,
    ADD COLUMN phone_verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN phone_verified_at TIMESTAMP NULL;
```

**5. OTP Controller**
```java
@RestController
@RequestMapping("/api/v1/auth/otp")
public class OtpController {
    
    @Autowired
    private OtpService otpService;
    
    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(
            @Valid @RequestBody OtpRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        OtpResponse response = otpService.sendOtp(
            request.getPhoneNumber(), 
            ipAddress
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        
        boolean verified = otpService.verifyOtp(
            request.getPhoneNumber(),
            request.getOtp(),
            request.getSessionId()
        );
        
        if (verified) {
            // Generate JWT token or return success
            return ResponseEntity.ok()
                .body(Map.of("message", "OTP verified successfully"));
        }
        
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Invalid OTP"));
    }
    
    @PostMapping("/resend")
    public ResponseEntity<OtpResponse> resendOtp(
            @Valid @RequestBody OtpRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        OtpResponse response = otpService.sendOtp(
            request.getPhoneNumber(),
            ipAddress
        );
        
        return ResponseEntity.ok(response);
    }
}
```

**6. DTOs**
```java
@Data
public class OtpRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+91)?[6-9]\\d{9}$", 
             message = "Invalid Indian mobile number")
    private String phoneNumber;
}

@Data
public class OtpVerifyRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
}

@Data
@Builder
public class OtpResponse {
    private String sessionId;
    private String message;
    private Integer expiresIn; // seconds
}
```

#### Implementation Checklist:
- [ ] Create `otp_sessions` table migration (V65)
- [ ] Add phone fields to `users` table
- [ ] Implement `OtpSession` entity
- [ ] Implement `OtpService`
- [ ] Implement `SmsGatewayService` (Twilio/MSG91)
- [ ] Create `OtpController`
- [ ] Create DTOs (OtpRequest, OtpVerifyRequest, OtpResponse)
- [ ] Add rate limiting (prevent OTP spam)
- [ ] Add IP-based fraud detection
- [ ] Create frontend OTP component
- [ ] Add unit tests
- [ ] Add integration tests
- [ ] Document API endpoints

#### Configuration Required:
```yaml
# application.yml
sms:
  gateway:
    provider: MSG91  # or TWILIO
    api:
      key: ${SMS_API_KEY}
    sender:
      id: PISYST
      
otp:
  length: 6
  expiry:
    minutes: 5
  max:
    attempts: 3
```

---

### 3. Unknown Transaction Review System ⚠️ HIGH PRIORITY

**Status:** NOT FOUND  
**Impact:** User experience and data quality  
**Effort:** MEDIUM (1-2 weeks)

#### Problem Statement:
When transactions are parsed from SMS or synced from external sources, there may be:
- Low confidence categorization
- Unclear merchants
- Duplicate transactions
- Unrecognized patterns

Users need a workflow to review and correct these transactions.

#### Required Components:

**1. Transaction Status Management**
```java
// Add to Expense entity
public enum TransactionStatus {
    CATEGORIZED,      // Fully categorized and confirmed
    PENDING_REVIEW,   // Needs user review
    UNKNOWN,          // Unable to categorize
    DUPLICATE,        // Potential duplicate
    IGNORED          // User marked as ignore
}

@Entity
public class Expense {
    // ... existing fields
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.CATEGORIZED;
    
    private BigDecimal categoryConfidence; // 0-100 confidence score
    
    private String suggestedCategory; // AI-suggested category if different from assigned
}
```

**2. Review Queue Entity**
```java
@Entity
@Table(name = "transaction_review_queue")
public class TransactionReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long expenseId;
    
    @Column(nullable = false)
    private Long userId;
    
    private String suggestedCategory;
    
    private BigDecimal confidenceScore;
    
    @Enumerated(EnumType.STRING)
    private ReviewReason reason; // LOW_CONFIDENCE, DUPLICATE, UNKNOWN_MERCHANT, USER_FLAGGED
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING; // PENDING, APPROVED, REJECTED, MODIFIED
    
    private Long reviewedBy; // User ID who reviewed
    
    private LocalDateTime reviewedAt;
    
    private String reviewNotes;
    
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", insertable = false, updatable = false)
    private Expense expense;
}

public enum ReviewReason {
    LOW_CONFIDENCE,      // AI confidence < 70%
    DUPLICATE,           // Potential duplicate detected
    UNKNOWN_MERCHANT,    // Merchant not recognized
    USER_FLAGGED,        // User manually flagged
    ANOMALY             // Unusual amount/pattern
}

public enum ReviewStatus {
    PENDING,    // Awaiting review
    APPROVED,   // User approved suggested category
    REJECTED,   // User rejected suggestion
    MODIFIED,   // User changed category
    IGNORED     // User marked as ignore
}
```

**3. Review Service**
```java
@Service
public class TransactionReviewService {
    
    @Autowired
    private TransactionReviewRepository reviewRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private SmartCategorizationService categorizationService;
    
    /**
     * Add transaction to review queue
     */
    public TransactionReview addToReviewQueue(Expense expense, 
                                               ReviewReason reason,
                                               BigDecimal confidence) {
        TransactionReview review = TransactionReview.builder()
            .expenseId(expense.getId())
            .userId(expense.getUserId())
            .suggestedCategory(expense.getCategory() != null ? 
                expense.getCategory().name() : null)
            .confidenceScore(confidence)
            .reason(reason)
            .reviewStatus(ReviewStatus.PENDING)
            .build();
        
        // Update expense status
        expense.setStatus(TransactionStatus.PENDING_REVIEW);
        expenseRepository.save(expense);
        
        return reviewRepository.save(review);
    }
    
    /**
     * Get pending reviews for user
     */
    public List<TransactionReviewDTO> getPendingReviews(Long userId, 
                                                         Pageable pageable) {
        return reviewRepository
            .findByUserIdAndReviewStatus(userId, ReviewStatus.PENDING, pageable)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Approve suggested category
     */
    @Transactional
    public void approveReview(Long reviewId, Long userId) {
        TransactionReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        validateUserAccess(review, userId);
        
        review.setReviewStatus(ReviewStatus.APPROVED);
        review.setReviewedBy(userId);
        review.setReviewedAt(LocalDateTime.now());
        reviewRepository.save(review);
        
        // Update expense
        Expense expense = expenseRepository.findById(review.getExpenseId())
            .orElseThrow();
        expense.setStatus(TransactionStatus.CATEGORIZED);
        expenseRepository.save(expense);
        
        // Learn from approval
        categorizationService.learnFromUserChoice(expense, true);
    }
    
    /**
     * Modify category
     */
    @Transactional
    public void modifyReview(Long reviewId, Long userId, 
                             ExpenseCategory newCategory, String notes) {
        TransactionReview review = reviewRepository.findById(reviewId)
            .orElseThrow();
        
        validateUserAccess(review, userId);
        
        review.setReviewStatus(ReviewStatus.MODIFIED);
        review.setReviewedBy(userId);
        review.setReviewedAt(LocalDateTime.now());
        review.setReviewNotes(notes);
        reviewRepository.save(review);
        
        // Update expense
        Expense expense = expenseRepository.findById(review.getExpenseId())
            .orElseThrow();
        expense.setCategory(newCategory);
        expense.setStatus(TransactionStatus.CATEGORIZED);
        expenseRepository.save(expense);
        
        // Learn from correction
        categorizationService.learnFromUserCorrection(
            expense, 
            ExpenseCategory.valueOf(review.getSuggestedCategory()),
            newCategory
        );
    }
    
    /**
     * Bulk approve reviews
     */
    @Transactional
    public void bulkApprove(List<Long> reviewIds, Long userId) {
        reviewIds.forEach(id -> approveReview(id, userId));
    }
    
    /**
     * Detect duplicate transactions
     */
    public void checkForDuplicates(Expense expense) {
        // Find similar transactions (same amount, similar date)
        List<Expense> similar = expenseRepository.findSimilarTransactions(
            expense.getUserId(),
            expense.getAmount(),
            expense.getExpenseDate().minusDays(1),
            expense.getExpenseDate().plusDays(1)
        );
        
        if (!similar.isEmpty()) {
            addToReviewQueue(expense, ReviewReason.DUPLICATE, BigDecimal.ZERO);
        }
    }
}
```

**4. Review Controller**
```java
@RestController
@RequestMapping("/api/v1/transactions/review")
public class TransactionReviewController {
    
    @Autowired
    private TransactionReviewService reviewService;
    
    @GetMapping("/{userId}/pending")
    public Page<TransactionReviewDTO> getPendingReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return reviewService.getPendingReviews(userId, pageable);
    }
    
    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<?> approveReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId) {
        
        reviewService.approveReview(reviewId, userId);
        return ResponseEntity.ok(Map.of("message", "Review approved"));
    }
    
    @PostMapping("/{reviewId}/modify")
    public ResponseEntity<?> modifyReview(
            @PathVariable Long reviewId,
            @RequestBody ModifyReviewRequest request) {
        
        reviewService.modifyReview(
            reviewId,
            request.getUserId(),
            request.getNewCategory(),
            request.getNotes()
        );
        return ResponseEntity.ok(Map.of("message", "Category updated"));
    }
    
    @PostMapping("/bulk-approve")
    public ResponseEntity<?> bulkApprove(@RequestBody BulkApproveRequest request) {
        reviewService.bulkApprove(request.getReviewIds(), request.getUserId());
        return ResponseEntity.ok(Map.of("message", "Reviews approved"));
    }
    
    @GetMapping("/{userId}/stats")
    public ReviewStatsDTO getReviewStats(@PathVariable Long userId) {
        return reviewService.getReviewStats(userId);
    }
}
```

**5. Database Migration**
```sql
-- V66__Create_Transaction_Review_System.sql

-- Add status to expenses table
ALTER TABLE expenses 
    ADD COLUMN status VARCHAR(20) DEFAULT 'CATEGORIZED',
    ADD COLUMN category_confidence DECIMAL(5,2),
    ADD COLUMN suggested_category VARCHAR(50);

CREATE INDEX idx_expense_status ON expenses(status);
CREATE INDEX idx_expense_user_status ON expenses(user_id, status);

-- Create review queue table
CREATE TABLE transaction_review_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    suggested_category VARCHAR(50),
    confidence_score DECIMAL(5,2),
    reason VARCHAR(30) NOT NULL,
    review_status VARCHAR(20) DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    review_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    INDEX idx_user_status (user_id, review_status),
    INDEX idx_status (review_status),
    INDEX idx_created (created_at)
);

-- Create learning data table for ML
CREATE TABLE categorization_learning (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_id BIGINT NOT NULL,
    merchant_name VARCHAR(255),
    description TEXT,
    amount DECIMAL(15,2),
    suggested_category VARCHAR(50),
    actual_category VARCHAR(50) NOT NULL,
    confidence_score DECIMAL(5,2),
    was_approved BOOLEAN,
    user_id BIGINT NOT NULL,
    learned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (expense_id) REFERENCES expenses(id),
    
    INDEX idx_merchant (merchant_name),
    INDEX idx_user (user_id)
);
```

**6. Smart Categorization Service**
```java
@Service
public class SmartCategorizationService {
    
    @Autowired
    private CategorizationLearningRepository learningRepository;
    
    /**
     * Suggest category based on merchant and description
     */
    public CategorySuggestion suggestCategory(String merchant, 
                                               String description,
                                               BigDecimal amount) {
        // 1. Check exact merchant match from learning data
        List<CategorizationLearning> history = learningRepository
            .findByMerchantNameIgnoreCase(merchant);
        
        if (!history.isEmpty()) {
            Map<String, Long> categoryCount = history.stream()
                .collect(Collectors.groupingBy(
                    CategorizationLearning::getActualCategory,
                    Collectors.counting()
                ));
            
            String mostCommon = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
            
            if (mostCommon != null) {
                BigDecimal confidence = BigDecimal.valueOf(
                    (categoryCount.get(mostCommon) * 100.0) / history.size()
                );
                
                return CategorySuggestion.builder()
                    .category(ExpenseCategory.valueOf(mostCommon))
                    .confidence(confidence)
                    .source("LEARNED")
                    .build();
            }
        }
        
        // 2. Rule-based categorization
        return ruleBasedCategorization(merchant, description, amount);
    }
    
    /**
     * Rule-based categorization
     */
    private CategorySuggestion ruleBasedCategorization(String merchant,
                                                        String description,
                                                        BigDecimal amount) {
        String text = (merchant + " " + description).toLowerCase();
        
        // Food & Dining
        if (text.matches(".*(swiggy|zomato|restaurant|cafe|food|pizza|burger).*")) {
            return CategorySuggestion.builder()
                .category(ExpenseCategory.FOOD)
                .confidence(BigDecimal.valueOf(85))
                .source("RULE_BASED")
                .build();
        }
        
        // Transport
        if (text.matches(".*(uber|ola|rapido|metro|petrol|fuel).*")) {
            return CategorySuggestion.builder()
                .category(ExpenseCategory.TRANSPORT)
                .confidence(BigDecimal.valueOf(85))
                .source("RULE_BASED")
                .build();
        }
        
        // Shopping
        if (text.matches(".*(amazon|flipkart|myntra|ajio|shopping).*")) {
            return CategorySuggestion.builder()
                .category(ExpenseCategory.SHOPPING)
                .confidence(BigDecimal.valueOf(80))
                .source("RULE_BASED")
                .build();
        }
        
        // Entertainment
        if (text.matches(".*(netflix|hotstar|prime|spotify|movie|cinema).*")) {
            return CategorySuggestion.builder()
                .category(ExpenseCategory.ENTERTAINMENT)
                .confidence(BigDecimal.valueOf(85))
                .source("RULE_BASED")
                .build();
        }
        
        // Utilities
        if (text.matches(".*(electricity|water|gas|internet|broadband|recharge).*")) {
            return CategorySuggestion.builder()
                .category(ExpenseCategory.UTILITIES)
                .confidence(BigDecimal.valueOf(85))
                .source("RULE_BASED")
                .build();
        }
        
        // Default: OTHERS with low confidence
        return CategorySuggestion.builder()
            .category(ExpenseCategory.OTHERS)
            .confidence(BigDecimal.valueOf(40))
            .source("DEFAULT")
            .build();
    }
    
    /**
     * Learn from user's category choice
     */
    public void learnFromUserChoice(Expense expense, boolean approved) {
        CategorizationLearning learning = CategorizationLearning.builder()
            .expenseId(expense.getId())
            .merchantName(expense.getDescription())
            .description(expense.getDescription())
            .amount(expense.getAmount())
            .suggestedCategory(expense.getSuggestedCategory())
            .actualCategory(expense.getCategory().name())
            .confidenceScore(expense.getCategoryConfidence())
            .wasApproved(approved)
            .userId(expense.getUserId())
            .build();
        
        learningRepository.save(learning);
    }
    
    /**
     * Learn from user correction
     */
    public void learnFromUserCorrection(Expense expense,
                                        ExpenseCategory suggested,
                                        ExpenseCategory actual) {
        CategorizationLearning learning = CategorizationLearning.builder()
            .expenseId(expense.getId())
            .merchantName(expense.getDescription())
            .description(expense.getDescription())
            .amount(expense.getAmount())
            .suggestedCategory(suggested.name())
            .actualCategory(actual.name())
            .confidenceScore(expense.getCategoryConfidence())
            .wasApproved(false)
            .userId(expense.getUserId())
            .build();
        
        learningRepository.save(learning);
    }
}

@Data
@Builder
public class CategorySuggestion {
    private ExpenseCategory category;
    private BigDecimal confidence; // 0-100
    private String source; // LEARNED, RULE_BASED, DEFAULT, ML_MODEL
}
```

#### Implementation Checklist:
- [ ] Add status fields to `expenses` table
- [ ] Create `transaction_review_queue` table
- [ ] Create `categorization_learning` table
- [ ] Implement `TransactionReview` entity
- [ ] Implement `TransactionReviewService`
- [ ] Implement `SmartCategorizationService`
- [ ] Create review controller and endpoints
- [ ] Auto-add low confidence transactions to queue
- [ ] Implement duplicate detection
- [ ] Create frontend review queue UI
- [ ] Add bulk operations support
- [ ] Implement learning algorithm
- [ ] Add analytics dashboard for categorization accuracy
- [ ] Create unit and integration tests

---

### 4. Transfer Between Accounts ⚠️ HIGH PRIORITY

**Status:** NOT FOUND  
**Impact:** Budget accuracy (transfers counted as expenses)  
**Effort:** MEDIUM (1 week)

#### Problem Statement:
Currently, if a user transfers money from Savings to FD, it would be:
1. Counted as an expense from Savings (incorrect)
2. Not tracked as a transfer

This inflates expense calculations and skews budget reports.

#### Required Implementation:

**1. Transfer Entity**
```java
@Entity
@Table(name = "account_transfers")
public class AccountTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType sourceAccountType;
    
    @Column(nullable = false)
    private Long sourceAccountId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType destinationAccountType;
    
    @Column(nullable = false)
    private Long destinationAccountId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDate transferDate;
    
    @Enumerated(EnumType.STRING)
    private TransferStatus status = TransferStatus.PENDING;
    
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

public enum AccountType {
    SAVINGS,
    FIXED_DEPOSIT,
    RECURRING_DEPOSIT,
    BANK_ACCOUNT,
    CREDIT_CARD,
    LOAN
}

public enum TransferStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
```

**2. Transfer Service**
```java
@Service
public class AccountTransferService {
    
    @Autowired
    private AccountTransferRepository transferRepository;
    
    @Autowired
    private SavingsAccountService savingsService;
    
    @Autowired
    private FixedDepositService fdService;
    
    @Autowired
    private BankAccountService bankAccountService;
    
    @Transactional
    public AccountTransfer createTransfer(TransferRequest request) {
        // 1. Validate accounts belong to user
        validateAccountOwnership(request);
        
        // 2. Validate sufficient balance in source
        validateBalance(request);
        
        // 3. Create transfer record
        AccountTransfer transfer = AccountTransfer.builder()
            .userId(request.getUserId())
            .sourceAccountType(request.getSourceType())
            .sourceAccountId(request.getSourceId())
            .destinationAccountType(request.getDestinationType())
            .destinationAccountId(request.getDestinationId())
            .amount(request.getAmount())
            .transferDate(LocalDate.now())
            .status(TransferStatus.PENDING)
            .notes(request.getNotes())
            .build();
        
        transfer = transferRepository.save(transfer);
        
        // 4. Execute transfer
        executeTransfer(transfer);
        
        return transfer;
    }
    
    private void executeTransfer(AccountTransfer transfer) {
        try {
            // Debit source
            debitAccount(
                transfer.getSourceAccountType(),
                transfer.getSourceAccountId(),
                transfer.getAmount()
            );
            
            // Credit destination
            creditAccount(
                transfer.getDestinationAccountType(),
                transfer.getDestinationAccountId(),
                transfer.getAmount()
            );
            
            // Mark as completed
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setCompletedAt(LocalDateTime.now());
            transferRepository.save(transfer);
            
        } catch (Exception e) {
            transfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(transfer);
            throw new TransferException("Transfer failed: " + e.getMessage());
        }
    }
    
    private void debitAccount(AccountType type, Long accountId, BigDecimal amount) {
        switch (type) {
            case SAVINGS:
                savingsService.debit(accountId, amount);
                break;
            case BANK_ACCOUNT:
                bankAccountService.debit(accountId, amount);
                break;
            // ... other account types
        }
    }
    
    private void creditAccount(AccountType type, Long accountId, BigDecimal amount) {
        switch (type) {
            case SAVINGS:
                savingsService.credit(accountId, amount);
                break;
            case FIXED_DEPOSIT:
                fdService.deposit(accountId, amount);
                break;
            // ... other account types
        }
    }
    
    public List<AccountTransfer> getTransferHistory(Long userId, Pageable pageable) {
        return transferRepository.findByUserId(userId, pageable);
    }
}
```

**3. Update Expense Entity**
```java
// Add transaction type to differentiate
public enum TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER  // NEW - Excluded from budget calculations
}

@Entity
public class Expense {
    // ... existing fields
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType = TransactionType.EXPENSE;
    
    private Long transferId; // Link to AccountTransfer if type=TRANSFER
}
```

**4. Update Budget Calculation**
```java
@Service
public class BudgetService {
    
    public BigDecimal calculateTotalExpenses(Long userId, String monthYear) {
        LocalDate startDate = YearMonth.parse(monthYear).atDay(1);
        LocalDate endDate = YearMonth.parse(monthYear).atEndOfMonth();
        
        // Exclude TRANSFER type from expense calculation
        return expenseRepository.sumExpensesByUserIdAndDateRangeExcludingTransfers(
            userId,
            startDate,
            endDate
        );
    }
}

// Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    @Query("SELECT SUM(e.amount) FROM Expense e " +
           "WHERE e.userId = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.transactionType != 'TRANSFER'")
    BigDecimal sumExpensesByUserIdAndDateRangeExcludingTransfers(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
```

**5. Database Migration**
```sql
-- V67__Create_Account_Transfers.sql

CREATE TABLE account_transfers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_account_type VARCHAR(30) NOT NULL,
    source_account_id BIGINT NOT NULL,
    destination_account_type VARCHAR(30) NOT NULL,
    destination_account_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transfer_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_date (transfer_date),
    INDEX idx_source (source_account_type, source_account_id),
    INDEX idx_destination (destination_account_type, destination_account_id)
);

-- Add transaction_type to expenses
ALTER TABLE expenses 
    ADD COLUMN transaction_type VARCHAR(20) DEFAULT 'EXPENSE',
    ADD COLUMN transfer_id BIGINT NULL,
    ADD FOREIGN KEY (transfer_id) REFERENCES account_transfers(id);

CREATE INDEX idx_expense_transaction_type ON expenses(transaction_type);
```

**6. Transfer Controller**
```java
@RestController
@RequestMapping("/api/v1/transfers")
public class AccountTransferController {
    
    @Autowired
    private AccountTransferService transferService;
    
    @PostMapping
    public ResponseEntity<AccountTransfer> createTransfer(
            @Valid @RequestBody TransferRequest request) {
        
        AccountTransfer transfer = transferService.createTransfer(request);
        return ResponseEntity.ok(transfer);
    }
    
    @GetMapping("/{userId}/history")
    public Page<AccountTransfer> getTransferHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("transferDate").descending());
        return transferService.getTransferHistory(userId, pageable);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AccountTransfer> getTransferDetails(
            @PathVariable Long id) {
        
        return ResponseEntity.ok(transferService.getTransfer(id));
    }
}
```

#### Implementation Checklist:
- [ ] Create `account_transfers` table
- [ ] Add `transaction_type` to expenses
- [ ] Implement `AccountTransfer` entity
- [ ] Implement `AccountTransferService`
- [ ] Add debit/credit methods to account services
- [ ] Update budget calculation to exclude transfers
- [ ] Create transfer controller
- [ ] Update networth calculation (should remain unchanged)
- [ ] Add transfer history UI
- [ ] Add validation for transfer limits
- [ ] Implement transfer reversal functionality
- [ ] Add unit tests
- [ ] Add integration tests

---

### 5. Bill Reminders ⚠️ HIGH PRIORITY

**Status:** NOT FOUND  
**Impact:** Core Phase-1 reminder feature  
**Effort:** MEDIUM (1 week)

#### Required Implementation:

**1. Bill Entity**
```java
@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String billName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillCategory category;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedAmount; // Can vary month-to-month
    
    @Column(nullable = false)
    private Integer dueDayOfMonth; // 1-31
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillFrequency frequency;
    
    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.ACTIVE;
    
    private LocalDate lastPaidDate;
    
    private Boolean autoReminder = true;
    
    private Integer reminderDaysBefore = 3;
    
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum BillCategory {
    ELECTRICITY,
    WATER,
    GAS,
    INTERNET,
    PHONE,
    CABLE_TV,
    RENT,
    INSURANCE,
    SUBSCRIPTION,
    LOAN_EMI,
    CREDIT_CARD,
    OTHER
}

public enum BillFrequency {
    MONTHLY,
    QUARTERLY,
    HALF_YEARLY,
    ANNUAL
}

public enum BillStatus {
    ACTIVE,
    INACTIVE,
    CANCELLED
}
```

**2. Bill Payment History**
```java
@Entity
@Table(name = "bill_payment_history")
public class BillPaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long billId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid;
    
    @Column(nullable = false)
    private LocalDate paidDate;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    private String paymentMethod;
    
    private String transactionId;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", insertable = false, updatable = false)
    private Bill bill;
}
```

**3. Bill Reminder Scheduler**
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class BillReminderScheduler {
    
    private final BillRepository billRepository;
    private final NotificationService notificationService;
    private final JobStatusService jobStatusService;
    
    @Scheduled(cron = "0 0 9 * * *") // 9 AM daily
    public void checkBillReminders() {
        if (!jobStatusService.isJobEnabled("BILL_REMINDERS")) {
            log.info("Skipping BILL_REMINDERS job as it is currently DISABLED.");
            return;
        }
        
        log.info("Starting Bill Reminder Check Job...");
        jobStatusService.updateLastRun("BILL_REMINDERS");
        
        LocalDate today = LocalDate.now();
        
        // Get all active bills
        List<Bill> activeBills = billRepository.findByStatus(BillStatus.ACTIVE);
        
        for (Bill bill : activeBills) {
            if (!bill.getAutoReminder()) {
                continue;
            }
            
            // Calculate next due date
            LocalDate nextDueDate = calculateNextDueDate(bill, today);
            
            // Check if reminder should be sent
            int daysUntilDue = (int) ChronoUnit.DAYS.between(today, nextDueDate);
            
            if (daysUntilDue == bill.getReminderDaysBefore()) {
                sendBillReminder(bill, nextDueDate, daysUntilDue);
            } else if (daysUntilDue == 0) {
                sendBillDueTodayReminder(bill);
            } else if (daysUntilDue < 0) {
                sendOverdueReminder(bill, nextDueDate);
            }
        }
        
        log.info("Completed Bill Reminder Check Job.");
    }
    
    private LocalDate calculateNextDueDate(Bill bill, LocalDate today) {
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        int dueDay = bill.getDueDayOfMonth();
        
        // Handle end of month cases
        int maxDayInMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth();
        int actualDueDay = Math.min(dueDay, maxDayInMonth);
        
        LocalDate dueThisMonth = LocalDate.of(currentYear, currentMonth, actualDueDay);
        
        if (dueThisMonth.isBefore(today)) {
            // Due date passed this month, calculate for next billing cycle
            switch (bill.getFrequency()) {
                case MONTHLY:
                    return dueThisMonth.plusMonths(1);
                case QUARTERLY:
                    return dueThisMonth.plusMonths(3);
                case HALF_YEARLY:
                    return dueThisMonth.plusMonths(6);
                case ANNUAL:
                    return dueThisMonth.plusYears(1);
                default:
                    return dueThisMonth.plusMonths(1);
            }
        }
        
        return dueThisMonth;
    }
    
    private void sendBillReminder(Bill bill, LocalDate dueDate, int daysUntil) {
        String title = "Bill Reminder: " + bill.getBillName();
        String message = String.format(
            "Your %s bill is due in %d days on %s. " +
            "Estimated amount: ₹%.2f",
            bill.getBillName(),
            daysUntil,
            dueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            bill.getEstimatedAmount() != null ? bill.getEstimatedAmount() : 0
        );
        
        notificationService.sendNotification(
            bill.getUserId(),
            title,
            message,
            NotificationType.BILL_REMINDER,
            AlertChannel.IN_APP
        );
    }
    
    private void sendBillDueTodayReminder(Bill bill) {
        String title = "Bill Due Today: " + bill.getBillName();
        String message = String.format(
            "Your %s bill is due today! " +
            "Don't forget to pay. Estimated amount: ₹%.2f",
            bill.getBillName(),
            bill.getEstimatedAmount() != null ? bill.getEstimatedAmount() : 0
        );
        
        notificationService.sendNotification(
            bill.getUserId(),
            title,
            message,
            NotificationType.BILL_DUE_TODAY,
            AlertChannel.IN_APP
        );
    }
    
    private void sendOverdueReminder(Bill bill, LocalDate dueDate) {
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        
        String title = "Bill Overdue: " + bill.getBillName();
        String message = String.format(
            "Your %s bill is overdue by %d days! " +
            "Due date was %s. Please pay immediately.",
            bill.getBillName(),
            daysOverdue,
            dueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        );
        
        notificationService.sendNotification(
            bill.getUserId(),
            title,
            message,
            NotificationType.BILL_OVERDUE,
            AlertChannel.IN_APP
        );
    }
}
```

**4. Bill Service**
```java
@Service
@RequiredArgsConstructor
public class BillService {
    
    private final BillRepository billRepository;
    private final BillPaymentHistoryRepository paymentHistoryRepository;
    
    @Transactional
    public Bill createBill(BillRequest request) {
        Bill bill = Bill.builder()
            .userId(request.getUserId())
            .billName(request.getBillName())
            .category(request.getCategory())
            .estimatedAmount(request.getEstimatedAmount())
            .dueDayOfMonth(request.getDueDayOfMonth())
            .frequency(request.getFrequency())
            .autoReminder(request.getAutoReminder() != null ? request.getAutoReminder() : true)
            .reminderDaysBefore(request.getReminderDaysBefore() != null ? request.getReminderDaysBefore() : 3)
            .notes(request.getNotes())
            .status(BillStatus.ACTIVE)
            .build();
        
        return billRepository.save(bill);
    }
    
    @Transactional
    public Bill updateBill(Long id, BillRequest request) {
        Bill bill = billRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        bill.setBillName(request.getBillName());
        bill.setCategory(request.getCategory());
        bill.setEstimatedAmount(request.getEstimatedAmount());
        bill.setDueDayOfMonth(request.getDueDayOfMonth());
        bill.setFrequency(request.getFrequency());
        bill.setAutoReminder(request.getAutoReminder());
        bill.setReminderDaysBefore(request.getReminderDaysBefore());
        bill.setNotes(request.getNotes());
        
        return billRepository.save(bill);
    }
    
    @Transactional
    public BillPaymentHistory recordPayment(PaymentRequest request) {
        Bill bill = billRepository.findById(request.getBillId())
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        BillPaymentHistory payment = BillPaymentHistory.builder()
            .billId(request.getBillId())
            .amountPaid(request.getAmountPaid())
            .paidDate(request.getPaidDate())
            .dueDate(request.getDueDate())
            .paymentMethod(request.getPaymentMethod())
            .transactionId(request.getTransactionId())
            .notes(request.getNotes())
            .build();
        
        payment = paymentHistoryRepository.save(payment);
        
        // Update last paid date
        bill.setLastPaidDate(request.getPaidDate());
        billRepository.save(bill);
        
        return payment;
    }
    
    public List<Bill> getUserBills(Long userId) {
        return billRepository.findByUserIdAndStatus(userId, BillStatus.ACTIVE);
    }
    
    public List<BillPaymentHistory> getPaymentHistory(Long billId) {
        return paymentHistoryRepository.findByBillIdOrderByPaidDateDesc(billId);
    }
    
    public List<Bill> getUpcomingBills(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        // Get all user bills and filter by due date
        List<Bill> bills = billRepository.findByUserIdAndStatus(userId, BillStatus.ACTIVE);
        
        return bills.stream()
            .filter(bill -> {
                LocalDate nextDue = calculateNextDueDate(bill);
                return !nextDue.isBefore(today) && !nextDue.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }
}
```

**5. Database Migration**
```sql
-- V68__Create_Bills_System.sql

CREATE TABLE bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bill_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    estimated_amount DECIMAL(15,2),
    due_day_of_month INT NOT NULL CHECK (due_day_of_month >= 1 AND due_day_of_month <= 31),
    frequency VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_paid_date DATE,
    auto_reminder BOOLEAN DEFAULT TRUE,
    reminder_days_before INT DEFAULT 3,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    INDEX idx_user_status (user_id, status),
    INDEX idx_status (status),
    INDEX idx_due_day (due_day_of_month)
);

CREATE TABLE bill_payment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bill_id BIGINT NOT NULL,
    amount_paid DECIMAL(15,2) NOT NULL,
    paid_date DATE NOT NULL,
    due_date DATE,
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    
    INDEX idx_bill_id (bill_id),
    INDEX idx_paid_date (paid_date)
);
```

**6. Bill Controller**
```java
@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {
    
    private final BillService billService;
    
    @PostMapping
    public ResponseEntity<Bill> createBill(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.ok(billService.createBill(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody BillRequest request) {
        return ResponseEntity.ok(billService.updateBill(id, request));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bill>> getUserBills(@PathVariable Long userId) {
        return ResponseEntity.ok(billService.getUserBills(userId));
    }
    
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<Bill>> getUpcomingBills(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(billService.getUpcomingBills(userId, days));
    }
    
    @PostMapping("/payment")
    public ResponseEntity<BillPaymentHistory> recordPayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(billService.recordPayment(request));
    }
    
    @GetMapping("/{billId}/history")
    public ResponseEntity<List<BillPaymentHistory>> getPaymentHistory(
            @PathVariable Long billId) {
        return ResponseEntity.ok(billService.getPaymentHistory(billId));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        billService.deleteBill(id);
        return ResponseEntity.ok(Map.of("message", "Bill deleted"));
    }
}
```

#### Implementation Checklist:
- [ ] Create `bills` table migration
- [ ] Create `bill_payment_history` table
- [ ] Implement `Bill` entity
- [ ] Implement `BillPaymentHistory` entity
- [ ] Implement `BillService`
- [ ] Create `BillReminderScheduler`
- [ ] Add notification types for bills
- [ ] Create bill controller and endpoints
- [ ] Add upcoming bills dashboard widget
- [ ] Create payment history tracking
- [ ] Add bill analytics (average amount, payment trends)
- [ ] Implement recurring bill templates
- [ ] Add unit tests
- [ ] Add integration tests

---

### 6. EMI Reminders ⚠️ HIGH PRIORITY

**Status:** Loan entity exists but no EMI scheduler  
**Impact:** Core Phase-1 reminder feature  
**Effort:** MEDIUM (3-4 days)

#### Current Status:
- ✅ Loan entity exists with EMI amount field
- ❌ No EMI reminder scheduler
- ❌ No next EMI date tracking
- ❌ No EMI payment history

#### Required Enhancements:

**1. Update Loan Entity**
```java
@Entity
@Table(name = "loans")
public class Loan {
    // ... existing fields
    
    @Column(name = "next_emi_date")
    private LocalDate nextEmiDate;
    
    @Column(name = "emi_day_of_month")
    private Integer emiDayOfMonth; // 1-31
    
    @Column(name = "auto_reminder")
    private Boolean autoReminder = true;
    
    @Column(name = "reminder_days_before")
    private Integer reminderDaysBefore = 3;
    
    @Column(name = "total_emis")
    private Integer totalEmis;
    
    @Column(name = "emis_paid")
    private Integer emisPaid = 0;
    
    // Calculate next EMI date
    public void calculateNextEmiDate() {
        if (emiDayOfMonth == null) {
            return;
        }
        
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        
        int maxDay = currentMonth.lengthOfMonth();
        int actualDay = Math.min(emiDayOfMonth, maxDay);
        
        LocalDate emiThisMonth = LocalDate.of(
            today.getYear(),
            today.getMonthValue(),
            actualDay
        );
        
        if (emiThisMonth.isBefore(today) || emiThisMonth.equals(today)) {
            // EMI for this month passed, set to next month
            YearMonth nextMonth = currentMonth.plusMonths(1);
            int nextMaxDay = nextMonth.lengthOfMonth();
            int nextActualDay = Math.min(emiDayOfMonth, nextMaxDay);
            
            this.nextEmiDate = LocalDate.of(
                nextMonth.getYear(),
                nextMonth.getMonthValue(),
                nextActualDay
            );
        } else {
            this.nextEmiDate = emiThisMonth;
        }
    }
}
```

**2. EMI Payment History Entity**
```java
@Entity
@Table(name = "emi_payment_history")
public class EMIPaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long loanId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal principalComponent;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal interestComponent;
    
    @Column(nullable = false)
    private LocalDate paymentDate;
    
    @Column(nullable = false)
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // ON_TIME, LATE, PARTIAL, SKIPPED
    
    private String paymentMethod;
    
    private String transactionId;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", insertable = false, updatable = false)
    private Loan loan;
}

public enum PaymentStatus {
    ON_TIME,    // Paid on or before due date
    LATE,       // Paid after due date
    PARTIAL,    // Less than full EMI paid
    SKIPPED     // Not paid
}
```

**3. EMI Reminder Scheduler**
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class EMIReminderScheduler {
    
    private final LoanRepository loanRepository;
    private final NotificationService notificationService;
    private final JobStatusService jobStatusService;
    
    @Scheduled(cron = "0 0 9 * * *") // 9 AM daily
    public void checkEMIReminders() {
        if (!jobStatusService.isJobEnabled("EMI_REMINDERS")) {
            log.info("Skipping EMI_REMINDERS job as it is currently DISABLED.");
            return;
        }
        
        log.info("Starting EMI Reminder Check Job...");
        jobStatusService.updateLastRun("EMI_REMINDERS");
        
        LocalDate today = LocalDate.now();
        
        // Get all active loans
        List<Loan> activeLoans = loanRepository.findByStatus("ACTIVE");
        
        for (Loan loan : activeLoans) {
            if (!loan.getAutoReminder() || loan.getNextEmiDate() == null) {
                continue;
            }
            
            int daysUntilDue = (int) ChronoUnit.DAYS.between(today, loan.getNextEmiDate());
            
            if (daysUntilDue == loan.getReminderDaysBefore()) {
                // Send reminder N days before
                sendEMIReminder(loan, daysUntilDue);
            } else if (daysUntilDue == 0) {
                // EMI due today
                sendEMIDueTodayReminder(loan);
            } else if (daysUntilDue < 0) {
                // EMI overdue
                sendEMIOverdueReminder(loan, Math.abs(daysUntilDue));
            }
        }
        
        log.info("Completed EMI Reminder Check Job.");
    }
    
    private void sendEMIReminder(Loan loan, int daysUntil) {
        String title = "EMI Reminder: " + loan.getLoanType() + " Loan";
        String message = String.format(
            "Your EMI of ₹%.2f for %s loan is due in %d days on %s. " +
            "Outstanding amount: ₹%.2f. " +
            "Ensure sufficient balance for auto-debit.",
            loan.getEmiAmount(),
            loan.getLoanType(),
            daysUntil,
            loan.getNextEmiDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            loan.getOutstandingAmount()
        );
        
        notificationService.sendNotification(
            loan.getUserId(),
            title,
            message,
            NotificationType.EMI_REMINDER,
            AlertChannel.IN_APP
        );
    }
    
    private void sendEMIDueTodayReminder(Loan loan) {
        String title = "EMI Due Today: " + loan.getLoanType() + " Loan";
        String message = String.format(
            "Your EMI of ₹%.2f is due today! " +
            "Make sure to pay on time to avoid late fees. " +
            "Outstanding: ₹%.2f",
            loan.getEmiAmount(),
            loan.getOutstandingAmount()
        );
        
        notificationService.sendNotification(
            loan.getUserId(),
            title,
            message,
            NotificationType.EMI_DUE_TODAY,
            AlertChannel.IN_APP
        );
    }
    
    private void sendEMIOverdueReminder(Loan loan, int daysOverdue) {
        String title = "EMI Overdue: " + loan.getLoanType() + " Loan";
        String message = String.format(
            "Your EMI of ₹%.2f is overdue by %d days! " +
            "Please pay immediately to avoid impact on credit score. " +
            "Late payment fees may apply.",
            loan.getEmiAmount(),
            daysOverdue
        );
        
        notificationService.sendNotification(
            loan.getUserId(),
            title,
            message,
            NotificationType.EMI_OVERDUE,
            AlertChannel.IN_APP
        );
    }
}
```

**4. Database Migration**
```sql
-- V69__Add_EMI_Tracking_To_Loans.sql

-- Add EMI tracking fields to loans table
ALTER TABLE loans
    ADD COLUMN next_emi_date DATE,
    ADD COLUMN emi_day_of_month INT,
    ADD COLUMN auto_reminder BOOLEAN DEFAULT TRUE,
    ADD COLUMN reminder_days_before INT DEFAULT 3,
    ADD COLUMN total_emis INT,
    ADD COLUMN emis_paid INT DEFAULT 0;

-- Create EMI payment history table
CREATE TABLE emi_payment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    emi_amount DECIMAL(15,2) NOT NULL,
    principal_component DECIMAL(15,2),
    interest_component DECIMAL(15,2),
    payment_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20), -- ON_TIME, LATE, PARTIAL, SKIPPED
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE,
    
    INDEX idx_loan_id (loan_id),
    INDEX idx_due_date (due_date),
    INDEX idx_payment_date (payment_date),
    INDEX idx_status (status)
);

-- Update existing loans to calculate next EMI date
-- (Run this as part of migration or manually)
```

**5. EMI Service Enhancement**
```java
@Service
@RequiredArgsConstructor
public class EMIService {
    
    private final LoanRepository loanRepository;
    private final EMIPaymentHistoryRepository emiHistoryRepository;
    
    @Transactional
    public EMIPaymentHistory recordEMIPayment(EMIPaymentRequest request) {
        Loan loan = loanRepository.findById(request.getLoanId())
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        // Calculate principal and interest components
        BigDecimal principalComponent = calculatePrincipalComponent(loan);
        BigDecimal interestComponent = loan.getEmiAmount().subtract(principalComponent);
        
        // Create payment record
        EMIPaymentHistory payment = EMIPaymentHistory.builder()
            .loanId(request.getLoanId())
            .emiAmount(request.getAmountPaid())
            .principalComponent(principalComponent)
            .interestComponent(interestComponent)
            .paymentDate(request.getPaymentDate())
            .dueDate(loan.getNextEmiDate())
            .status(determinePaymentStatus(request.getPaymentDate(), loan.getNextEmiDate()))
            .paymentMethod(request.getPaymentMethod())
            .transactionId(request.getTransactionId())
            .notes(request.getNotes())
            .build();
        
        payment = emiHistoryRepository.save(payment);
        
        // Update loan
        loan.setEmis Paid(loan.getEmisPaid() + 1);
        loan.setOutstandingAmount(
            loan.getOutstandingAmount().subtract(principalComponent)
        );
        
        // Calculate next EMI date
        loan.calculateNextEmiDate();
        
        loanRepository.save(loan);
        
        return payment;
    }
    
    private PaymentStatus determinePaymentStatus(LocalDate paymentDate, LocalDate dueDate) {
        if (paymentDate.isBefore(dueDate) || paymentDate.equals(dueDate)) {
            return PaymentStatus.ON_TIME;
        } else {
            return PaymentStatus.LATE;
        }
    }
    
    private BigDecimal calculatePrincipalComponent(Loan loan) {
        // Simple calculation - can be enhanced with proper loan amortization
        BigDecimal monthlyInterest = loan.getOutstandingAmount()
            .multiply(loan.getInterestRate())
            .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
        
        return loan.getEmiAmount().subtract(monthlyInterest);
    }
    
    public List<EMIPaymentHistory> getEMIHistory(Long loanId) {
        return emiHistoryRepository.findByLoanIdOrderByDueDateDesc(loanId);
    }
    
    public EMIScheduleDTO getEMISchedule(Long loanId) {
        // Generate full EMI schedule with amortization
        // ... implementation
    }
}
```

**6. EMI Controller**
```java
@RestController
@RequestMapping("/api/v1/emi")
@RequiredArgsConstructor
public class EMIController {
    
    private final EMIService emiService;
    
    @PostMapping("/payment")
    public ResponseEntity<EMIPaymentHistory> recordPayment(
            @Valid @RequestBody EMIPaymentRequest request) {
        return ResponseEntity.ok(emiService.recordEMIPayment(request));
    }
    
    @GetMapping("/loan/{loanId}/history")
    public ResponseEntity<List<EMIPaymentHistory>> getHistory(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.getEMIHistory(loanId));
    }
    
    @GetMapping("/loan/{loanId}/schedule")
    public ResponseEntity<EMIScheduleDTO> getSchedule(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.getEMISchedule(loanId));
    }
    
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<UpcomingEMIDTO>> getUpcomingEMIs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(emiService.getUpcomingEMIs(userId, days));
    }
}
```

#### Implementation Checklist:
- [ ] Add EMI tracking fields to `loans` table (Migration V69)
- [ ] Create `emi_payment_history` table
- [ ] Update `Loan` entity with new fields
- [ ] Implement `EMIPaymentHistory` entity
- [ ] Create `EMIReminderScheduler`
- [ ] Implement `EMIService`
- [ ] Add EMI payment recording
- [ ] Create EMI schedule calculator
- [ ] Add notification types (EMI_REMINDER, EMI_DUE_TODAY, EMI_OVERDUE)
- [ ] Create EMI controller
- [ ] Update all existing loans with next EMI date
- [ ] Add EMI dashboard widget
- [ ] Implement amortization schedule
- [ ] Add unit tests
- [ ] Add integration tests

---

## 📊 IMPLEMENTATION PRIORITY MATRIX

### HIGH PRIORITY (Complete First)
1. **SMS Parsing for Transactions** - 3 weeks
2. **Phone OTP Verification** - 2 weeks
3. **Unknown Transaction Review System** - 2 weeks
4. **Transfer Between Accounts** - 1 week
5. **Bill Reminders** - 1 week
6. **EMI Reminders** - 4 days

**Total Effort: ~9-10 weeks**

### MEDIUM PRIORITY (Phase 1.5)
7. Push Notifications - 1 week
8. SMS Notifications - 3 days
9. Predictive Analytics - 2 weeks
10. Anomaly Detection - 1 week

---

## 🎯 RECOMMENDED SPRINT PLAN

### Sprint 1 (2 weeks): Critical Security & UX
- **Week 1-2:** Phone OTP Verification
  - Database migration
  - OTP service implementation
  - SMS gateway integration
  - Frontend components
  - Testing

### Sprint 2 (3 weeks): Transaction Automation
- **Week 1-2:** SMS Parsing Implementation
  - SMS receiver (Android/iOS)
  - Parser service with bank patterns
  - Database tables
  - Auto-expense creation
- **Week 3:** Unknown Transaction Review System
  - Review queue
  - Smart categorization
  - Learning algorithm

### Sprint 3 (2 weeks): Account Management
- **Week 1:** Transfer Between Accounts
  - Transfer entity and service
  - Update budget calculations
  - UI implementation
- **Week 2:** Bill Reminders
  - Bill entity and scheduler
  - Payment history
  - Reminders integration

### Sprint 4 (1 week): EMI & Refinement
- **Week 1:** EMI Reminders
  - Loan enhancements
  - EMI scheduler
  - Payment history
  - Testing and bug fixes

---

## 📈 SUCCESS METRICS

Upon completion of missing features:

| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Phase-1 Completion | 72% | 100% | 🔴 In Progress |
| Automated Transactions | 0% | 80% | 🔴 Missing SMS |
| User Onboarding (OTP) | 0% | 100% | 🔴 Missing |
| Reminder Coverage | 40% | 100% | 🟡 Partial |
| Transaction Accuracy | 60% | 95% | 🟡 Manual Only |

---

## 🚀 CONCLUSION

Your PI-System has an **excellent foundation** with world-class implementation of:
- ✅ Budget Management (100%)
- ✅ Networth Calculation (100%)
- ✅ Financial Insights (90%)

**Critical Next Steps:**
1. Implement SMS parsing - cornerstone feature for automation
2. Add OTP verification - security requirement
3. Build review system - data quality assurance
4. Complete reminder infrastructure - bill + EMI

**Estimated Timeline:** 9-10 weeks to achieve 100% Phase-1 compliance

After completing these features, the system will be:
- Production-ready for Phase-1
- Fully automated transaction capture
- Comprehensive reminder system
- Secure user verification

**Recommendation:** Proceed with Sprint 1 (OTP + SMS Parsing) immediately as these are the most critical missing features.

---

**Report Generated:** March 12, 2026  
**Next Review:** Post Sprint 1 Completion  
**Contact:** Senior Software Architect
