# 🤖 AI Assistant Project Guide - PI System

> **Purpose**: Help AI assistants quickly understand and work with the PI System  
> **Audience**: AI coding assistants, LLMs, automated tools  
> **Last Updated**: February 1, 2026

---

## 🎯 Quick Project Context

**What is PI System?**
A Personal Investment & Financial Intelligence Platform - a comprehensive financial management system for individual investors to track portfolios, manage budgets, analyze taxes, and monitor wealth.

**Core Principle**: Read-only analysis and transparency. NO financial advice, NO trading execution, NO rebalancing actions.

**Current Status**:
- Overall: 69.6% complete (71/102 features)
- Backend: 75% complete (Spring Boot, REST APIs)
- Frontend: 40% complete (React, needs major development)
- Testing: 21% coverage (needs improvement)

---

## 🏗️ Technical Architecture

### Tech Stack
```yaml
Backend:
  Language: Java 17
  Framework: Spring Boot 3.4.0
  Security: Spring Security + JWT
  Database: MySQL 8.0.40
  Migration: Flyway 9.x
  Caching: Redis
  API Docs: OpenAPI 3 / Swagger UI (port 8082)

Frontend:
  Framework: React 18
  Build: Vite
  Styling: Vanilla CSS
  Icons: Lucide React
  Charts: Recharts
  State: React Context API

Build & Deploy:
  Build Tool: Gradle 9.2.1
  Container: Docker + Docker Compose
  Java Runtime: OpenJDK 17

Testing:
  Framework: JUnit 5
  Integration: REST Assured
  Containers: Testcontainers
```

### Architecture Pattern
**Layered Architecture**:
```
Controller → Service → Repository → Database
    ↓          ↓
  DTO      Business Logic
```

### Port Configuration
- Backend API: `8082`
- Frontend Dev: `5173` (Vite default)
- MySQL: `3306`
- Redis: `6379`

---

## 📁 Project Structure

```
pi-system/
├── src/
│   ├── main/
│   │   ├── java/com/
│   │   │   ├── aa/               # Account Aggregation (Mock AA)
│   │   │   ├── admin/            # Admin Portal (User Management)
│   │   │   ├── auth/             # Authentication & JWT
│   │   │   ├── budget/           # Budget & Expense Tracking
│   │   │   ├── common/           # Shared utilities, helpers
│   │   │   ├── config/           # Spring configurations
│   │   │   ├── exception/        # Global exception handling
│   │   │   ├── feature/          # Feature Flags system
│   │   │   ├── insurance/        # Insurance Policy Tracking
│   │   │   ├── lending/          # Lending Money Tracker
│   │   │   ├── loan/             # Loan Management (Personal/Home/Vehicle)
│   │   │   ├── mutualfund/       # Mutual Fund Integration
│   │   │   ├── portfolio/        # Investment Portfolio & Stocks
│   │   │   ├── savings/          # Savings Accounts, FD, RD
│   │   │   ├── stock/            # Stock Management & Pricing
│   │   │   ├── subscription/     # Subscription Tracking
│   │   │   ├── tax/              # Tax Management & Planning
│   │   │   └── wealth/           # Wealth aggregation
│   │   └── resources/
│   │       ├── db/migration/     # Flyway SQL migrations (V1, V2, etc.)
│   │       ├── application.yml   # Main config
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── application-test.yml
│   └── test/
│       └── java/com/             # Integration tests (21% coverage)
│
├── frontend/
│   ├── src/
│   │   ├── components/           # React components
│   │   ├── contexts/             # Context providers (Auth, Feature)
│   │   ├── pages/               # Page components
│   │   └── services/            # API service calls
│   └── public/                  # Static assets
│
├── docs/                        # Documentation (60+ files)
├── devtools/                    # Migration CLI, dev dashboard
├── planning/                    # Vision, scope, constraints
└── gradle/                      # Gradle wrapper
```

---

## 🔑 Key Concepts & Patterns

### 1. Authentication Flow
- JWT-based authentication
- Tokens stored in localStorage
- Role-based access: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- AuthenticationHelper utility for getting current user

**Pattern**:
```java
// Controller
@GetMapping("/my-endpoint")
public ResponseEntity<?> getMyData() {
    User user = authenticationHelper.getCurrentUser();
    // ... business logic
}
```

