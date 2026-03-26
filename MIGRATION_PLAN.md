# Pi-System Monorepo Restructuring - Migration Plan

**Date:** March 27, 2026  
**Status:** Draft - Ready for Implementation

## Executive Summary

This document outlines the complete migration plan to reorganize the pi-system repository from its current flat structure into a clean, scalable, feature-first monorepo layout while preserving all runtime behavior.

## Current State Analysis

### Backend Structure (src/main/java/com/)
```
com/
├── aa/              (Account Aggregator)
├── admin/
├── ai/
├── alerts/
├── api/
├── audit/
├── auth/
├── budget/
├── common/
├── etf/
├── externalServices/
├── healthstatus/
├── investments/
├── lending/
├── loan/
├── main/            (Main application class)
├── mutualfund/
├── payments/
├── portfolio/
├── protection/
├── savings/
├── sms/
├── stocks/
├── tax/
├── upi/
├── users/
└── websocket/
```

**Issues:**
- Flat structure under `com/` package - not scalable
- No clear namespace or domain grouping
- Inconsistent internal organization within modules
- Core/shared services mixed with business modules

### Frontend Structure (frontend/src/)
```
src/
├── App.jsx
├── main.jsx
├── api/              (10 scattered API files)
├── assets/
├── components/       (36+ mixed components)
├── contexts/
├── layouts/
├── pages/            (25+ pages - flat structure)
├── services/         (3 API files)
├── utils/
└── websocket/
```

**Issues:**
- Generic pages/ and components/ directories
- Difficult to understand which components belong to which features
- API clients scattered across api/ and services/
- No co-location of related code
- Hard to maintain and scale

### Root Directory
```
Root/
├── 40+ markdown documentation files (unorganized)
├── build.gradle, settings.gradle
├── docker-compose.yml, Dockerfile
├── monitoring-start.sh, run-api-tests.sh, run-integration-tests.sh
├── QuickRegexTest.class, TestPatterns.class (orphaned)
├── bug-bounty-hunter/ (standalone tool)
├── _bmad/, _bmad-output/ (tooling artifacts)
├── devtools/
├── docs/ (partially organized)
├── monitoring/
├── planning/
└── ls / (unknown artifact)
```

**Issues:**
- Massive root-level clutter (40+ markdown files)
- Shell scripts not organized
- Compiled .class files at root
- Unclear separation between code, docs, infra, and tooling

---

## Target Structure

### 1. Clean Root Directory
```
pi-system/
├── README.md                    (Essential root docs)
├── .env.example
├── .gitignore
├── .dockerignore
│
├── build.gradle                 (Build config)
├── settings.gradle
├── gradle.properties
├── gradlew, gradlew.bat
├── gradle/
│
├── Dockerfile                   (Container config)
├── docker-compose.yml
│
├── src/                         (Backend source)
├── frontend/                    (Frontend source)
│
├── docs/                        (All documentation)
├── scripts/                     (Shell scripts)
├── infra/                       (Infrastructure/ops)
├── tools/                       (Standalone utilities)
├── devtools/                    (Development tools)
│
├── build/                       (Generated - no changes)
├── bin/                         (Generated - no changes)
├── logs/
├── .gradle/                     (Generated)
└── .github/
    └── workflows/
```

### 2. Backend Structure (src/main/java/com/pisystem/)
```
src/main/java/com/pisystem/
├── PiSystemApplication.java     (Main app class)
│
├── config/                      (Global configs)
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── CorsConfig.java
│   └── ...
│
├── shared/                      (Shared/common utilities)
│   ├── exception/
│   ├── util/
│   ├── dto/
│   └── audit/
│
├── core/                        (Core domain services)
│   ├── auth/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── security/
│   │   └── exception/
│   │
│   ├── users/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   └── dto/
│   │
│   └── admin/
│       ├── controller/
│       ├── service/
│       └── dto/
│
├── modules/                     (Business feature modules)
│   ├── budget/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── exception/
│   │   └── scheduler/
│   │
│   ├── tax/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   └── dto/
│   │
│   ├── portfolio/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── engine/
│   │   └── cas/
│   │
│   ├── stocks/
│   ├── mutualfunds/
│   ├── etf/
│   ├── lending/
│   ├── loans/
│   ├── insurance/
│   ├── savings/
│   ├── sms/
│   └── upi/
│
├── integrations/                (External integrations)
│   ├── accountaggregator/      (AA module)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── adapter/
│   │   └── mock/
│   │
│   └── externalservices/
│
├── infrastructure/              (Infrastructure concerns)
│   ├── healthcheck/
│   ├── monitoring/
│   ├── websocket/
│   └── email/
│
└── devtools/                    (Development utilities)
    ├── controller/
    └── testrunner/
```

