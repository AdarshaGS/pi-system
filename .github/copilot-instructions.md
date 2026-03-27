# PI SYSTEM - Copilot Instructions

## Project Overview

PI SYSTEM is a Spring Boot + React monorepo for comprehensive personal finance and investment intelligence. The system tracks stocks, portfolio, mutual funds, ETFs, loans, lending, insurance, taxes, budgets, and more.

**Tech Stack:**
- **Backend**: Spring Boot 3.x, Java 17+, Gradle 8.x
- **Frontend**: React 18+, Vite 7.x, React Router v6
- **Database**: PostgreSQL (production), H2 (dev)
- **Real-time**: WebSocket for stock prices
- **Architecture**: Modular monolith with feature-based organization

## Project Structure

### Backend (`src/main/java/com/pisystem/`)
```
com.pisystem/
├── core/                    # Core domain entities
│   ├── entity/             # User, Transaction, Account, etc.
│   ├── repository/
│   └── dto/
├── modules/                 # Feature modules
│   ├── portfolio/          # Stocks, ETFs, Mutual Funds
│   ├── tax/                # Tax planning and compliance
│   ├── loans/              # Loan management
│   ├── lending/            # Money lending tracker
│   ├── insurance/          # Insurance policy tracking
│   ├── budget/             # Budget and cash flow
│   ├── banking/            # Bank accounts and FDs
│   ├── alerts/             # Notification system
│   └── admin/              # Admin portal
├── integrations/           # External APIs
│   ├── sms/                # SMS transaction parser
│   └── external/           # Third-party integrations
├── infrastructure/         # Cross-cutting concerns
│   ├── security/
│   ├── config/
│   └── exception/
└── shared/                 # Shared utilities
```

### Frontend (`frontend/src/`)
```
src/
├── features/               # Feature modules (co-located code)
│   ├── auth/              # Login, Register, ForgotPassword
│   ├── dashboard/         # Main dashboard
│   ├── budget/            # Budget pages, components, services
│   ├── tax/               # Tax pages, components, services
│   ├── portfolio/         # Portfolio management
│   ├── insurance/         # Insurance tracking
│   ├── lending/           # Lending management
│   ├── loans/             # Loan management
│   ├── banking/           # Banking features
│   └── [7 more features]
├── shared/                 # Cross-feature code
│   ├── components/        # Reusable components (AiAssistant, FeatureGate)
│   ├── layouts/           # Layout components
│   └── utils/             # Utility functions
├── core/                   # Infrastructure
│   ├── api.js             # Axios configuration
│   ├── contexts/          # React contexts (Feature, Tier)
│   └── websocket/         # WebSocket clients
└── App.jsx                # Main app component
```

## Coding Standards

### Backend

**Package Naming:**
- Use singular nouns: `com.pisystem.modules.portfolio` (not portfolios)
- No nested package duplication: `etf.controller` (not `etf.etf.controller`)
- Pattern: `modules.<feature>.<layer>` where layer is controller/service/repository

**Controller Best Practices:**
- Always use `@RestController` and `@RequestMapping`
- Return `ResponseEntity<?>` for explicit HTTP status control
- Use proper HTTP methods: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- Include `@PreAuthorize` when needed for security
- Path format: `/api/<module>/<resource>` (e.g., `/api/tax/capital-gains`)

**Service Layer:**
- Interface + Implementation pattern for complex services
- Single responsibility per service
- Use `@Transactional` for write operations
- Inject repositories via constructor injection

**DTOs:**
- Separate request/response DTOs (e.g., `TaxDetailsRequest`, `TaxDetailsResponse`)
- Use validation annotations: `@NotNull`, `@Valid`, `@Min`, `@Max`
- Include Javadoc for complex DTOs

**Exception Handling:**
- Use custom exceptions in `infrastructure.exception`
- Global exception handler with `@ControllerAdvice`
- Return meaningful error messages with proper HTTP status codes

### Frontend