### 2. API Structure
**Standard REST Pattern**:
- `POST /api/v1/resource` - Create
- `GET /api/v1/resource` - List all
- `GET /api/v1/resource/{id}` - Get one
- `PUT /api/v1/resource/{id}` - Update
- `DELETE /api/v1/resource/{id}` - Delete

**Special Endpoints**:
- `/api/v1/auth/*` - Authentication endpoints
- `/api/v1/admin/*` - Admin-only endpoints
- `/api/v1/mutual-funds/external/*` - Third-party API proxy

### 3. Database Migrations
**Flyway Convention**:
- Format: `V{number}__{Description}.sql`
- Example: `V34__Add_Missing_Tax_Columns.sql`
- Sequential numbering (V1, V2, V3...)
- Never modify existing migrations after deployment

**Current Migration Count**: 34 migrations

### 4. Feature Flags
**Usage**:
```java
@RequiresFeature("BUDGET_MODULE")
public ResponseEntity<?> budgetEndpoint() { ... }
```

**Available Flags**:
- BUDGET_MODULE
- PORTFOLIO
- STOCKS
- RECURRING_TRANSACTIONS
- SUBSCRIPTIONS
- ALERTS
- TAX_PLANNING
- LENDING_TRACKER

### 5. Error Handling
**Global Exception Handler** in `exception/GlobalExceptionHandler.java`:
- Returns consistent JSON error responses
- HTTP status codes: 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Server Error)

---

## 📊 Module Status Reference

| Module | Backend | Frontend | Tests | Priority |
|--------|---------|----------|-------|----------|
| Authentication | 100% | 100% | 70% | Complete |
| Admin Portal | 100% | 100% | 0% | Complete |
| Portfolio | 80% | 100% | 0% | High |
| Budgeting | 100% | 90% | 0% | High |
| Wealth (Loans/FD/RD) | 100% | 0% | 0% | High |
| Tax Management | 60% | 0% | 0% | Medium |
| Insurance | 100% | 0% | 0% | Medium |
| Lending | 100% | 0% | 0% | Medium |
| Stocks | 50% | 0% | 0% | Low |
| Mutual Funds | 80% | 0% | 0% | Low |
| Feature Flags | 100% | 100% | 0% | Complete |

**Key Insight**: Frontend is the biggest gap (0% in most modules)

---

## 🚨 Critical Information

### Recent Fixes (Feb 1, 2026)
1. **Loan Calculations Fixed**:
   - EMI calculation now handles 0% interest
   - Prepayment simulation improved
   - Payment recording logic corrected
   - File: `src/main/java/com/loan/service/LoanServiceImpl.java`

2. **Tax Database Schema Fixed**:
   - Added 13 missing columns to tax_details table
   - Created V34 migration
   - Fixed Flyway V32 checksum mismatch

### Known Issues
- Tax module needs 3 additional tables (capital_gains_transactions, tax_saving_investments, tds_entries)
- Frontend development at 0% for: Loans, Tax, Insurance, Lending, Stocks
- Test coverage only 21% (target: 80%)

### Technical Debt
- Need to refactor older controllers to use AuthenticationHelper
- Standardize error response format
- Add comprehensive input validation
- Increase test coverage

---

## 🎯 Common Tasks & How To Do Them

### Task 1: Add a New REST Endpoint
**Steps**:
1. Create/update Entity in appropriate package
2. Create Repository (extends JpaRepository)
3. Create Service interface and implementation
4. Create Controller with @RestController
5. Add DTO classes if needed
6. Update tests

**Example Structure**:
```java
// Entity
@Entity
@Table(name = "my_table")
public class MyEntity { ... }

// Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> { }

// Service
@Service
public class MyServiceImpl implements MyService {
    @Autowired
    private MyRepository repository;
    @Autowired
    private AuthenticationHelper authHelper;
}

// Controller
@RestController
@RequestMapping("/api/v1/my-resource")
public class MyController {
    @Autowired
    private MyService service;
}
```

