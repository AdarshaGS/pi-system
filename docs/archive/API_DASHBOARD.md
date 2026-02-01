# 🚀 API Documentation Dashboard

*Last generated: 2/1/2026, 12:44:00 AM*

## Overview

This dashboard provides a complete inventory of all REST APIs in the project.

---

## 📊 Statistics

- **Total Endpoints**: 160
- **Controllers**: 24
- **Domains**: 10

### HTTP Methods Distribution

- 🔍 **GET**: 95
- ➕ **POST**: 60
- 🗑️ **DELETE**: 41
- 📝 **PUT**: 36

---

## 🗺️ Quick Navigation

- [Authentication & Users](#authentication-users) (21 endpoints)
- [Budget Management](#budget-management) (48 endpoints)
- [External Services](#external-services) (3 endpoints)
- [Insurance](#insurance) (6 endpoints)
- [Investments - ETF](#investments-etf) (4 endpoints)
- [Investments - Mutual Funds](#investments-mutual-funds) (8 endpoints)
- [Investments - Stocks](#investments-stocks) (8 endpoints)
- [Lending](#lending) (6 endpoints)
- [Other](#other) (50 endpoints)
- [Savings](#savings) (6 endpoints)

---

## Authentication & Users

**Total Endpoints**: 21

### `/api/auth/api`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/auth/api/auth` | - | `AuthController` |

### `/api/auth/does-user-exists`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `/api/auth/does-user-exists` | doesUserExists | `AuthController` |

### `/api/auth/forgot-password`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `/api/auth/forgot-password` | forgotPassword | `AuthController` |

### `/api/auth/login`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `/api/auth/login` | login | `AuthController` |

### `/api/auth/logout`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `/api/auth/logout` | logout | `AuthController` |

### `/api/auth/profile`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `/api/auth/profile` | getProfile | `AuthController` |

### `/api/auth/refresh`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `/api/auth/refresh` | refresh | `AuthController` |

### `/api/auth/register`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `/api/auth/register` | register | `AuthController` |

### `/api/auth/update-user`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `/api/auth/update-user` | updateUser | `AuthController` |

### `/api/v1/admin`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/admin/api/v1/admin` | - | `AdminController` |
| 🔍 GET | `/api/v1/admin/internal/health` | internalHealth | `AdminController` |
| 🔍 GET | `/api/v1/admin/dashboard` | getAdminStats | `AdminController` |
| 🔍 GET | `/api/v1/admin/users` | - | `AdminController` |
| 🔍 GET | `/api/v1/admin/users/{userId}` | - | `AdminController` |
| 📝 PUT | `/api/v1/admin/users/{userId}` | - | `AdminController` |
| ➕ POST | `/api/v1/admin/users/{userId}/roles/{roleName}` | - | `AdminController` |
| 🗑️ DELETE | `/api/v1/admin/users/{userId}/roles/{roleName}` | - | `AdminController` |
| 🗑️ DELETE | `/api/v1/admin/users/{userId}` | - | `AdminController` |
| 🔍 GET | `/api/v1/admin/utilities/critical-logs` | - | `AdminController` |
| 🔍 GET | `/api/v1/admin/utilities/activity-logs` | - | `AdminController` |
| 🔍 GET | `/api/v1/admin/roles` | - | `AdminController` |


## Budget Management

**Total Endpoints**: 48

### `/api/v1/budget`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/budget/api/v1/budget` | - | `BudgetController` |
| ➕ POST | `/api/v1/budget/expense` | addExpense | `BudgetController` |
| 🔍 GET | `/api/v1/budget/expense/{userId}` | getExpenses | `BudgetController` |
| 🔍 GET | `/api/v1/budget/expense/detail/{id}` | getExpenseById | `BudgetController` |
| 📝 PUT | `/api/v1/budget/expense/{id}` | updateExpense | `BudgetController` |
| 🗑️ DELETE | `/api/v1/budget/expense/{id}` | deleteExpense | `BudgetController` |
| ➕ POST | `/api/v1/budget/income` | addIncome | `BudgetController` |
| 🔍 GET | `/api/v1/budget/income/{userId}` | getIncomes | `BudgetController` |
| 🔍 GET | `/api/v1/budget/income/detail/{id}` | getIncomeById | `BudgetController` |
| 📝 PUT | `/api/v1/budget/income/{id}` | updateIncome | `BudgetController` |
| 🗑️ DELETE | `/api/v1/budget/income/{id}` | deleteIncome | `BudgetController` |
| ➕ POST | `/api/v1/budget/limit` | setBudget | `BudgetController` |
| ➕ POST | `/api/v1/budget/limit/batch` | setBudgetsBatch | `BudgetController` |
| 🔍 GET | `/api/v1/budget/limit/{userId}` | getAllBudgets | `BudgetController` |
| 🗑️ DELETE | `/api/v1/budget/limit/{id}` | deleteBudget | `BudgetController` |
| 🔍 GET | `/api/v1/budget/report/{userId}` | getReport | `BudgetController` |
| 🔍 GET | `/api/v1/budget/cashflow/{userId}` | getCashFlow | `BudgetController` |
| ➕ POST | `/api/v1/budget/category/custom` | createCustomCategory | `BudgetController` |
| 🔍 GET | `/api/v1/budget/category/custom/{userId}` | getUserCustomCategories | `BudgetController` |
| 🔍 GET | `/api/v1/budget/category/all/{userId}` | getAllCategories | `BudgetController` |
| 📝 PUT | `/api/v1/budget/category/custom/{id}` | updateCustomCategory | `BudgetController` |
| 🗑️ DELETE | `/api/v1/budget/category/custom/{id}` | deleteCustomCategory | `BudgetController` |
| 🗑️ DELETE | `/api/v1/budget/category/custom/{id}/hard` | hardDeleteCustomCategory | `BudgetController` |
| 🔍 GET | `/api/v1/budget/total/{userId}` | getTotalMonthlyBudget | `BudgetController` |
| 🔍 GET | `/api/v1/budget/expense/{userId}/export/csv` | exportExpensesCSV | `BudgetController` |
| 🔍 GET | `/api/v1/budget/expense/{userId}/export/excel` | exportExpensesExcel | `BudgetController` |
| 🔍 GET | `/api/v1/budget/income/{userId}/export/csv` | exportIncomesCSV | `BudgetController` |
| 🔍 GET | `/api/v1/budget/income/{userId}/export/excel` | exportIncomesExcel | `BudgetController` |
| 🔍 GET | `/api/v1/budget/report/{userId}/pdf` | generatePDFReport | `BudgetController` |
| ➕ POST | `/api/v1/budget/report/{userId}/email` | - | `BudgetController` |
| ➕ POST | `/api/v1/budget/expense/bulk-delete` | - | `BudgetController` |
| ➕ POST | `/api/v1/budget/expense/bulk-update-category` | - | `BudgetController` |
| 🔍 GET | `/api/v1/budget/variance-analysis` | - | `BudgetController` |
| 🔍 GET/POST/PUT/DELETE | `/api/v1/budget/recurring/api/v1/budget/recurring` | - | `RecurringTransactionController` |
| 🔍 GET | `/api/v1/budget/recurring/{userId}` | getUserTemplates | `RecurringTransactionController` |
| 🔍 GET | `/api/v1/budget/recurring/{userId}/active` | getActiveTemplates | `RecurringTransactionController` |
| ➕ POST | `/api/v1/budget/recurring` | createTemplate | `RecurringTransactionController` |
| 📝 PUT | `/api/v1/budget/recurring/{id}` | updateTemplate | `RecurringTransactionController` |
| 🗑️ DELETE | `/api/v1/budget/recurring/{id}` | - | `RecurringTransactionController` |
| ➕ POST | `/api/v1/budget/recurring/{id}/toggle` | toggleActive | `RecurringTransactionController` |
| 🔍 GET | `/api/v1/budget/recurring/{id}/upcoming` | getUpcomingDates | `RecurringTransactionController` |
| ➕ POST | `/api/v1/budget/recurring/generate` | - | `RecurringTransactionController` |
| 🔍 GET/POST/PUT/DELETE | `/api/v1/budget/tags/api/v1/budget/tags` | - | `TagController` |
| 🔍 GET | `/api/v1/budget/tags/{userId}` | getUserTags | `TagController` |
| 🔍 GET | `/api/v1/budget/tags/detail/{tagId}` | getTagById | `TagController` |
| ➕ POST | `/api/v1/budget/tags` | createTag | `TagController` |
| 📝 PUT | `/api/v1/budget/tags/{tagId}` | updateTag | `TagController` |
| 🗑️ DELETE | `/api/v1/budget/tags/{tagId}` | - | `TagController` |


## External Services

**Total Endpoints**: 3

### `/api/v1/external-services`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/external-services/api/v1/external-services` | - | `ExternalServiceController` |
| 🔍 GET | `/api/v1/external-services` | getAllServices | `ExternalServiceController` |
| 🔍 GET | `/api/v1/external-services/{serviceName}` | getExternalService | `ExternalServiceController` |


## Insurance

**Total Endpoints**: 6

### `/api/v1/insurance`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/insurance/api/v1/insurance` | - | `InsuranceController` |
| ➕ POST | `/api/v1/insurance` | createInsuranceDetails | `InsuranceController` |
| 🔍 GET | `/api/v1/insurance` | - | `InsuranceController` |
| 🔍 GET | `/api/v1/insurance/user/{userId}` | - | `InsuranceController` |
| 🔍 GET | `/api/v1/insurance/{id}` | getInsuranceDetailsById | `InsuranceController` |
| 🗑️ DELETE | `/api/v1/insurance/{id}` | deleteInsurancePolicy | `InsuranceController` |


## Investments - ETF

**Total Endpoints**: 4

### `/api/v1/etf`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/etf/api/v1/etf` | - | `EtfController` |
| 🔍 GET | `/api/v1/etf` | getAllEtfs | `EtfController` |
| ➕ POST | `/api/v1/etf` | addEtf | `EtfController` |
| 🔍 GET | `/api/v1/etf/{symbol}` | getEtfBySymbol | `EtfController` |


## Investments - Mutual Funds

**Total Endpoints**: 8

### `/api/v1/mutual-funds`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/mutual-funds/api/v1/mutual-funds` | - | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/summary` | getSummary | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/holdings` | - | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/insights` | getInsights | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/external/search` | - | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/external/schemes` | - | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/external/schemes/{schemeCode}/nav` | - | `MutualFundController` |
| 🔍 GET | `/api/v1/mutual-funds/external/schemes/{schemeCode}/latest` | - | `MutualFundController` |


## Investments - Stocks

**Total Endpoints**: 8

### `/api/v1/net-worth`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/net-worth/api/v1/net-worth` | - | `NetWorthController` |
| 🔍 GET | `/api/v1/net-worth/{userId}` | getNetWorth | `NetWorthController` |
| 🔍 GET | `/api/v1/net-worth/template` | getTemplates | `NetWorthController` |

### `/api/v1/portfolio`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/portfolio/api/v1/portfolio` | - | `PortfolioController` |
| ➕ POST | `/api/v1/portfolio` | postPortfolioData | `PortfolioController` |
| 🔍 GET | `/api/v1/portfolio/summary/{userId}` | getPortfolioSummary | `PortfolioController` |

### `/api/v1/stocks`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/stocks/api/v1/stocks` | - | `StockController` |
| 🔍 GET | `/api/v1/stocks/{symbol}` | getStockBySymbol | `StockController` |


## Lending

**Total Endpoints**: 6

### `/api/v1/lending`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/lending/api/v1/lending` | - | `LendingController` |
| ➕ POST | `/api/v1/lending` | addLending | `LendingController` |
| 🔍 GET | `/api/v1/lending` | - | `LendingController` |
| 🔍 GET | `/api/v1/lending/{id}` | getLendingById | `LendingController` |
| ➕ POST | `/api/v1/lending/{id}/repayment` | addRepayment | `LendingController` |
| 📝 PUT | `/api/v1/lending/{id}/close` | closeLending | `LendingController` |


## Other

**Total Endpoints**: 50

### `/api/health`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `/api/health` | getHealth | `HealthCheckController` |

### `/api/health/api`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/health/api/health` | - | `HealthCheckController` |

### `/api/v1/aa`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/aa/api/v1/aa` | - | `AAController` |
| 🔍 GET | `/api/v1/aa/consent/templates` | - | `AAController` |
| ➕ POST | `/api/v1/aa/consent` | createConsent | `AAController` |
| 🔍 GET | `/api/v1/aa/consent/{consentId}/status` | getConsentStatus | `AAController` |
| ➕ POST | `/api/v1/aa/fetch` | initiateFetch | `AAController` |
| 🔍 GET | `/api/v1/aa/fetch/{requestId}/status` | getFetchStatus | `AAController` |
| 🔍 GET | `/api/v1/aa/fetch/{requestId}/data` | getDecryptedData | `AAController` |
| 🗑️ DELETE | `/api/v1/aa/consent/{consentId}` | revokeConsent | `AAController` |

### `/api/v1/alerts`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/alerts/api/v1/alerts` | - | `AlertController` |
| 🔍 GET | `/api/v1/alerts` | - | `AlertController` |
| 🔍 GET | `/api/v1/alerts/unread` | - | `AlertController` |
| 🔍 GET | `/api/v1/alerts/month/{monthYear}` | - | `AlertController` |
| 🔍 GET | `/api/v1/alerts/summary` | - | `AlertController` |
| 📝 PUT | `/api/v1/alerts/{alertId}/read` | - | `AlertController` |
| 📝 PUT | `/api/v1/alerts/read-all` | - | `AlertController` |
| 🗑️ DELETE | `/api/v1/alerts/{alertId}` | - | `AlertController` |
| 🗑️ DELETE | `/api/v1/alerts` | - | `AlertController` |
| ➕ POST | `/api/v1/alerts/check` | - | `AlertController` |

### `/api/v1/loans`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/loans/api/v1/loans` | - | `LoanController` |
| ➕ POST | `/api/v1/loans/create` | createLoan | `LoanController` |
| 🔍 GET | `/api/v1/loans/all` | - | `LoanController` |
| 🔍 GET | `/api/v1/loans/user/{userId}` | - | `LoanController` |
| 🔍 GET | `/api/v1/loans/{id}` | getLoanById | `LoanController` |
| 🗑️ DELETE | `/api/v1/loans/{id}` | deleteLoan | `LoanController` |
| ➕ POST | `/api/v1/loans/{id}/simulate-prepayment` | - | `LoanController` |

### `/api/v1/super-admin`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/super-admin/api/v1/super-admin` | - | `SuperAdminController` |
| ➕ POST | `/api/v1/super-admin/update-role/{userId}` | updateRole | `SuperAdminController` |

### `/api/v1/test-runner`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/test-runner/api/v1/test-runner` | - | `TestRunnerController` |
| ➕ POST | `/api/v1/test-runner/run` | runTests | `TestRunnerController` |
| 🔍 GET | `/api/v1/test-runner/status` | getStatus | `TestRunnerController` |

### `/open/dev/migration`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/open/dev/migration/open/dev/migration` | - | `MigrationGeneratorController` |
| ➕ POST | `/open/dev/migration/generate` | generateMigration | `MigrationGeneratorController` |

### `api/v1/fixed-deposit`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `api/v1/fixed-deposit` | createFixedDeposit | `FixedDepositController` |

### `api/v1/fixed-deposit/user`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `api/v1/fixed-deposit/user/{userId}` | getAllFixedDeposits | `FixedDepositController` |

### `api/v1/fixed-deposit/{id}`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `api/v1/fixed-deposit/{id}` | getFixedDeposit | `FixedDepositController` |
| 📝 PUT | `api/v1/fixed-deposit/{id}` | updateFixedDeposit | `FixedDepositController` |
| 🗑️ DELETE | `api/v1/fixed-deposit/{id}` | deleteFixedDeposit | `FixedDepositController` |

### `api/v1/fixed-depositapi/v1`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `api/v1/fixed-depositapi/v1/fixed-deposit` | - | `FixedDepositController` |

### `api/v1/recurring-deposit`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `api/v1/recurring-deposit` | createRecurringDeposit | `RecurringDepositController` |

### `api/v1/recurring-deposit/user`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `api/v1/recurring-deposit/user/{userId}` | getAllRecurringDeposits | `RecurringDepositController` |

### `api/v1/recurring-deposit/{id}`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `api/v1/recurring-deposit/{id}` | getRecurringDeposit | `RecurringDepositController` |
| 📝 PUT | `api/v1/recurring-deposit/{id}` | updateRecurringDeposit | `RecurringDepositController` |
| 🗑️ DELETE | `api/v1/recurring-deposit/{id}` | deleteRecurringDeposit | `RecurringDepositController` |

### `api/v1/recurring-depositapi/v1`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `api/v1/recurring-depositapi/v1/recurring-deposit` | - | `RecurringDepositController` |

### `api/v1/tax`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| ➕ POST | `api/v1/tax` | createTaxDetails | `TaxController` |

### `api/v1/tax/{userId}`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET | `api/v1/tax/{userId}` | getTaxDetails | `TaxController` |
| 🔍 GET | `api/v1/tax/{userId}/liability` | getOutstandingTaxLiability | `TaxController` |

### `api/v1/taxapi/v1`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `api/v1/taxapi/v1/tax` | - | `TaxController` |


## Savings

**Total Endpoints**: 6

### `/api/v1/savings-accounts`

| Method | Endpoint | Description | Controller |
|--------|----------|-------------|------------|
| 🔍 GET/POST/PUT/DELETE | `/api/v1/savings-accounts/api/v1/savings-accounts` | - | `SavingsController` |
| ➕ POST | `/api/v1/savings-accounts` | postSavingsAccountDetails | `SavingsController` |
| 🔍 GET | `/api/v1/savings-accounts` | getAllSavingsAccounts | `SavingsController` |
| 🔍 GET | `/api/v1/savings-accounts/{id}` | getSavingsAccountById | `SavingsController` |
| 📝 PUT | `/api/v1/savings-accounts/{id}` | updateSavingsAccount | `SavingsController` |
| 🗑️ DELETE | `/api/v1/savings-accounts/{id}` | - | `SavingsController` |


---

## 📖 Detailed Endpoint Reference

### Authentication & Users

#### `GET/POST/PUT/DELETE /api/v1/admin/api/v1/admin`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `GET /api/v1/admin/internal/health`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: `internalHealth`

---

#### `GET /api/v1/admin/dashboard`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: `getAdminStats`

---

#### `GET /api/v1/admin/users`

**Request Parameters**:
- `search`
- `role`
- `page`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `GET /api/v1/admin/users/{userId}`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `PUT /api/v1/admin/users/{userId}`

**Path Variables**:
- `userId`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `POST /api/v1/admin/users/{userId}/roles/{roleName}`

**Path Variables**:
- `userId`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `DELETE /api/v1/admin/users/{userId}/roles/{roleName}`

**Path Variables**:
- `userId`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `DELETE /api/v1/admin/users/{userId}`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `GET /api/v1/admin/utilities/critical-logs`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `GET /api/v1/admin/utilities/activity-logs`

**Request Parameters**:
- `userId`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `GET /api/v1/admin/roles`

**Controller**: [`AdminController`](src/main/java/com/auth/controller/AdminController.java)
**Method**: ``

---

#### `GET/POST/PUT/DELETE /api/auth/api/auth`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: ``

---

#### `POST /api/auth/login`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `login`

---

#### `POST /api/auth/register`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `register`

---

#### `POST /api/auth/forgot-password`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `forgotPassword`

---

#### `POST /api/auth/logout`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `logout`

---

#### `POST /api/auth/update-user`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `updateUser`

---

#### `POST /api/auth/refresh`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `refresh`

---

#### `GET /api/auth/does-user-exists`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `doesUserExists`

---

#### `GET /api/auth/profile`

**Controller**: [`AuthController`](src/main/java/com/auth/controller/AuthController.java)
**Method**: `getProfile`

---

### Budget Management

#### `GET/POST/PUT/DELETE /api/v1/budget/api/v1/budget`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: ``

---

#### `POST /api/v1/budget/expense`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `addExpense`

---

#### `GET /api/v1/budget/expense/{userId}`

**Path Variables**:
- `userId`

**Request Parameters**:
- `page`
- `size`
- `sortBy`
- `order`
- `category`
- `startDate`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getExpenses`

---

#### `GET /api/v1/budget/expense/detail/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getExpenseById`

---

#### `PUT /api/v1/budget/expense/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `updateExpense`

---

#### `DELETE /api/v1/budget/expense/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `deleteExpense`

---

#### `POST /api/v1/budget/income`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `addIncome`

---

#### `GET /api/v1/budget/income/{userId}`

**Path Variables**:
- `userId`

**Request Parameters**:
- `page`
- `size`
- `sortBy`
- `order`
- `source`
- `startDate`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getIncomes`

---

#### `GET /api/v1/budget/income/detail/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getIncomeById`

---

#### `PUT /api/v1/budget/income/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `updateIncome`

---

#### `DELETE /api/v1/budget/income/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `deleteIncome`

---

#### `POST /api/v1/budget/limit`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `setBudget`

---

#### `POST /api/v1/budget/limit/batch`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `setBudgetsBatch`

---

#### `GET /api/v1/budget/limit/{userId}`

**Path Variables**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getAllBudgets`

---

#### `DELETE /api/v1/budget/limit/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `deleteBudget`

---

#### `GET /api/v1/budget/report/{userId}`

**Path Variables**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getReport`

---

#### `GET /api/v1/budget/cashflow/{userId}`

**Path Variables**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getCashFlow`

---

#### `POST /api/v1/budget/category/custom`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `createCustomCategory`

---

#### `GET /api/v1/budget/category/custom/{userId}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getUserCustomCategories`

---

#### `GET /api/v1/budget/category/all/{userId}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getAllCategories`

---

#### `PUT /api/v1/budget/category/custom/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `updateCustomCategory`

---

#### `DELETE /api/v1/budget/category/custom/{id}`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `deleteCustomCategory`

---

#### `DELETE /api/v1/budget/category/custom/{id}/hard`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `hardDeleteCustomCategory`

---

#### `GET /api/v1/budget/total/{userId}`

**Path Variables**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `getTotalMonthlyBudget`

---

#### `GET /api/v1/budget/expense/{userId}/export/csv`

**Path Variables**:
- `userId`

**Request Parameters**:
- `startDate`
- `endDate`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `exportExpensesCSV`

---

#### `GET /api/v1/budget/expense/{userId}/export/excel`

**Path Variables**:
- `userId`

**Request Parameters**:
- `startDate`
- `endDate`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `exportExpensesExcel`

---

#### `GET /api/v1/budget/income/{userId}/export/csv`

**Path Variables**:
- `userId`

**Request Parameters**:
- `startDate`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `exportIncomesCSV`

---

#### `GET /api/v1/budget/income/{userId}/export/excel`

**Path Variables**:
- `userId`

**Request Parameters**:
- `startDate`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `exportIncomesExcel`

---

#### `GET /api/v1/budget/report/{userId}/pdf`

**Path Variables**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: `generatePDFReport`

---

#### `POST /api/v1/budget/report/{userId}/email`

**Path Variables**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: ``

---

#### `POST /api/v1/budget/expense/bulk-delete`

**Request Parameters**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: ``

---

#### `POST /api/v1/budget/expense/bulk-update-category`

**Request Parameters**:
- `userId`
- `category`
- `customCategoryName`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: ``

---

#### `GET /api/v1/budget/variance-analysis`

**Request Parameters**:
- `userId`

**Controller**: [`BudgetController`](src/main/java/com/budget/controller/BudgetController.java)
**Method**: ``

---

#### `GET/POST/PUT/DELETE /api/v1/budget/recurring/api/v1/budget/recurring`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: ``

---

#### `GET /api/v1/budget/recurring/{userId}`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: `getUserTemplates`

---

#### `GET /api/v1/budget/recurring/{userId}/active`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: `getActiveTemplates`

---

#### `POST /api/v1/budget/recurring`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: `createTemplate`

---

#### `PUT /api/v1/budget/recurring/{id}`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: `updateTemplate`

---

#### `DELETE /api/v1/budget/recurring/{id}`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: ``

---

#### `POST /api/v1/budget/recurring/{id}/toggle`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: `toggleActive`

---

#### `GET /api/v1/budget/recurring/{id}/upcoming`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: `getUpcomingDates`

---

#### `POST /api/v1/budget/recurring/generate`

**Controller**: [`RecurringTransactionController`](src/main/java/com/budget/controller/RecurringTransactionController.java)
**Method**: ``

---

#### `GET/POST/PUT/DELETE /api/v1/budget/tags/api/v1/budget/tags`

**Controller**: [`TagController`](src/main/java/com/budget/controller/TagController.java)
**Method**: ``

---

#### `GET /api/v1/budget/tags/{userId}`

**Controller**: [`TagController`](src/main/java/com/budget/controller/TagController.java)
**Method**: `getUserTags`

---

#### `GET /api/v1/budget/tags/detail/{tagId}`

**Controller**: [`TagController`](src/main/java/com/budget/controller/TagController.java)
**Method**: `getTagById`

---

#### `POST /api/v1/budget/tags`

**Controller**: [`TagController`](src/main/java/com/budget/controller/TagController.java)
**Method**: `createTag`

---

#### `PUT /api/v1/budget/tags/{tagId}`

**Controller**: [`TagController`](src/main/java/com/budget/controller/TagController.java)
**Method**: `updateTag`

---

#### `DELETE /api/v1/budget/tags/{tagId}`

**Controller**: [`TagController`](src/main/java/com/budget/controller/TagController.java)
**Method**: ``

---

### External Services

#### `GET/POST/PUT/DELETE /api/v1/external-services/api/v1/external-services`

**Controller**: [`ExternalServiceController`](src/main/java/com/externalServices/controller/ExternalServiceController.java)
**Method**: ``

---

#### `GET /api/v1/external-services`

**Controller**: [`ExternalServiceController`](src/main/java/com/externalServices/controller/ExternalServiceController.java)
**Method**: `getAllServices`

---

#### `GET /api/v1/external-services/{serviceName}`

**Controller**: [`ExternalServiceController`](src/main/java/com/externalServices/controller/ExternalServiceController.java)
**Method**: `getExternalService`

---

### Insurance

#### `GET/POST/PUT/DELETE /api/v1/insurance/api/v1/insurance`

**Controller**: [`InsuranceController`](src/main/java/com/protection/insurance/controller/InsuranceController.java)
**Method**: ``

---

#### `POST /api/v1/insurance`

**Controller**: [`InsuranceController`](src/main/java/com/protection/insurance/controller/InsuranceController.java)
**Method**: `createInsuranceDetails`

---

#### `GET /api/v1/insurance`

**Controller**: [`InsuranceController`](src/main/java/com/protection/insurance/controller/InsuranceController.java)
**Method**: ``

---

#### `GET /api/v1/insurance/user/{userId}`

**Controller**: [`InsuranceController`](src/main/java/com/protection/insurance/controller/InsuranceController.java)
**Method**: ``

---

#### `GET /api/v1/insurance/{id}`

**Controller**: [`InsuranceController`](src/main/java/com/protection/insurance/controller/InsuranceController.java)
**Method**: `getInsuranceDetailsById`

---

#### `DELETE /api/v1/insurance/{id}`

**Controller**: [`InsuranceController`](src/main/java/com/protection/insurance/controller/InsuranceController.java)
**Method**: `deleteInsurancePolicy`

---

### Investments - ETF

#### `GET/POST/PUT/DELETE /api/v1/etf/api/v1/etf`

**Controller**: [`EtfController`](src/main/java/com/investments/etf/controller/EtfController.java)
**Method**: ``

---

#### `GET /api/v1/etf`

**Controller**: [`EtfController`](src/main/java/com/investments/etf/controller/EtfController.java)
**Method**: `getAllEtfs`

---

#### `POST /api/v1/etf`

**Controller**: [`EtfController`](src/main/java/com/investments/etf/controller/EtfController.java)
**Method**: `addEtf`

---

#### `GET /api/v1/etf/{symbol}`

**Controller**: [`EtfController`](src/main/java/com/investments/etf/controller/EtfController.java)
**Method**: `getEtfBySymbol`

---

### Investments - Mutual Funds

#### `GET/POST/PUT/DELETE /api/v1/mutual-funds/api/v1/mutual-funds`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: ``

---

#### `GET /api/v1/mutual-funds/summary`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: `getSummary`

---

#### `GET /api/v1/mutual-funds/holdings`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: ``

---

#### `GET /api/v1/mutual-funds/insights`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: `getInsights`

---

#### `GET /api/v1/mutual-funds/external/search`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: ``

---

#### `GET /api/v1/mutual-funds/external/schemes`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: ``

---

#### `GET /api/v1/mutual-funds/external/schemes/{schemeCode}/nav`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: ``

---

#### `GET /api/v1/mutual-funds/external/schemes/{schemeCode}/latest`

**Controller**: [`MutualFundController`](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java)
**Method**: ``

---

### Investments - Stocks

#### `GET/POST/PUT/DELETE /api/v1/stocks/api/v1/stocks`

**Controller**: [`StockController`](src/main/java/com/investments/stocks/controller/StockController.java)
**Method**: ``

---

#### `GET /api/v1/stocks/{symbol}`

**Controller**: [`StockController`](src/main/java/com/investments/stocks/controller/StockController.java)
**Method**: `getStockBySymbol`

---

#### `GET/POST/PUT/DELETE /api/v1/portfolio/api/v1/portfolio`

**Controller**: [`PortfolioController`](src/main/java/com/investments/stocks/diversification/portfolio/controller/PortfolioController.java)
**Method**: ``

---

#### `POST /api/v1/portfolio`

**Controller**: [`PortfolioController`](src/main/java/com/investments/stocks/diversification/portfolio/controller/PortfolioController.java)
**Method**: `postPortfolioData`

---

#### `GET /api/v1/portfolio/summary/{userId}`

**Controller**: [`PortfolioController`](src/main/java/com/investments/stocks/diversification/portfolio/controller/PortfolioController.java)
**Method**: `getPortfolioSummary`

---

#### `GET/POST/PUT/DELETE /api/v1/net-worth/api/v1/net-worth`

**Controller**: [`NetWorthController`](src/main/java/com/investments/stocks/networth/controller/NetWorthController.java)
**Method**: ``

---

#### `GET /api/v1/net-worth/{userId}`

**Controller**: [`NetWorthController`](src/main/java/com/investments/stocks/networth/controller/NetWorthController.java)
**Method**: `getNetWorth`

---

#### `GET /api/v1/net-worth/template`

**Controller**: [`NetWorthController`](src/main/java/com/investments/stocks/networth/controller/NetWorthController.java)
**Method**: `getTemplates`

---

### Lending

#### `GET/POST/PUT/DELETE /api/v1/lending/api/v1/lending`

**Controller**: [`LendingController`](src/main/java/com/lending/controller/LendingController.java)
**Method**: ``

---

#### `POST /api/v1/lending`

**Controller**: [`LendingController`](src/main/java/com/lending/controller/LendingController.java)
**Method**: `addLending`

---

#### `GET /api/v1/lending`

**Controller**: [`LendingController`](src/main/java/com/lending/controller/LendingController.java)
**Method**: ``

---

#### `GET /api/v1/lending/{id}`

**Controller**: [`LendingController`](src/main/java/com/lending/controller/LendingController.java)
**Method**: `getLendingById`

---

#### `POST /api/v1/lending/{id}/repayment`

**Path Variables**:
- `id`

**Controller**: [`LendingController`](src/main/java/com/lending/controller/LendingController.java)
**Method**: `addRepayment`

---

#### `PUT /api/v1/lending/{id}/close`

**Controller**: [`LendingController`](src/main/java/com/lending/controller/LendingController.java)
**Method**: `closeLending`

---

### Other

#### `GET/POST/PUT/DELETE /api/v1/aa/api/v1/aa`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: ``

---

#### `GET /api/v1/aa/consent/templates`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: ``

---

#### `POST /api/v1/aa/consent`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: `createConsent`

---

#### `GET /api/v1/aa/consent/{consentId}/status`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: `getConsentStatus`

---

#### `POST /api/v1/aa/fetch`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: `initiateFetch`

---

#### `GET /api/v1/aa/fetch/{requestId}/status`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: `getFetchStatus`

---

#### `GET /api/v1/aa/fetch/{requestId}/data`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: `getDecryptedData`

---

#### `DELETE /api/v1/aa/consent/{consentId}`

**Controller**: [`AAController`](src/main/java/com/aa/controller/AAController.java)
**Method**: `revokeConsent`

---

#### `GET/POST/PUT/DELETE /api/v1/test-runner/api/v1/test-runner`

**Controller**: [`TestRunnerController`](src/main/java/com/api/testrunner/TestRunnerController.java)
**Method**: ``

---

#### `POST /api/v1/test-runner/run`

**Controller**: [`TestRunnerController`](src/main/java/com/api/testrunner/TestRunnerController.java)
**Method**: `runTests`

---

#### `GET /api/v1/test-runner/status`

**Controller**: [`TestRunnerController`](src/main/java/com/api/testrunner/TestRunnerController.java)
**Method**: `getStatus`

---

#### `GET/POST/PUT/DELETE /api/v1/super-admin/api/v1/super-admin`

**Controller**: [`SuperAdminController`](src/main/java/com/auth/controller/SuperAdminController.java)
**Method**: ``

---

#### `POST /api/v1/super-admin/update-role/{userId}`

**Controller**: [`SuperAdminController`](src/main/java/com/auth/controller/SuperAdminController.java)
**Method**: `updateRole`

---

#### `GET/POST/PUT/DELETE /api/v1/alerts/api/v1/alerts`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `GET /api/v1/alerts`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `GET /api/v1/alerts/unread`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `GET /api/v1/alerts/month/{monthYear}`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `GET /api/v1/alerts/summary`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `PUT /api/v1/alerts/{alertId}/read`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `PUT /api/v1/alerts/read-all`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `DELETE /api/v1/alerts/{alertId}`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `DELETE /api/v1/alerts`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `POST /api/v1/alerts/check`

**Controller**: [`AlertController`](src/main/java/com/budget/controller/AlertController.java)
**Method**: ``

---

#### `GET/POST/PUT/DELETE /open/dev/migration/open/dev/migration`

**Controller**: [`MigrationGeneratorController`](src/main/java/com/common/devtools/migration/MigrationGeneratorController.java)
**Method**: ``

---

#### `POST /open/dev/migration/generate`

**Controller**: [`MigrationGeneratorController`](src/main/java/com/common/devtools/migration/MigrationGeneratorController.java)
**Method**: `generateMigration`

---

#### `GET/POST/PUT/DELETE /api/health/api/health`

**Controller**: [`HealthCheckController`](src/main/java/com/healthstatus/controller/HealthCheckController.java)
**Method**: ``

---

#### `GET /api/health`

**Controller**: [`HealthCheckController`](src/main/java/com/healthstatus/controller/HealthCheckController.java)
**Method**: `getHealth`

---

#### `GET/POST/PUT/DELETE /api/v1/loans/api/v1/loans`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: ``

---

#### `POST /api/v1/loans/create`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: `createLoan`

---

#### `GET /api/v1/loans/all`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: ``

---

#### `GET /api/v1/loans/user/{userId}`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: ``

---

#### `GET /api/v1/loans/{id}`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: `getLoanById`

---

#### `DELETE /api/v1/loans/{id}`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: `deleteLoan`

---

#### `POST /api/v1/loans/{id}/simulate-prepayment`

**Controller**: [`LoanController`](src/main/java/com/loan/controller/LoanController.java)
**Method**: ``

---

#### `GET/POST/PUT/DELETE api/v1/fixed-depositapi/v1/fixed-deposit`

**Controller**: [`FixedDepositController`](src/main/java/com/savings/controller/FixedDepositController.java)
**Method**: ``

---

#### `POST api/v1/fixed-deposit`

**Controller**: [`FixedDepositController`](src/main/java/com/savings/controller/FixedDepositController.java)
**Method**: `createFixedDeposit`

---

#### `GET api/v1/fixed-deposit/{id}`

**Controller**: [`FixedDepositController`](src/main/java/com/savings/controller/FixedDepositController.java)
**Method**: `getFixedDeposit`

---

#### `GET api/v1/fixed-deposit/user/{userId}`

**Controller**: [`FixedDepositController`](src/main/java/com/savings/controller/FixedDepositController.java)
**Method**: `getAllFixedDeposits`

---

#### `PUT api/v1/fixed-deposit/{id}`

**Path Variables**:
- `id`

**Request Parameters**:
- `userId`

**Controller**: [`FixedDepositController`](src/main/java/com/savings/controller/FixedDepositController.java)
**Method**: `updateFixedDeposit`

---

#### `DELETE api/v1/fixed-deposit/{id}`

**Controller**: [`FixedDepositController`](src/main/java/com/savings/controller/FixedDepositController.java)
**Method**: `deleteFixedDeposit`

---

#### `GET/POST/PUT/DELETE api/v1/recurring-depositapi/v1/recurring-deposit`

**Controller**: [`RecurringDepositController`](src/main/java/com/savings/controller/RecurringDepositController.java)
**Method**: ``

---

#### `POST api/v1/recurring-deposit`

**Controller**: [`RecurringDepositController`](src/main/java/com/savings/controller/RecurringDepositController.java)
**Method**: `createRecurringDeposit`

---

#### `GET api/v1/recurring-deposit/{id}`

**Controller**: [`RecurringDepositController`](src/main/java/com/savings/controller/RecurringDepositController.java)
**Method**: `getRecurringDeposit`

---

#### `GET api/v1/recurring-deposit/user/{userId}`

**Controller**: [`RecurringDepositController`](src/main/java/com/savings/controller/RecurringDepositController.java)
**Method**: `getAllRecurringDeposits`

---

#### `PUT api/v1/recurring-deposit/{id}`

**Path Variables**:
- `id`

**Request Parameters**:
- `userId`

**Controller**: [`RecurringDepositController`](src/main/java/com/savings/controller/RecurringDepositController.java)
**Method**: `updateRecurringDeposit`

---

#### `DELETE api/v1/recurring-deposit/{id}`

**Controller**: [`RecurringDepositController`](src/main/java/com/savings/controller/RecurringDepositController.java)
**Method**: `deleteRecurringDeposit`

---

#### `GET/POST/PUT/DELETE api/v1/taxapi/v1/tax`

**Controller**: [`TaxController`](src/main/java/com/tax/controller/TaxController.java)
**Method**: ``

---

#### `POST api/v1/tax`

**Controller**: [`TaxController`](src/main/java/com/tax/controller/TaxController.java)
**Method**: `createTaxDetails`

---

#### `GET api/v1/tax/{userId}`

**Controller**: [`TaxController`](src/main/java/com/tax/controller/TaxController.java)
**Method**: `getTaxDetails`

---

#### `GET api/v1/tax/{userId}/liability`

**Controller**: [`TaxController`](src/main/java/com/tax/controller/TaxController.java)
**Method**: `getOutstandingTaxLiability`

---

### Savings

#### `GET/POST/PUT/DELETE /api/v1/savings-accounts/api/v1/savings-accounts`

**Controller**: [`SavingsController`](src/main/java/com/savings/controller/SavingsController.java)
**Method**: ``

---

#### `POST /api/v1/savings-accounts`

**Controller**: [`SavingsController`](src/main/java/com/savings/controller/SavingsController.java)
**Method**: `postSavingsAccountDetails`

---

#### `GET /api/v1/savings-accounts`

**Controller**: [`SavingsController`](src/main/java/com/savings/controller/SavingsController.java)
**Method**: `getAllSavingsAccounts`

---

#### `GET /api/v1/savings-accounts/{id}`

**Path Variables**:
- `id`

**Controller**: [`SavingsController`](src/main/java/com/savings/controller/SavingsController.java)
**Method**: `getSavingsAccountById`

---

#### `PUT /api/v1/savings-accounts/{id}`

**Path Variables**:
- `id`

**Request Parameters**:
- `userId`

**Controller**: [`SavingsController`](src/main/java/com/savings/controller/SavingsController.java)
**Method**: `updateSavingsAccount`

---

#### `DELETE /api/v1/savings-accounts/{id}`

**Path Variables**:
- `id`

**Controller**: [`SavingsController`](src/main/java/com/savings/controller/SavingsController.java)
**Method**: ``

---

## 🎛️ Controller Index

| Controller | Endpoints | Base Path | Location |
|------------|-----------|-----------|----------|
| `AAController` | 8 | `/api/v1/aa` | [View](src/main/java/com/aa/controller/AAController.java) |
| `AdminController` | 12 | `/api/v1/admin` | [View](src/main/java/com/auth/controller/AdminController.java) |
| `AlertController` | 10 | `/api/v1/alerts` | [View](src/main/java/com/budget/controller/AlertController.java) |
| `AuthController` | 9 | `/api/auth` | [View](src/main/java/com/auth/controller/AuthController.java) |
| `BudgetController` | 33 | `/api/v1/budget` | [View](src/main/java/com/budget/controller/BudgetController.java) |
| `EtfController` | 4 | `/api/v1/etf` | [View](src/main/java/com/investments/etf/controller/EtfController.java) |
| `ExternalServiceController` | 3 | `/api/v1/external-services` | [View](src/main/java/com/externalServices/controller/ExternalServiceController.java) |
| `FixedDepositController` | 6 | `api/v1/fixed-deposit` | [View](src/main/java/com/savings/controller/FixedDepositController.java) |
| `HealthCheckController` | 2 | `/api/health` | [View](src/main/java/com/healthstatus/controller/HealthCheckController.java) |
| `InsuranceController` | 6 | `/api/v1/insurance` | [View](src/main/java/com/protection/insurance/controller/InsuranceController.java) |
| `LendingController` | 6 | `/api/v1/lending` | [View](src/main/java/com/lending/controller/LendingController.java) |
| `LoanController` | 7 | `/api/v1/loans` | [View](src/main/java/com/loan/controller/LoanController.java) |
| `MigrationGeneratorController` | 2 | `/open/dev/migration` | [View](src/main/java/com/common/devtools/migration/MigrationGeneratorController.java) |
| `MutualFundController` | 8 | `/api/v1/mutual-funds` | [View](src/main/java/com/investments/mutualfunds/controller/MutualFundController.java) |
| `NetWorthController` | 3 | `/api/v1/net-worth` | [View](src/main/java/com/investments/stocks/networth/controller/NetWorthController.java) |
| `PortfolioController` | 3 | `/api/v1/portfolio` | [View](src/main/java/com/investments/stocks/diversification/portfolio/controller/PortfolioController.java) |
| `RecurringDepositController` | 6 | `api/v1/recurring-deposit` | [View](src/main/java/com/savings/controller/RecurringDepositController.java) |
| `RecurringTransactionController` | 9 | `/api/v1/budget/recurring` | [View](src/main/java/com/budget/controller/RecurringTransactionController.java) |
| `SavingsController` | 6 | `/api/v1/savings-accounts` | [View](src/main/java/com/savings/controller/SavingsController.java) |
| `StockController` | 2 | `/api/v1/stocks` | [View](src/main/java/com/investments/stocks/controller/StockController.java) |
| `SuperAdminController` | 2 | `/api/v1/super-admin` | [View](src/main/java/com/auth/controller/SuperAdminController.java) |
| `TagController` | 6 | `/api/v1/budget/tags` | [View](src/main/java/com/budget/controller/TagController.java) |
| `TaxController` | 4 | `api/v1/tax` | [View](src/main/java/com/tax/controller/TaxController.java) |
| `TestRunnerController` | 3 | `/api/v1/test-runner` | [View](src/main/java/com/api/testrunner/TestRunnerController.java) |
