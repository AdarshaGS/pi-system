# 🗺️ Module Improvement Roadmap

> **Created**: February 22, 2026
> **Scope**: Net Worth, Portfolio Risk Engine, Budget Planner, Loan Calculations, Insurance Calculations, Stock Calculations
> **Purpose**: Prioritised list of gaps and improvements — tackle one by one in order

---

## 📈 Priority Action Matrix

| # | Priority | Module | Action |
|---|----------|--------|--------|
| 1 | 🔴 Critical | **All Modules** | Create `UserProfile` (income, dependents, risk tolerance) |
| 2 | 🔴 Critical | **Insurance** | Build frontend UI (backend is 100% done) |
| 3 | 🔴 Critical | **Net Worth** | Wire Mutual Fund & ETF values, add historical snapshots |
| 4 | 🟠 High | **Stocks** | XIRR calculation, fix `$` → `₹`, wire price alerts |
| 5 | 🟠 High | **Budget** | Month-end spend projection, loan EMI auto-import |
| 6 | 🟠 High | **Loans** | Debt-to-income ratio API, floating rate simulator |
| 7 | 🟡 Medium | **Risk Engine** | Beta, Sharpe Ratio, personalised risk profile matching |
| 8 | 🟡 Medium | **Insurance** | Pull real income data for coverage recommendations, expiry alerts |
| 9 | 🟢 Lower | **All Modules** | API integration tests (0% coverage everywhere) |

---

## 🔴 CRITICAL — Do First

---

### ✅ Item 1 — Create `UserProfile` Entity (All Modules)

**Why this unblocks everything**: Multiple modules hardcode or ignore user income, dependents, and risk tolerance. A single profile entity fixes recommendations across Insurance, Loans, Budget, and Net Worth simultaneously.

**Fields needed:**
- `annualIncome` / `monthlyIncome`
- `employmentType` (SALARIED, SELF_EMPLOYED, BUSINESS, RETIRED)
- `dependents` (number of dependents)
- `riskTolerance` (CONSERVATIVE, MODERATE, AGGRESSIVE)
- `city` / `cityTier` (METRO, TIER_1, TIER_2)
- `age` / `dateOfBirth`

**Files to create:**
```
src/main/java/com/users/data/UserProfile.java
src/main/java/com/users/repo/UserProfileRepository.java
src/main/java/com/users/service/UserProfileService.java
src/main/java/com/users/controller/UserProfileController.java
```

**Hardcoded values this fixes:**
- `InsuranceServiceImpl.java` — `assumedAnnualIncome = ₹10L` → replace with `userProfile.getAnnualIncome()`
- `InsuranceServiceImpl.java` — `dependentsCount = 0` → replace with `userProfile.getDependents()`
- `InsuranceServiceImpl.java` — `city = "Metro"` → replace with `userProfile.getCity()`
- `InsuranceServiceImpl.java` — `outstandingLiabilities = 0` → pull from LoanService
- `RetirementPlanningService.java` — `"$"` currency symbol → replace with `"₹"`

**API Endpoints:**
```
POST   /api/v1/users/{userId}/profile    - Create/update profile
GET    /api/v1/users/{userId}/profile    - Get profile
```

---

### ✅ Item 2 — Insurance Frontend UI

**Why**: Backend is 100% complete (11 endpoints, 3 DTOs, premium + claims + coverage analysis). Frontend is 0% built. Users cannot access any insurance features.

**Pages to build:**
- [ ] Insurance Dashboard — list all policies, summary cards (Total Coverage, Total Premium, Active Policies), filter by type
- [ ] Add Insurance Form — type selector, provider, policy number, premium amount & frequency, cover amount, start/end dates, next premium date, nominee details
- [ ] Policy Details Page — full info, premium history, claims history, edit/delete buttons
- [ ] Record Premium Modal — payment amount, date, method, auto-renewal toggle
- [ ] File Claim Modal — claim number, amount, incident date, description, status
- [ ] Coverage Analysis Panel — life adequacy, health adequacy, gap visualisation

**API to wire up (already exist):**
```
GET    /api/v1/insurance/user/{userId}
GET    /api/v1/insurance/{id}
GET    /api/v1/insurance/{id}/premiums
GET    /api/v1/insurance/{id}/claims
GET    /api/v1/insurance/user/{userId}/analysis
POST   /api/v1/insurance
POST   /api/v1/insurance/{id}/premium
POST   /api/v1/insurance/{id}/claim
DELETE /api/v1/insurance/{id}
```

---

### ✅ Item 3 — Net Worth: Wire MF/ETF + Historical Snapshots