### 3. Backend Tests (src/test/java/com/pisystem/)
```
src/test/java/com/pisystem/
├── unit/                        (Unit tests)
│   ├── core/
│   │   ├── auth/
│   │   └── users/
│   └── modules/
│       ├── budget/
│       ├── tax/
│       ├── lending/
│       └── ...
│
├── integration/                 (Integration tests)
│   ├── api/
│   ├── repository/
│   └── service/
│
└── contract/                    (Contract tests - future)
    └── api/
```

### 4. Frontend Structure (frontend/src/)
```
frontend/src/
├── main.jsx                     (Entry point)
├── App.jsx                      (Root component)
├── App.css
├── index.css
│
├── app/                         (App-level)
│   ├── router/
│   ├── providers/
│   └── config/
│
├── features/                    (Feature modules - co-located)
│   ├── auth/
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   └── ForgotPassword.jsx
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/
│   │   │   └── authApi.js
│   │   └── index.js
│   │
│   ├── dashboard/
│   │   ├── pages/
│   │   │   ├── Dashboard.jsx
│   │   │   └── NetWorth.jsx
│   │   ├── components/
│   │   └── services/
│   │
│   ├── budget/
│   │   ├── pages/
│   │   │   ├── Budget.jsx
│   │   │   ├── CashFlow.jsx
│   │   │   └── RecurringTransactions.jsx
│   │   ├── components/
│   │   │   ├── TransactionModal.jsx
│   │   │   ├── CreateTemplateModal.jsx
│   │   │   ├── TagManagementModal.jsx
│   │   │   └── TagSelector.jsx
│   │   ├── services/
│   │   │   ├── cashFlowApi.js
│   │   │   └── recurringTransactionsApi.js
│   │   └── styles/
│   │
│   ├── tax/
│   │   ├── pages/
│   │   │   └── Tax.jsx
│   │   ├── components/
│   │   │   ├── TaxDashboard.jsx
│   │   │   ├── CapitalGainsModule.jsx
│   │   │   ├── DeductionsTracker.jsx
│   │   │   ├── TDSManagement.jsx
│   │   │   ├── ITRFilingAssistant.jsx
│   │   │   ├── TaxPlanningTools.jsx
│   │   │   └── IncomeEntryForms.jsx
│   │   └── services/
│   │       └── taxApi.js
│   │
│   ├── portfolio/
│   │   ├── pages/
│   │   │   ├── Portfolio.jsx
│   │   │   └── PortfolioRebalancing.jsx
│   │   └── services/
│   │       └── rebalancingApi.js
│   │
│   ├── goals/
│   │   ├── pages/
│   │   │   ├── FinancialGoals.jsx
│   │   │   ├── GoalDetails.jsx
│   │   │   └── RetirementPlanning.jsx
│   │   ├── components/
│   │   │   ├── GoalCard.jsx
│   │   │   └── CreateGoalModal.jsx
│   │   └── services/
│   │       ├── goalsApi.js
│   │       └── retirementPlanningApi.js
│   │
│   ├── banking/
│   │   ├── pages/
│   │   │   └── Banking.jsx
│   │   ├── components/
│   │   └── services/
│   │
│   ├── loans/
│   │   ├── pages/
│   │   │   └── Loans.jsx
│   │   └── services/
│   │
│   ├── insurance/
│   │   ├── pages/
│   │   │   └── Insurance.jsx
│   │   ├── components/
│   │   │   └── insurance/
│   │   └── services/
│   │       └── insuranceApi.js
│   │
│   ├── lending/
│   │   ├── pages/
│   │   │   └── Lending.jsx
│   │   ├── components/
│   │   │   ├── Lending.jsx
│   │   │   ├── LendingForm.jsx
│   │   │   ├── AddLendingModal.jsx
│   │   │   ├── LendingDetailModal.jsx
│   │   │   ├── AddRepaymentModal.jsx
│   │   │   └── RepaymentTracker.jsx
│   │   └── services/
│   │       └── lendingApi.js
│   │
│   ├── documents/
│   │   ├── pages/
│   │   │   └── Documents.jsx
│   │   ├── components/
│   │   │   └── DocumentCard.jsx
│   │   └── services/
│   │       └── documentsApi.js
│   │
│   ├── credit/
│   │   ├── pages/
│   │   │   └── CreditScore.jsx
│   │   └── services/
│   │       └── creditScoreApi.js
│   │
│   ├── insights/
│   │   ├── pages/
│   │   │   └── Insights.jsx
│   │   └── components/
│   │
│   ├── settings/
│   │   ├── pages/
│   │   │   └── Settings.jsx
│   │   └── components/
│   │
│   ├── admin/
│   │   ├── pages/
│   │   │   └── admin/
│   │   └── components/
│   │
│   └── ai-assistant/
│       ├── components/
│       │   └── AiAssistant.jsx
│       └── services/
│
├── shared/                      (Shared/reusable code)
│   ├── components/              (Shared UI components)
│   │   ├── BulkActionsToolbar.jsx
│   │   ├── ExportModal.jsx
│   │   ├── FeatureGate.jsx
│   │   ├── TierBadge.jsx
│   │   ├── TierLimitIndicator.jsx
│   │   ├── UpgradePrompt.jsx
│   │   └── ...
│   │
│   ├── layouts/                 (Layout components)
│   │   └── ...existing layouts...
│   │
│   ├── utils/                   (Utility functions)
│   │   └── ...existing utils...
│   │
│   ├── services/                (Shared services)
│   │   ├── api.js              (Base API config)
│   │   └── stockPriceWebSocket.js
│   │
│   ├── contexts/                (Global contexts)
│   │   └── ...existing contexts...
│   │
│   ├── styles/                  (Global styles)
│   │   └── index.css
│   │
│   ├── hooks/                   (Custom hooks)
│   │
│   └── types/                   (TypeScript types - future)
│
├── assets/                      (Static assets)
│   └── ...
│
└── websocket/                   (WebSocket utilities)
    └── ...
```