### Task 2: Add a Database Migration
**Steps**:
1. Navigate to `src/main/resources/db/migration/`
2. Find the last migration number (currently V34)
3. Create new file: `V35__Your_Description.sql`
4. Write SQL (CREATE TABLE, ALTER TABLE, INSERT, etc.)
5. Test locally before committing
6. Never modify after deployment

**Example**:
```sql
-- V35__Create_New_Feature_Table.sql
CREATE TABLE IF NOT EXISTS new_feature (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Task 3: Add Frontend Component
**Steps**:
1. Create component in `frontend/src/components/` or `frontend/src/pages/`
2. Use React functional components with hooks
3. Use `AuthContext` for user data
4. Use `FeatureContext` for feature flags
5. Style with CSS modules or inline styles

**Example**:
```javascript
import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';

function MyComponent() {
  const { user } = useAuth();
  const [data, setData] = useState([]);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    const response = await fetch('/api/v1/my-endpoint', {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
    setData(await response.json());
  };

  return <div>{/* Component JSX */}</div>;
}
```

### Task 4: Add Integration Test
**Steps**:
1. Navigate to `src/test/java/com/`
2. Create test class matching controller name
3. Use `@SpringBootTest` and `@AutoConfigureMockMvc`
4. Use REST Assured for API testing
5. Test all CRUD operations

**Example**:
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    private String authToken;
    
    @BeforeEach
    void setUp() {
        // Get auth token
        authToken = getAuthToken();
    }
    
    @Test
    void testCreate() throws Exception {
        mockMvc.perform(post("/api/v1/my-resource")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\"}"))
            .andExpect(status().isCreated());
    }
}
```

---

## 🔍 Finding Things in the Codebase

### How to Find...

**A specific entity/table**:
- Search for `@Entity` annotation
- Check package names (budget, loan, tax, etc.)
- Look in corresponding migration files

**An API endpoint**:
- Search for `@RequestMapping` or `@GetMapping`
- Check controller classes (suffix: `Controller.java`)
- Reference Swagger UI at http://localhost:8082/swagger-ui.html

**Business logic**:
- Look in service implementations (suffix: `ServiceImpl.java`)
- Check service interfaces first for method signatures

**Database schema**:
- Check `src/main/resources/db/migration/V*.sql` files
- Most recent schema is in highest version number

**Configuration**:
- Main config: `src/main/resources/application.yml`
- Environment-specific: `application-{env}.yml`
- Java config: `src/main/java/com/config/`

**Frontend pages**:
- Main pages: `frontend/src/pages/`
- Components: `frontend/src/components/`
- Routes: Check `frontend/src/App.jsx`

---

## 📚 Important Files to Know

### Backend
1. **Application Entry Point**: `src/main/java/com/PiSystemApplication.java`
2. **Security Config**: `src/main/java/com/config/SecurityConfig.java`
3. **JWT Utilities**: `src/main/java/com/auth/util/JwtUtil.java`
4. **Global Exception Handler**: `src/main/java/com/exception/GlobalExceptionHandler.java`
5. **Authentication Helper**: `src/main/java/com/common/AuthenticationHelper.java`

### Frontend
1. **App Entry**: `frontend/src/App.jsx`
2. **Auth Context**: `frontend/src/contexts/AuthContext.jsx`
3. **Feature Context**: `frontend/src/contexts/FeatureContext.jsx`
4. **Main CSS**: `frontend/src/App.css`

### Configuration
1. **Gradle Build**: `build.gradle`
2. **Docker Compose**: `docker-compose.yml`
3. **Dockerfile**: `Dockerfile`
4. **Application Config**: `src/main/resources/application.yml`

### Documentation
1. **Feature List**: `docs/FEATURES.md`
2. **Progress Tracker**: `docs/PROGRESS.md`
3. **Pending Features**: `MODULE_PENDING_FEATURES.md`
4. **Development Standards**: `docs/DEVELOPMENT_STANDARDS.md`
5. **Testing Guide**: `docs/TESTING_PROCESS.md`

---

## 🎨 Code Conventions

### Naming Conventions
- **Entities**: Singular noun (User, Loan, Budget)
- **Tables**: Plural or singular based on context (users, loan_details)
- **Controllers**: `{Resource}Controller` (UserController, LoanController)
- **Services**: `{Resource}Service` interface, `{Resource}ServiceImpl` implementation
- **Repositories**: `{Entity}Repository` (UserRepository)
- **DTOs**: `{Purpose}DTO` or `{Resource}Request/Response`

### Package Structure
- Each module has its own package (budget, loan, tax)
- Within each module: controller, service, repository, model (entity), dto
- Common utilities in `common/` package
- Configurations in `config/` package

### Code Style
- Use `@Autowired` for dependency injection
- Prefer constructor injection for required dependencies
- Use Optional for nullable returns
- Use BigDecimal for financial calculations (NEVER double or float)
- Always set MathContext for BigDecimal operations
- Use ResponseEntity for controller responses
- Return proper HTTP status codes

### Database Conventions
- Primary keys: `id BIGINT AUTO_INCREMENT`
- Foreign keys: `{table}_id` (user_id, loan_id)
- Timestamps: `created_date`, `updated_date` (TIMESTAMP)
- Amounts: `DECIMAL(15,2)` or `DECIMAL(10,2)`
- Percentages: Store as decimal (5.5% = 5.5, not 0.055)

---

## 🚀 Getting Started Quickly

### For Code Generation
1. **Understand the module** - Check module status in section above
2. **Check existing patterns** - Look at similar modules (e.g., Budget for new expense tracking)
3. **Follow the layers** - Entity → Repository → Service → Controller
4. **Add tests** - At least basic CRUD tests
5. **Update docs** - Add to PROGRESS.md

### For Bug Fixes
1. **Locate the issue** - Controller → Service → Repository
2. **Check related tests** - See if tests exist and if they pass
3. **Fix the code** - Follow existing patterns
4. **Add test if missing** - Prevent regression
5. **Document the fix** - Add note in relevant .md file

### For Frontend Development
1. **Check if backend exists** - Verify API endpoint in Swagger
2. **Create component** - Follow React best practices
3. **Use contexts** - AuthContext for user, FeatureContext for flags
4. **Style consistently** - Match existing UI patterns
5. **Test the flow** - Complete user journey

---

## ⚠️ Common Pitfalls & Solutions

### Pitfall 1: Financial Calculation Precision
**Problem**: Using double/float for money calculations
**Solution**: Always use BigDecimal with MathContext
```java
// Wrong
double emi = principal * rate / 100;

// Right
MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
BigDecimal emi = principal.multiply(rate).divide(ONE_HUNDRED, MC);
```

### Pitfall 2: Authentication Issues
**Problem**: Getting 401 Unauthorized
**Solution**: Check JWT token validity, use AuthenticationHelper
```java
// Get current user safely
User user = authenticationHelper.getCurrentUser();
```

### Pitfall 3: Migration Conflicts
**Problem**: Flyway checksum mismatch
**Solution**: Never modify migrations after deployment. Create new migration instead.

### Pitfall 4: Missing Feature Flags
**Problem**: Feature not working in production
**Solution**: Check feature_toggles table, ensure flag is enabled

### Pitfall 5: CORS Issues
**Problem**: Frontend can't call backend
**Solution**: Check SecurityConfig CORS configuration, ensure proper headers

---

## 🎓 Learning Resources

### To Understand the Project
1. Read: `planning/vision.md` - Understand the "why"
2. Read: `PRODUCT.md` - Know what IS and IS NOT implemented
3. Read: `docs/PROGRESS.md` - Current status
4. Read: `MODULE_PENDING_FEATURES.md` - What's next

### To Start Development
1. Read: `README.md` - Setup instructions
2. Read: `docs/DEVELOPMENT_STANDARDS.md` - Coding standards
3. Read: `docs/TESTING_PROCESS.md` - Testing approach
4. Check: Module-specific docs in `docs/` folder

### To Deploy
1. Read: `docs/DEPLOYMENT_GUIDE.md`
2. Read: `FREE_HOSTING_DEPLOYMENT.md` (for free hosting options)
3. Check: `docker-compose.yml` for container setup

---

## 📊 Database Schema Quick Reference

### Core Tables
- `users` - User accounts (id, username, email, password, role)
- `user_activity_logs` - Activity tracking
- `feature_toggles` - Feature flags

### Budget Module
- `categories` - Expense categories
- `expenses` - Expense entries
- `incomes` - Income entries
- `budgets` - Budget limits
- `recurring_transactions` - SIPs, recurring expenses
- `subscriptions` - Subscription tracking
- `alerts` - Budget alerts

### Wealth Module
- `savings_accounts` - Savings account tracking
- `fixed_deposits` - FD tracking
- `recurring_deposits` - RD tracking
- `loans` - Personal/home/vehicle loans
- `loan_payments` - Loan payment history
- `lending_records` - Money lent to others
- `lending_payments` - Repayment tracking
- `insurance_policies` - Insurance tracking

### Investment Module
- `stocks` - Stock master data
- `portfolio` - Stock holdings
- `stock_prices` - Historical prices
- `mutual_funds` - MF tracking (partial)

### Tax Module
- `tax_details` - Tax information per year

### Account Aggregation
- `consents` - AA consent management
- `templates` - Data fetch templates

---

## 🔧 Development Commands

### Backend
```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Run with profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Test
./gradlew test

# Clean build
./gradlew clean build
```

### Frontend
```bash
# Install dependencies
cd frontend && npm install

# Run dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Docker
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild and start
docker-compose up -d --build
```

### Database
```bash
# Connect to MySQL
mysql -u root -p

# Run migrations manually (not recommended)
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo
```

---

## 🎯 Priority Areas for AI Assistance

### High Priority (Do These First)
1. **Frontend Development** - Biggest gap, needs components for:
   - Loans module (dashboard, forms, payment tracking)
   - Tax module (dashboard, forms, calculators)
   - Insurance module (policy tracking, premium alerts)
   - Lending module (dashboard, repayment tracking)

2. **Integration Tests** - Coverage is only 21%, need tests for:
   - All 15 untested controllers
   - Edge cases in loan calculations
   - Tax calculation scenarios

3. **Tax Module Database** - Need to create:
   - capital_gains_transactions table
   - tax_saving_investments table
   - tds_entries table

### Medium Priority
4. **API Documentation** - Enhance Swagger annotations
5. **Error Handling** - Standardize error responses
6. **Input Validation** - Add comprehensive validation
7. **Performance** - Add caching where appropriate

### Low Priority
8. **Refactoring** - Code cleanup and optimization
9. **Documentation** - Keep docs updated
10. **Advanced Features** - ML, AI-powered insights (future)

---

## 💡 Tips for AI Assistants

1. **Always check module status first** - Don't implement what's already done
2. **Follow existing patterns** - Look at similar code before creating new
3. **Use BigDecimal for money** - Never float/double
4. **Test your changes** - At least basic integration test
5. **Update documentation** - Add to PROGRESS.md or relevant doc
6. **Check for feature flags** - Some features are toggleable
7. **Respect the architecture** - Layered pattern is enforced
8. **Mind the security** - Always use AuthenticationHelper
9. **Database changes need migrations** - Create Flyway migration
10. **Frontend needs backend first** - Ensure API exists before building UI

---

## 📞 Quick Reference Links

### Key Documentation
- [README.md](../README.md) - Project setup
- [PROGRESS.md](PROGRESS.md) - Current status
- [MODULE_PENDING_FEATURES.md](../MODULE_PENDING_FEATURES.md) - What's next
- [FEATURES.md](FEATURES.md) - Feature list
- [DEVELOPMENT_STANDARDS.md](DEVELOPMENT_STANDARDS.md) - Code standards

### API Access
- Swagger UI: http://localhost:8082/swagger-ui.html
- API Base: http://localhost:8082/api/v1
- Frontend: http://localhost:5173

### External APIs
- Mutual Funds: https://api.mfapi.in
- Stocks: Mock data (no real API yet)

---

**Version**: 1.0.0  
**Last Updated**: February 1, 2026  
**Maintained By**: AI Assistant Documentation Team

---

## 🎉 Quick Start Checklist for AI

- [ ] Read this entire document
- [ ] Check current module status
- [ ] Review existing code patterns
- [ ] Understand authentication flow
- [ ] Know database schema basics
- [ ] Familiarize with API structure
- [ ] Check pending features list
- [ ] Review recent fixes and known issues
- [ ] Understand priority areas
- [ ] Ready to assist! 🚀
