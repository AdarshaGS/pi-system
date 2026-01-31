# Missing Integration Tests

**Last Updated**: 31 January 2026

## Current Test Status

- ✅ **53 tests PASSING**
- ⏭️ **7 tests SKIPPED** (awaiting prerequisites)
- ❌ **0 tests FAILING**

**Total Test Coverage**: 60 integration tests across 11 controllers

---

## 1. Skipped Tests (Requires Data Seeding)

### Portfolio Controller (5 tests skipped)
**Reason**: Requires stock data in `stocks` table

#### Missing Tests:
1. ❌ `testAddStockToPortfolio` - Add stock to portfolio successfully
2. ❌ `testGetUserPortfolio` - Get user portfolio summary
3. ❌ `testInvalidStockSymbol` - Validate stock symbol format
4. ❌ `testNegativeQuantity` - Validate positive quantity
5. ❌ `testPortfolioTotalValue` - Calculate portfolio total value

**Prerequisites**:
- Seed `stocks` table with test data (RELIANCE, TCS, INFY, ITC, SBIN, ICICIBANK, WIPRO, HDFC)
- Seed `sectors` table with sector data
- Set up stock price data

**Impact**: Portfolio management is a core feature - HIGH PRIORITY

---

### Mutual Fund Controller (2 tests skipped)
**Reason**: Requires Account Aggregator (AA) consent setup

#### Missing Tests:
1. ❌ `testGetMutualFundHoldings` - Get mutual fund holdings via AA
2. ❌ `testGetMutualFundSummary` - Get mutual fund summary via AA

**Prerequisites**:
- Set up mock AA consent in test database
- Configure AA consent with `ACTIVE` status
- Mock AA FI data response

**Impact**: MF tracking via AA is read-only - MEDIUM PRIORITY

---

## 2. Controllers Without Integration Tests

### High Priority Controllers

#### StockController
**Endpoints**:
- `GET /api/v1/stocks` - Get all stocks
- `GET /api/v1/stocks/{symbol}` - Get stock by symbol
- `GET /api/v1/stocks/price/{symbol}` - Get stock price

**Missing Tests** (3 tests):
1. Should get all stocks successfully
2. Should get stock by symbol
3. Should get stock price with cache fallback

**Complexity**: Medium (requires stock data seeding)

---

#### InsuranceController
**Endpoints**:
- `POST /api/v1/insurance` - Create insurance policy
- `GET /api/v1/insurance/user/{userId}` - Get user policies
- `GET /api/v1/insurance/{id}` - Get policy by ID
- `PUT /api/v1/insurance/{id}` - Update insurance policy
- `DELETE /api/v1/insurance/{id}` - Delete insurance policy

**Missing Tests** (7-10 tests):
1. Should create life insurance policy
2. Should create health insurance policy
3. Should get all user policies
4. Should get policy by ID
5. Should update policy successfully
6. Should delete policy successfully
7. Should validate required fields
8. Should prevent unauthorized access
9. Should calculate premium correctly
10. Should track policy expiry

**Complexity**: Medium

---

#### TaxController
**Endpoints**:
- `POST /api/v1/tax` - Create tax details
- `GET /api/v1/tax/user/{userId}` - Get tax details by user
- `GET /api/v1/tax/liability/{userId}` - Calculate total tax liability
- `GET /api/v1/tax/outstanding/{userId}` - Get outstanding tax liability

**Missing Tests** (6-8 tests):
1. Should create tax details successfully
2. Should get tax details by user and year
3. Should calculate total tax liability
4. Should calculate outstanding liability
5. Should validate financial year format
6. Should prevent duplicate tax records
7. Should calculate tax based on income slabs
8. Should handle deductions correctly

**Complexity**: High (complex tax calculations)

---

#### LendingController
**Endpoints**:
- `POST /api/v1/lending` - Create P2P lending
- `GET /api/v1/lending/user/{userId}` - Get user lending
- `GET /api/v1/lending/{id}` - Get lending by ID
- `PUT /api/v1/lending/{id}/status` - Update lending status
- `GET /api/v1/lending/returns/{userId}` - Calculate returns

**Missing Tests** (8-10 tests):
1. Should create P2P lending successfully
2. Should get all user lending
3. Should get lending by ID
4. Should update lending status
5. Should calculate lending returns
6. Should validate minimum lending amount
7. Should track lending tenure
8. Should calculate interest earned
9. Should handle lending maturity
10. Should prevent unauthorized access

**Complexity**: High (financial calculations, state management)

---

### Medium Priority Controllers

#### AAController (Account Aggregator)
**Endpoints**:
- `GET /api/v1/aa/consent-templates` - Get consent templates
- `POST /api/v1/aa/consent` - Create AA consent
- `GET /api/v1/aa/consent/{consentId}/status` - Get consent status
- `POST /api/v1/aa/fi-request` - Request financial information
- `DELETE /api/v1/aa/consent/{consentId}` - Revoke consent