**Why**: Mutual Funds and ETFs are listed as asset templates in the UI but their values are silently excluded from the net worth total. Historical snapshots don't exist — no chart is possible.

**Sub-tasks:**

#### 3a — Wire MF & ETF into `NetWorthReadServiceImpl`
- Inject `MutualFundService` (or `EtfService`) and add their current value to `assetBreakdown`
- Similar to how `savingsValue` is computed for savings accounts

```java
// Add to NetWorthReadServiceImpl.getNetWorth()
BigDecimal mfValue = BigDecimal.ZERO;
try {
    // fetch user's MF holdings and sum current value
    mfValue = mutualFundService.getTotalCurrentValue(userId);
} catch (Exception ignored) {}
assetBreakdown.merge(EntityType.MUTUAL_FUND, mfValue, BigDecimal::add);
```

#### 3b — Create `net_worth_snapshots` table
```sql
CREATE TABLE net_worth_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_assets DECIMAL(19,2),
    total_liabilities DECIMAL(19,2),
    net_worth DECIMAL(19,2),
    net_worth_after_tax DECIMAL(19,2),
    snapshot_json TEXT,  -- full breakdown as JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_date (user_id, snapshot_date)
);
```

#### 3c — Add a Monthly Snapshot Scheduler
```java
// Runs on the 1st of every month
@Scheduled(cron = "0 0 6 1 * *")
public void captureMonthlyNetWorthSnapshot() { ... }
```

#### 3d — Add History API
```
GET /api/v1/networth/{userId}/history?months=12   - Returns last N monthly snapshots
GET /api/v1/networth/{userId}/trend               - Returns net worth change % MoM, YoY
```

---

## 🟠 HIGH — Do Next

---

### ✅ Item 4 — Stocks: XIRR, Currency Fix, Price Alerts

**Why**: XIRR is the single most important return metric for Indian investors. The `$` currency bug is embarrassing. Price alerts silently fire into a TODO comment.

#### 4a — Fix Currency Symbol
**File**: `src/main/java/com/investments/stocks/service/RetirementPlanningService.java`
```java
// Line 120 — Replace:
"Increase monthly contributions by $" + additionalContribution
// With:
"Increase monthly contributions by ₹" + additionalContribution
```

#### 4b — Implement XIRR
- Use Newton-Raphson iteration to solve for the rate `r` in: `Σ CFi / (1+r)^(ti/365) = 0`
- Inputs: list of `(portfolioTransaction.date, portfolioTransaction.amount)` + current portfolio value as final cash flow
- Output: annualised return rate

```java
// New service method in StockReadService / PortfolioReadService
BigDecimal calculateXIRR(Long userId);
```

**New endpoint:**
```
GET /api/v1/portfolio/{userId}/xirr   - Returns XIRR as a percentage
```

#### 4c — Wire Price Alert Notifications
**File**: `StockManagementServiceImpl.java` around line 280
```java
// Replace the TODO comment with:
notificationService.sendNotification(
    userId,
    "Stock Price Alert",
    String.format("Stock %s has reached your target price of ₹%.2f", symbol, targetPrice),
    NotificationType.ALERT,
    AlertChannel.IN_APP,
    Map.of("symbol", symbol, "price", currentPrice.toString()),
    alertRuleId
);
```

#### 4d — Realised vs. Unrealised P&L Split
- Add `isRealised` flag to `PortfolioTransaction`
- Compute `realisedPnl` (from sell transactions) and `unrealisedPnl` (from current holdings) separately
- Add to portfolio summary response

---

### ✅ Item 5 — Budget: Month-End Projection + EMI Auto-Import

**Why**: The budget module is powerful but blind to future obligations and can't tell users if they're on pace to overspend.

#### 5a — Month-End Spend Projection
Add to `BudgetService`:

```java
public BudgetProjectionDTO getMonthEndProjection(Long userId, String monthYear) {
    // 1. Get total spent so far this month
    // 2. Get current day of month
    // 3. Projected = (spent / currentDay) * totalDaysInMonth
    // 4. Compare projected vs budget → surplus or deficit
    // 5. Per-category projection
}
```

**New endpoint:**
```
GET /api/v1/budgets/{userId}/projection?month=2026-02   - Month-end spend forecast
```

#### 5b — Auto-Import EMI Obligations
- In `getMonthlyReport()`, pull all active loans for the user from `LoanService`
- Add each loan's `emiAmount` as a fixed obligation line in the report
- Deduct from disposable income to show "true" remaining budget

#### 5c — Auto-Import Insurance Premiums
- Pull monthly-due premiums from `InsuranceService`
- Add to fixed obligations alongside EMIs