### 5. Frontend Tests (frontend/tests/)
```
frontend/
├── src/
└── tests/                       (New structure)
    ├── unit/
    │   ├── features/
    │   │   ├── budget/
    │   │   ├── tax/
    │   │   └── ...
    │   └── shared/
    │
    ├── integration/
    │   ├── api/
    │   └── features/
    │
    └── e2e/
        ├── auth.spec.js
        ├── budget.spec.js
        └── ...
```

### 6. Documentation Structure (docs/)
```
docs/
├── README.md                    (Documentation index)
│
├── architecture/                (Architecture docs)
│   ├── OVERVIEW.md
│   ├── FEATURE_FLAG_ARCHITECTURE.md
│   ├── REAL_TIME_FEATURES_SUMMARY.md
│   └── DEVELOPMENT_STANDARDS.md
│
├── modules/                     (Module-specific docs)
│   ├── budget/
│   │   └── BUDGET_MODULE.md
│   ├── tax/
│   │   ├── TAX_MODULE_DEVELOPER_GUIDE.md
│   │   ├── TAX_API_COMPLETE_REFERENCE.md
│   │   └── TAX_API_QUICK_REFERENCE.md
│   ├── loans/
│   │   ├── LOANS_MODULE_DEVELOPER_GUIDE.md
│   │   ├── LOANS_API_QUICK_REFERENCE.md
│   │   └── LOANS_COMPLETE_IMPLEMENTATION_SUMMARY.md
│   ├── lending/
│   │   └── LENDING_MODULE_IMPLEMENTATION_COMPLETE.md
│   ├── portfolio/
│   │   └── PORTFOLIO_TRANSACTION_IMPLEMENTATION.md
│   ├── mutualfunds/
│   │   ├── MUTUAL_FUND_ETF_IMPLEMENTATION.md
│   │   ├── MUTUAL_FUND_API_QUICK_START.md
│   │   └── MUTUAL_FUND_INTEGRATION_SUMMARY.md
│   ├── insurance/
│   │   ├── INSURANCE_TRACKING_IMPLEMENTATION.md
│   │   └── INSURANCE_FRONTEND_IMPLEMENTATION.md
│   ├── sms/
│   │   ├── SMS_TRANSACTION_PARSER_IMPLEMENTATION.md
│   │   ├── SMS_PARSER_QUICK_START.md
│   │   └── SMS_MIGRATION_SWAGGER_COMPLETE.md
│   ├── upi/
│   │   ├── UPI_MODULE.md
│   │   ├── UPI_IMPLEMENTATION.md
│   │   └── UPI_COMPLETION_SUMMARY.md
│   └── ai/
│       └── AI_PROJECT_GUIDE.md
│
├── deployment/                  (Deployment guides)
│   ├── DOCKER_DEPLOYMENT_GUIDE.md
│   ├── DEPLOYMENT_GUIDE.md
│   ├── FREE_HOSTING_DEPLOYMENT.md
│   └── MOBILE_APP_DEVELOPMENT_GUIDE.md
│
├── testing/                     (Testing docs)
│   ├── TESTING_IMPLEMENTATION_COMPLETE.md
│   ├── VERTICAL_TESTING_PROTOCOL.md
│   ├── TESTING_PROCESS.md
│   └── TESTS_SUMMARY.md
│
├── api/                         (API references)
│   ├── HIGH_IMPACT_APIS.md
│   └── feature-specific-apis.md
│
├── features/                    (Feature documentation)
│   ├── FREE_TIER_INTEGRATION_GUIDE.md
│   ├── FREE_TIER_QUICK_START.md
│   ├── FEATURE_FLAG_QUICK_REFERENCE.md
│   ├── REAL_TIME_FEATURES_QUICK_REF.md
│   └── advanced/
│       ├── ADVANCED_FEATURES_IMPLEMENTATION.md
│       └── ADVANCED_FEATURES_FRONTEND_COMPLETE.md
│
├── operations/                  (Operations docs)
│   ├── MONITORING_GUIDE.md
│   ├── PROMETHEUS_GRAFANA_IMPLEMENTATION.md
│   ├── EMAIL_SETUP_GUIDE.md
│   └── SCHEDULER_JOBS.md
│
├── planning/                    (Planning docs)
│   ├── IMPLEMENTATION_ROADMAP.md
│   ├── IMPLEMENTATION_CHECKLIST.md
│   ├── MODULE_IMPROVEMENT_ROADMAP.md
│   ├── MODULE_PENDING_FEATURES.md
│   └── phase1/
│       ├── PHASE_1_FEATURE_AUDIT_REPORT.md
│       ├── PHASE_1_GAP_ANALYSIS.md
│       └── PHASE_1_LAUNCH_STRATEGY.md
│
├── archive/                     (Historical/session docs)
│   ├── sessions/
│   │   ├── SESSION_SUMMARY_FEB_1_2026.md
│   │   ├── SESSION_SUMMARY_FEB_5_2026_AFTERNOON.md
│   │   └── DOCUMENTATION_UPDATE_SUMMARY_FEB_1_2026.md
│   ├── analysis/
│   │   ├── MISSING_FEATURES_ANALYSIS.md
│   │   ├── COST_ANALYSIS_AND_MONETIZATION.md
│   │   └── PORTFOLIO_MODULE_GAPS_AND_RECOMMENDATIONS.md
│   └── ai/
│       └── AI_ASSISTANT_IMPROVEMENT_THINKINGS.md
│
└── DOCUMENTATION_INDEX.md       (Master index - update!)
```