**Missing Tests** (8-10 tests):
1. Should get consent templates successfully
2. Should create AA consent
3. Should get consent status
4. Should request FI data
5. Should revoke consent successfully
6. Should validate consent expiry
7. Should handle consent approval flow
8. Should validate FI type filters
9. Should handle consent rejection
10. Should track consent history

**Complexity**: High (integration with external AA system)

---

#### DeveloperToolsController
**Endpoints**:
- `POST /api/v1/dev/migration/generate` - Generate migration
- `GET /api/v1/dev/health` - System health check
- `POST /api/v1/dev/seed` - Seed test data

**Missing Tests** (3-5 tests):
1. Should generate migration successfully
2. Should check system health
3. Should seed test data
4. Should validate admin access only
5. Should be disabled in production

**Complexity**: Low (admin utilities)

---

#### UserController
**Endpoints**:
- `GET /api/v1/users/{userId}` - Get user profile
- `PUT /api/v1/users/{userId}` - Update user profile
- `DELETE /api/v1/users/{userId}` - Delete user account
- `GET /api/v1/users/{userId}/settings` - Get user settings
- `PUT /api/v1/users/{userId}/settings` - Update user settings

**Missing Tests** (7-10 tests):
1. Should get user profile successfully
2. Should update user profile
3. Should delete user account
4. Should get user settings
5. Should update user settings
6. Should prevent unauthorized profile access
7. Should validate email format on update
8. Should validate phone number format
9. Should handle profile picture upload
10. Should track last login timestamp

**Complexity**: Medium

---

## 3. Missing Test Scenarios (Existing Controllers)

### AuthController (10 tests exist)
**Additional Scenarios Needed**:
1. ✅ Basic auth flows covered
2. ❌ Password reset flow (forgot password)
3. ❌ Email verification flow
4. ❌ OAuth2 login flow (if enabled)
5. ❌ Token expiration handling
6. ❌ Concurrent session management
7. ❌ Brute force protection

**Additional Tests**: 6-8 tests

---

### SavingsAccountController (9 tests exist)
**Additional Scenarios Needed**:
1. ✅ CRUD operations covered
2. ❌ Bulk savings account import
3. ❌ Account balance reconciliation
4. ❌ Interest calculation over time
5. ❌ Account statement generation

**Additional Tests**: 4-5 tests

---

### FixedDepositController (11 tests exist)
**Additional Scenarios Needed**:
1. ✅ CRUD and validation covered
2. ❌ FD premature withdrawal calculation
3. ❌ FD renewal flow
4. ❌ FD ladder strategy testing
5. ❌ Compound interest verification

**Additional Tests**: 4-5 tests

---

### RecurringDepositController (8 tests exist)
**Additional Scenarios Needed**:
1. ✅ Basic CRUD covered
2. ❌ Missed installment handling
3. ❌ RD closure before maturity
4. ❌ Installment payment tracking
5. ❌ Penalty calculation

**Additional Tests**: 4-5 tests

---

### LoanController (2 tests exist)
**Additional Scenarios Needed**:
1. ✅ Basic create/get covered
2. ❌ Loan prepayment simulation (endpoint exists but not tested)
3. ❌ EMI calculation verification
4. ❌ Loan amortization schedule
5. ❌ Multiple loan types (home, personal, car, education)
6. ❌ Loan status updates (active, closed, defaulted)

**Additional Tests**: 6-8 tests

---

### BudgetController (2 tests exist)
**Additional Scenarios Needed**:
1. ✅ Basic expense/get covered
2. ❌ Budget limits and alerts
3. ❌ Monthly budget tracking
4. ❌ Category-wise expense breakdown
5. ❌ Budget vs actual comparison
6. ❌ Recurring expense handling
7. ❌ Budget recommendations

**Additional Tests**: 7-10 tests

---

### ETFController (2 tests exist)
**Additional Scenarios Needed**:
1. ✅ Basic add/get covered
2. ❌ ETF by symbol lookup
3. ❌ ETF portfolio valuation
4. ❌ ETF returns calculation
5. ❌ ETF vs mutual fund comparison

**Additional Tests**: 4-5 tests

---

### NetWorthController (1 test exists)
**Additional Scenarios Needed**:
1. ✅ Basic summary covered
2. ❌ Detailed asset breakdown by category
3. ❌ Net worth trend over time
4. ❌ Asset allocation visualization
5. ❌ Liability tracking
6. ❌ Net worth growth rate
7. ❌ Comparative net worth (peer benchmarking)

**Additional Tests**: 6-8 tests

---

## 4. Cross-Cutting Test Scenarios

### Security Tests (Missing)
1. ❌ SQL injection prevention
2. ❌ XSS attack prevention
3. ❌ CSRF token validation
4. ❌ Rate limiting enforcement
5. ❌ API key validation
6. ❌ Role-based access control (RBAC)
7. ❌ Data encryption in transit

**Additional Tests**: 7-10 tests

---