#### 5d — Spending Anomaly Alert
- Compare this month's per-category spend vs 3-month rolling average
- If any category is >150% of average, generate an alert

#### 5e — Auto-Categorization of Expenses
- Build a keyword-based merchant mapping engine (e.g., "Amazon" -> SHOPPING, "Zomato" -> FOOD).
- Implement predictive categorization using user's transaction history.
- API to suggest category for a given description.

---

### ✅ Item 6 — Loans: Debt-to-Income Ratio + Floating Rate Simulator

**Why**: EMI burden is the #1 loan health metric lenders and financial advisors use. Indian home loan holders are highly sensitive to RBI rate changes.

#### 6a — Debt-to-Income (DTI) Ratio API
Create new endpoint:
```
GET /api/v1/loans/user/{userId}/debt-analysis
```
Response:
```json
{
  "totalMonthlyEmi": 45000,
  "monthlyIncome": 120000,
  "debtToIncomeRatio": 37.5,
  "status": "MODERATE",       // GREEN < 30%, YELLOW 30-40%, RED > 40%
  "recommendation": "Your EMI burden is moderate. Avoid taking new loans.",
  "loanBreakdown": [...]
}
```

#### 6b — Floating Rate Impact Simulator
New endpoint:
```
POST /api/v1/loans/{id}/rate-impact
Body: { "rateChangePct": 0.50 }    // e.g., +0.5% RBI hike
```
Response:
```json
{
  "currentRate": 8.5,
  "newRate": 9.0,
  "currentEmi": 22000,
  "newEmi": 23150,
  "emiIncrease": 1150,
  "additionalTotalInterest": 41400,
  "additionalTenureMonths": 2
}
```

#### 6c — Loan Comparison Tool
New endpoint:
```
POST /api/v1/loans/compare
Body: { "loan1": {...}, "loan2": {...} }
```
Returns total cost of each option with recommendation.

#### 6d — Fix Generic RuntimeExceptions
Create `src/main/java/com/loan/exception/LoanValidationException.java` and replace all `throw new RuntimeException(...)` in `LoanServiceImpl`.

---

## 🟡 MEDIUM — Do After High Priority

---

### ✅ Item 7 — Portfolio Risk Engine: Beta, Sharpe Ratio, Risk Profile

**Why**: The current risk engine is purely rule-based (thresholds). Adding statistical measures elevates it to a professional-grade tool.

#### 7a — Portfolio Beta
- Store weekly price history in `stock_prices` table (already exists)
- Calculate weekly returns for each stock and for Nifty 50 index
- Beta = `Cov(stock, market) / Var(market)`
- Weighted portfolio beta = `Σ (weight_i × beta_i)`

**New endpoint:**
```
GET /api/v1/portfolio/{userId}/beta
```

#### 7b — Sharpe Ratio
```java
// sharpeRatio = (portfolioReturn - riskFreeRate) / portfolioStdDev
// Use 6.5% (10-yr G-Sec) as risk-free rate
BigDecimal calculateSharpeRatio(Long userId, double riskFreeRate);
```

**New endpoint:**
```
GET /api/v1/portfolio/{userId}/sharpe-ratio
```

#### 7c — Personalised Risk Profile Matching
- After `UserProfile` (Item 1) is done, compare portfolio risk score against user's declared `riskTolerance`
- Add `riskAlignment` field to `PortfolioScoringResult`:
  - `ALIGNED` — portfolio risk matches tolerance
  - `TOO_AGGRESSIVE` — portfolio riskier than tolerance
  - `TOO_CONSERVATIVE` — portfolio safer than tolerance, leaving returns on the table

#### 7d — Remove 90-Score Cap
**File**: `PortfolioScoringService.java` line 58–60
```java
// Remove or change this:
if (score > 90) score = 90;
// To: allow scores up to 100 for genuinely well-diversified portfolios
```

---

### ✅ Item 8 — Insurance: Real Income Data + Expiry Alerts

**Why**: After `UserProfile` (Item 1) is built, insurance recommendations can be immediately made accurate. Expiry alerts prevent lapsed policies.

#### 8a — Pull Real Income & Dependents from UserProfile
**File**: `InsuranceServiceImpl.analyzeCoverage()`
```java
// Replace hardcoded values:
UserProfile profile = userProfileService.getProfile(userId);
BigDecimal annualIncome = profile.getAnnualIncome();           // was: 1000000
int dependentsCount = profile.getDependents();                  // was: 0
String city = profile.getCity();                               // was: "Metro"

// Recommended life cover = 10-15x annual income + outstanding loans
BigDecimal outstandingLoans = loanService.getTotalOutstanding(userId);
BigDecimal recommendedLifeCoverage = annualIncome.multiply(BigDecimal.valueOf(10))
    .add(outstandingLoans);
```