### 7. Scripts Directory (scripts/)
```
scripts/
├── README.md
├── monitoring/
│   └── monitoring-start.sh
├── testing/
│   ├── run-api-tests.sh
│   └── run-integration-tests.sh
└── deployment/
    └── deploy.sh (future)
```

### 8. Infrastructure Directory (infra/)
```
infra/
├── README.md
├── monitoring/
│   ├── README.md
│   ├── prometheus.yml
│   └── grafana/
├── docker/
│   ├── Dockerfile.dev
│   └── Dockerfile.prod
└── kubernetes/  (future)
    └── ...
```

### 9. Tools Directory (tools/)
```
tools/
├── README.md
└── bug-bounty-hunter/
    ├── README.md
    ├── QUICKSTART.md
    ├── cli.py
    ├── ... (all existing files)
```

---

## Migration Steps

### Phase 1: Backend Restructuring

#### 1.1 Create New Package Structure
```bash
mkdir -p src/main/java/com/pisystem/{config,shared,core,modules,integrations,infrastructure,devtools}
```

#### 1.2 Move Core Modules
- `com.main` → `com.pisystem` (root package)
- `com.auth` → `com.pisystem.core.auth`
- `com.users` → `com.pisystem.core.users`
- `com.admin` → `com.pisystem.core.admin`