**Import Patterns:**
- **ALWAYS use @ alias**: `import { api } from '@/core/api'`
- **Never use relative imports beyond one level**: Avoid `../../../`
- **Feature imports**: `@/features/budget/components/TagSelector`
- **Shared imports**: `@/shared/components/AiAssistant`
- **Core imports**: `@/core/api.js`

**Component Organization:**
- Pages go in `features/*/pages/` with `*Page.jsx` suffix
- Feature-specific components in `features/*/components/`
- Shared components in `shared/components/`
- Use `.jsx` extension for components with JSX

**API Service Pattern:**
```javascript
// features/tax/services/taxApi.js
import api from '@/core/api';

export const fetchTaxDetails = (userId, year) =>
  api.get(`/api/tax/details/${userId}/${year}`);

export const saveTaxDetails = (data) =>
  api.post('/api/tax/save', data);
```

**State Management:**
- Use React hooks (`useState`, `useEffect`) for local state
- Context API for cross-feature state (TierContext, FeatureContext)
- No Redux (keep it simple)

## Module-Specific Guidance

### Tax Module
- Capital gains calculation uses asset type and holding period
- Old vs New regime comparison requires full salary breakdown
- TDS tracking is quarterly with reconciliation support
- ITR filing assistant helps with Form 16 and deductions

### Portfolio Module
- Real-time stock prices via WebSocket (30s market hours, 5m off-hours)
- XIRR calculation for returns measurement
- Transaction types: BUY, SELL, DIVIDEND
- FIFO method for capital gains calculation

### Loans Module
- EMI calculation: `E = [P * r * (1 + r)^n] / [(1 + r)^n - 1]`
- Supports: Home Loan, Personal Loan, Car Loan, Education Loan
- Prepayment simulation with foreclosure analysis
- Amortization schedule generation

### Budget Module
- Recurring transactions with auto-application
- Tag-based categorization (user-defined tags)
- Cash flow projection (12 months forward)
- Bulk actions for transaction management

## Feature Flags & Tiers

**Free Tier Limits:**
- Transactions: 100/month
- Portfolio holdings: 10 stocks
- Loans: 2 active loans
- Insurance policies: 3 policies

**Premium Features:**
- Unlimited transactions
- Advanced tax optimization
- Real-time stock alerts
- AI-powered insights
- Priority support

**Feature Flag Pattern:**
- Use `FeatureContext` and `<FeatureGate>` component
- Backend: `@FeatureFlag` annotation (if exists)
- Always gracefully degrade for free tier users

## Testing Expectations

### Backend Tests
- Unit tests for all services (70%+ coverage)
- Integration tests for controllers with `@SpringBootTest`
- Test locations: `src/test/java/com/pisystem/`
- Run: `./gradlew test`

### Frontend Tests
- Component tests with React Testing Library
- Test files: `*.test.jsx` alongside component
- Run: `npm test`

## Build Commands

**Backend:**
```bash
./gradlew clean build          # Full build with tests
./gradlew compileJava          # Quick compile check
./gradlew bootRun              # Start server (port 8080)
```

**Frontend:**
```bash
cd frontend
npm install                    # Install dependencies
npm run dev                    # Dev server (port 5173)
npm run build                  # Production build
npm test                       # Run tests
```

## Common Pitfalls

1. **Nested Package Duplication**: Watch for `etf.etf`, `stocks.stocks` - flatten to single level
2. **Import Hell**: Always use @ alias, never relative imports beyond `./`
3. **CSS Import Mismatch**: When renaming `Tax.jsx` to `TaxPage.jsx`, update `./Tax.css` to `./TaxPage.css`
4. **API Service Location**: Feature-specific APIs belong in `features/*/services/`, not `core/api/`
5. **Missing Exports**: Ensure all functions used externally are exported
6. **WebSocket Cleanup**: Always disconnect WebSocket on component unmount

## Git Practices

- **Commit Messages**: Follow conventional commits (feat:, fix:, refactor:, docs:)
- **File Moves**: Use `git mv` to preserve history
- **Large Refactors**: Stage in logical chunks, provide detailed commit messages

---

_Last Updated: March 27, 2026_
_This file helps GitHub Copilot understand the PI SYSTEM project structure and conventions._