#### 8b — Policy Expiry Alert Scheduler
```java
@Scheduled(cron = "0 0 9 * * *")  // Daily at 9 AM
public void checkPolicyExpiry() {
    // Find all policies where endDate is within 30, 60, or 90 days
    // Send notification to user for each
}
```

#### 8c — Premium Due Date Scheduler
```java
@Scheduled(cron = "0 0 9 * * *")  // Daily at 9 AM
public void checkPremiumDueDates() {
    // Find premiums where nextPremiumDate is within 7 days
    // Send reminder notification
}
```

#### 8d — Vehicle Insurance IDV Check
- In `analyzeCoverage()`, add vehicle insurance section
- Recommend IDV = current market value of vehicle (user-inputted)
- Flag if policy cover < market value

---

## 🟢 LOWER PRIORITY — After Medium Complete

---

### 🟡 MEDIUM: Item 9 — API Integration Tests
TBD

### 🔵 NEW: Item 10 — AI Financial Assistant (Context-Aware Chat)
*   **Goal**: Provide a premium chat experience that analyzes user data and suggests structural improvements.
*   **Key Files**: 
    *   `AiAssistantController.java`
    *   `AiAssistantService.java`
    *   `FinancialContextService.java`
*   **Detailed Technical Strategy**: See [AI_ASSISTANT_IMPROVEMENT_THINKINGS.md](./AI_ASSISTANT_IMPROVEMENT_THINKINGS.md)
*   **Prompt Logic**: 
    *   "You are Pi-Assistant, a premium financial architect."
    *   "Analyze the provided JSON context of the user's Net Worth, Budget, and Loans."
    *   "Identify gaps like high interest loans vs low return assets, or lack of emergency fund."
    *   "CRITICAL: Do not suggest specific stocks or mutual funds."
**Why**: Current test coverage is 0% for most modules. Required before any production deployment.

| Module | Endpoints | Tests Needed |
|--------|-----------|-------------|
| Insurance | 11 | ~15 tests |
| Loans | 15 | ~20 tests |
| Budget | 20+ | ~25 tests |
| Stocks / Portfolio | 30+ | ~40 tests |
| Net Worth | 5 | ~8 tests |
| Risk Engine | calculated | ~10 tests |

**Test locations:**
```
src/test/java/com/api/
├── insurance/InsuranceControllerIntegrationTest.java
├── loan/LoanControllerIntegrationTest.java
├── budget/BudgetControllerIntegrationTest.java
├── stocks/StockControllerIntegrationTest.java
├── networth/NetWorthControllerIntegrationTest.java
└── portfolio/PortfolioControllerIntegrationTest.java
```

---

## 🔁 Cross-Cutting Improvements (Apply Alongside All Items)

These should be done incrementally as you touch each module:

| Improvement | What to Do |
|-------------|-----------|
| **Replace generic RuntimeExceptions** | Create domain-specific exceptions per module (e.g., `LoanValidationException`, `InsurancePolicyNotFoundException`) |
| **Unified Financial Health Score** | After Items 1–6 done, build `GET /api/v1/users/{userId}/financial-health` combining: savings rate + DTI ratio + coverage adequacy + portfolio risk score |
| **Notification wiring** | Each module should call `NotificationService` on key events (EMI due, policy expiring, budget overspent, price alert) |
| **Currency consistency** | Search codebase for `"$"` and replace with `"₹"` — especially in RetirementPlanningService |
| **UserController & SettingsController** | Profile management APIs missing entirely — documented in `MISSING_FEATURES_ANALYSIS.md` |

---

## 📌 Progress Tracker

Update this as you complete each item:

- [x] **Item 1** — UserProfile Entity (All Modules)
- [ ] **Item 2** — Insurance Frontend UI
- [ ] **Item 3** — Net Worth: Wire MF/ETF + Historical Snapshots
- [x] **Item 4** — Stocks: XIRR, Currency Fix, Price Alerts
- [ ] **Item 5** — Budget: Month-End Projection + EMI Auto-Import
- [ ] **Item 6** — Loans: DTI Ratio + Floating Rate Simulator
- [ ] **Item 7** — Risk Engine: Beta + Sharpe Ratio + Risk Profile
- [ ] **Item 8** — Insurance: Real Income Data + Expiry Alerts
- [ ] **Item 9** — API Integration Tests (All Modules)
- [x] **Item 10** — AI Financial Assistant (Context-Aware Chat)