#### 1.3 Move Business Modules
- `com.budget` → `com.pisystem.modules.budget`
- `com.tax` → `com.pisystem.modules.tax`
- `com.portfolio` → `com.pisystem.modules.portfolio`
- `com.stocks` → `com.pisystem.modules.stocks`
- `com.mutualfund` → `com.pisystem.modules.mutualfunds`
- `com.etf` → `com.pisystem.modules.etf`
- `com.lending` → `com.pisystem.modules.lending`
- `com.loan` → `com.pisystem.modules.loans`
- `com.protection` → `com.pisystem.modules.insurance`
- `com.savings` → `com.pisystem.modules.savings`
- `com.sms` → `com.pisystem.modules.sms`
- `com.payments.upi` → `com.pisystem.modules.upi`

#### 1.4 Move Integrations
- `com.aa` → `com.pisystem.integrations.accountaggregator`
- `com.externalServices` → `com.pisystem.integrations.externalservices`

#### 1.5 Move Infrastructure
- `com.healthstatus` → `com.pisystem.infrastructure.healthcheck`
- `com.websocket` → `com.pisystem.infrastructure.websocket`
- `com.alerts` → `com.pisystem.infrastructure.alerts`

#### 1.6 Move Shared/Common
- `com.common` → `com.pisystem.shared`
- `com.audit` → `com.pisystem.shared.audit`

#### 1.7 Move Dev Tools
- `com.api.testrunner` → `com.pisystem.devtools.testrunner`

#### 1.8 Update Package Declarations
- Update all package declarations
- Update all imports across the codebase
- Update Spring component scanning in main application class

#### 1.9 Update Test Structure
- Mirror the main structure in test/java
- Organize into unit/, integration/, contract/

### Phase 2: Frontend Restructuring

#### 2.1 Create Feature Directories
```bash
cd frontend/src
mkdir -p features/{auth,dashboard,budget,tax,portfolio,goals,banking,loans,insurance,lending,documents,credit,insights,settings,admin,ai-assistant}
mkdir -p shared/{components,layouts,utils,services,contexts,styles,hooks}
mkdir -p app/{router,providers,config}
```

#### 2.2 Move Pages to Features
- `pages/Login.jsx` → `features/auth/pages/Login.jsx`
- `pages/Register.jsx` → `features/auth/pages/Register.jsx`
- `pages/ForgotPassword.jsx` → `features/auth/pages/ForgotPassword.jsx`
- `pages/Dashboard.jsx` → `features/dashboard/pages/Dashboard.jsx`
- `pages/Budget.jsx` → `features/budget/pages/Budget.jsx`
- `pages/Tax.jsx` → `features/tax/pages/Tax.jsx`
- ... (continue for all pages)

#### 2.3 Move Components to Features
Group feature-specific components with their features:
- Tax components → `features/tax/components/`
- Budget components → `features/budget/components/`
- Lending components → `features/lending/components/`
- Insurance components → `features/insurance/components/`