### Performance Tests (Missing)
1. ❌ Load testing (100+ concurrent users)
2. ❌ Response time SLA validation (<200ms)
3. ❌ Database connection pool exhaustion
4. ❌ Cache hit rate verification
5. ❌ API throttling behavior

**Additional Tests**: 5-7 tests

---

### Error Handling Tests (Partial)
1. ✅ 404 errors covered
2. ✅ 403 forbidden covered
3. ❌ 500 internal server errors
4. ❌ Database connection failures
5. ❌ External API timeouts
6. ❌ Invalid JSON payloads
7. ❌ Missing required fields
8. ❌ Malformed request bodies

**Additional Tests**: 6-8 tests

---

## 5. Summary

### Current Coverage
| Category | Tests Written | Tests Passing | Tests Skipped | Coverage % |
|----------|--------------|---------------|---------------|------------|
| **Auth** | 10 | 10 | 0 | 80% |
| **Savings** | 28 | 28 | 0 | 75% |
| **Portfolio** | 7 | 2 | 5 | 30% (blocked) |
| **Investments** | 4 | 2 | 2 | 50% (blocked) |
| **Loan** | 2 | 2 | 0 | 40% |
| **Budget** | 2 | 2 | 0 | 30% |
| **NetWorth** | 2 | 2 | 0 | 50% |
| **Health** | 2 | 2 | 0 | 100% |
| **Stock** | 0 | 0 | 0 | 0% |
| **Insurance** | 0 | 0 | 0 | 0% |
| **Tax** | 0 | 0 | 0 | 0% |
| **Lending** | 0 | 0 | 0 | 0% |
| **AA** | 0 | 0 | 0 | 0% |
| **User** | 0 | 0 | 0 | 0% |
| **DevTools** | 0 | 0 | 0 | 0% |
| **TOTAL** | **60** | **53** | **7** | **~45%** |

---

### Recommended Priorities

#### **Phase 1: Unblock Skipped Tests** (HIGH PRIORITY)
1. Set up stock data seeding → Enable 5 Portfolio tests
2. Set up AA consent mocking → Enable 2 Mutual Fund tests

**Impact**: +7 tests (total: 60 active tests)

---

#### **Phase 2: Core Feature Controllers** (HIGH PRIORITY)
1. Insurance Controller → +10 tests
2. Tax Controller → +8 tests
3. Lending Controller → +10 tests
4. Stock Controller → +3 tests

**Impact**: +31 tests (total: 91 tests)

---

#### **Phase 3: Enhanced Coverage** (MEDIUM PRIORITY)
1. Add missing scenarios to existing controllers → +50 tests
2. User Controller → +10 tests
3. AA Controller → +10 tests
4. NetWorth enhancements → +7 tests

**Impact**: +77 tests (total: 168 tests)

---

#### **Phase 4: Cross-Cutting Concerns** (MEDIUM PRIORITY)
1. Security tests → +10 tests
2. Error handling tests → +8 tests
3. Performance tests → +7 tests

**Impact**: +25 tests (total: 193 tests)

---

### Target: 200+ Integration Tests
**Current**: 60 tests (30% coverage)  
**Blocked**: 7 tests (requires data seeding)  
**Remaining**: 140+ tests to reach comprehensive coverage

---

## 6. Next Steps

### Immediate Actions (This Week)
1. ✅ Fix all failing tests → **COMPLETED**
2. 🔄 Set up stock data seeding script
3. 🔄 Set up AA consent mocking
4. 🔄 Re-enable 7 skipped tests

### Short Term (2 Weeks)
1. 📝 Implement Insurance Controller tests (10 tests)
2. 📝 Implement Tax Controller tests (8 tests)
3. 📝 Implement Lending Controller tests (10 tests)
4. 📝 Implement Stock Controller tests (3 tests)

### Medium Term (1 Month)
1. 📝 Add enhanced scenarios to existing controllers (50 tests)
2. 📝 Implement User Controller tests (10 tests)
3. 📝 Implement AA Controller tests (10 tests)
4. 📝 Add security and error handling tests (18 tests)

---

## 7. Test Data Requirements

### Required Test Data Seeding
1. **Stocks Table**:
   - 20+ Indian stock symbols (NSE/BSE)
   - Current prices
   - Historical prices (optional)
   - Sector mappings

2. **Sectors Table**:
   - IT, Banking, Pharma, Auto, FMCG, Energy, etc.
   - Sector descriptions

3. **AA Consents**:
   - Mock ACTIVE consent for test user
   - Mock FI data responses
   - Consent templates

4. **Test Users**:
   - Pre-created users with different roles
   - Users with existing portfolios
   - Users with different asset combinations

---

## Notes
- All tests use H2 in-memory database
- Tests are isolated (each test gets fresh user)
- REST Assured 5.4.0 for API testing
- Spring Boot Test framework
- Current test execution time: ~25 seconds for 60 tests

**Status**: Document created on 31 January 2026  
**Next Review**: After Phase 1 completion