Move shared components:
- Generic/reusable components → `shared/components/`

#### 2.4 Move API Clients
- `api/taxApi.js` → `features/tax/services/taxApi.js`
- `api/lendingApi.js` → `features/lending/services/lendingApi.js`
- `services/lendingApi.js` → `features/lending/services/lendingApi.js`
- `api.js` → `shared/services/api.js`
- ... (continue for all APIs)

#### 2.5 Update Imports
- Update all import paths throughout the codebase
- Ensure routing paths remain consistent

### Phase 3: Documentation & Root Cleanup

#### 3.1 Create Docs Structure
```bash
mkdir -p docs/{architecture,modules,deployment,testing,api,features,operations,planning,archive}
```

#### 3.2 Move Documentation Files
Move all markdown files from root to appropriate docs/ subdirectories

#### 3.3 Create Scripts Directory
```bash
mkdir -p scripts/{monitoring,testing,deployment}
```

Move:
- `monitoring-start.sh` → `scripts/monitoring/monitoring-start.sh`
- `run-api-tests.sh` → `scripts/testing/run-api-tests.sh`
- `run-integration-tests.sh` → `scripts/testing/run-integration-tests.sh`

#### 3.4 Create Infra Directory
```bash
mkdir -p infra/monitoring
```

Move:
- `monitoring/` → `infra/monitoring/`

#### 3.5 Create Tools Directory
```bash
mkdir -p tools
```

Move:
- `bug-bounty-hunter/` → `tools/bug-bounty-hunter/`

#### 3.6 Clean Root Directory
Remove/archive:
- `QuickRegexTest.class`
- `TestPatterns.class`
- `ls /`
- Obsolete markdown files (after moving to docs/)

### Phase 4: Configuration Updates

#### 4.1 Update Spring Boot Configuration
- Update component scanning base package
- Update application.yml if needed
- Verify no hardcoded package paths

#### 4.2 Update Gradle Configuration
- No changes needed (source directories remain same)
- Verify test source paths

#### 4.3 Update Frontend Build Config
- Update Vite config if needed for path aliases
- Add path aliases for cleaner imports:
  ```js
  resolve: {
    alias: {
      '@': '/src',
      '@features': '/src/features',
      '@shared': '/src/shared',
      '@app': '/src/app'
    }
  }
  ```

#### 4.4 Update Docker Configuration
- Verify COPY commands in Dockerfile
- Update docker-compose.yml if needed

---

## Risk Analysis & Mitigation

### 🔴 HIGH RISK - Requires Careful Handling

#### 1. Spring Component Scanning
**Risk:** Moving packages may break Spring's component scanning.

**Impact:** Application won't start; beans won't be found.

**Mitigation:**
- Update `@SpringBootApplication` or `@ComponentScan` annotations
- Before: `@ComponentScan(basePackages = "com")`
- After: `@ComponentScan(basePackages = "com.pisystem")`
- Test application startup after each major move

#### 2. Import Statements (Backend)
**Risk:** Hundreds of import statements need updating.

**Impact:** Compilation failures across the codebase.

**Mitigation:**
- Use IDE refactoring tools (Rename/Move)
- Move packages incrementally
- Compile and fix errors after each move
- Run tests frequently

#### 3. Frontend Import Paths
**Risk:** Many import statements in React components.

**Impact:** Runtime errors, broken features.

**Mitigation:**
- Use VSCode refactoring where possible
- Implement path aliases in Vite config
- Update imports systematically
- Test each feature after moving

#### 4. API Routes (Backend)
**Risk:** Moving controllers could break API endpoints if not careful.

**Impact:** Frontend can't communicate with backend.

**Mitigation:**
- Controllers' `@RequestMapping` paths are unchanged
- Package location doesn't affect URL routes
- Verify all endpoints still work post-migration
- Run integration tests

### 🟡 MEDIUM RISK - Test Thoroughly

#### 5. Repository Layer
**Risk:** JPA repositories might have issues if package scanning is configured.

**Mitigation:**
- Update `@EnableJpaRepositories` if it has basePackages
- Test database connections and queries

#### 6. Test Files
**Risk:** Test package structure must match source structure.

**Mitigation:**
- Mirror the new package structure in tests
- Update test package declarations
- Run full test suite after migration

#### 7. Flyway Migrations
**Risk:** Database migrations should be unaffected, but verify.

**Mitigation:**
- No changes needed (migrations are in resources/db/)
- Verify migration history table is intact

### 🟢 LOW RISK - Should Work Seamlessly

#### 8. Static Resources
**Risk:** Minimal - resources are location-independent.

**Mitigation:**
- Verify resource loading paths
- Check application.yml for any resource paths

#### 9. Build System
**Risk:** Minimal - Gradle doesn't care about package names.

**Mitigation:**
- Run `./gradlew clean build` to verify

#### 10. Frontend Assets
**Risk:** Minimal - asset paths are relative or configured.

**Mitigation:**
- Verify asset imports after restructuring

---

## Testing Strategy

### 1. Unit Tests
- Run after each phase
- Verify all tests pass
- Fix broken imports immediately

### 2. Integration Tests
- Run API tests: `./scripts/testing/run-api-tests.sh`
- Test database interactions
- Verify external service integrations

### 3. Manual Testing
- Test critical user workflows:
  - Login/Authentication
  - Budget creation
  - Tax calculations
  - Portfolio operations
  - Loan management
  - Insurance tracking

### 4. Smoke Testing
- Application startup
- Health check endpoint
- Database connectivity
- Frontend loads correctly
- API endpoints respond

---

## Implementation Timeline

### Phase 1: Backend (Estimated: 4-6 hours)
1. Create new package structure (30 min)
2. Move core modules (1 hour)
3. Move business modules (2 hours)
4. Move infrastructure & integrations (1 hour)
5. Update imports & test (1-2 hours)

### Phase 2: Frontend (Estimated: 3-4 hours)
1. Create feature structure (30 min)
2. Move pages (1 hour)
3. Move components (1 hour)
4. Move API clients (30 min)
5. Update imports & test (1 hour)

### Phase 3: Documentation (Estimated: 1-2 hours)
1. Create docs structure (15 min)
2. Move documentation files (1 hour)
3. Update index and links (30 min)

### Phase 4: Root Cleanup (Estimated: 1 hour)
1. Organize scripts (20 min)
2. Organize infra/tools (20 min)
3. Clean artifacts (20 min)

### Phase 5: Testing & Validation (Estimated: 2-3 hours)
1. Run all tests
2. Manual testing
3. Fix issues
4. Documentation updates

**Total Estimated Time: 11-16 hours**

---

## Rollback Plan

If critical issues arise:

1. **Git Safety:** All changes in Git, easy to revert
2. **Incremental:** Each phase is independent
3. **Testing:** Test after each phase before proceeding
4. **Backup:** Keep backup of current state before starting

---

## Post-Migration Tasks

### 1. Update Documentation
- [ ] Update README.md with new structure
- [ ] Create ARCHITECTURE.md documenting new organization
- [ ] Update CONTRIBUTING.md with new conventions

### 2. Add Path Aliases
- [ ] Configure Vite aliases for frontend
- [ ] Document import conventions

### 3. CI/CD Updates
- [ ] Update GitHub Actions if paths changed
- [ ] Update deployment scripts if needed

### 4. Developer Communication
- [ ] Notify team of new structure
- [ ] Provide migration guide for in-flight branches
- [ ] Update onboarding documentation

---

## Success Criteria

✅ All application features work as before  
✅ All tests pass  
✅ API endpoints respond correctly  
✅ Frontend renders without errors  
✅ Documentation is organized and accessible  
✅ Root directory is clean and professional  
✅ Code is more maintainable and discoverable  
✅ Clear module boundaries established  

---

## Next Steps

1. **Review this plan** with team/stakeholders
2. **Create a feature branch** for the migration
3. **Execute Phase 1** (Backend restructuring)
4. **Test & validate** Phase 1
5. **Execute remaining phases** incrementally
6. **Merge to main** after full validation

---

**Document Version:** 1.0  
**Last Updated:** March 27, 2026  
**Status:** Ready for Implementation
