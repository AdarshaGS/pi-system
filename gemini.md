# GEMINI.md — pi-system

## Project Overview
pi-system is a backend-first financial core platform designed to evolve into a unified system for:
- UPI payments and PIN management
- Transaction processing and reconciliation
- Portfolio tracking and analytics
- Alerts, notifications, and monitoring
- Future expansion into merchant tooling and financial automation

The system is being built incrementally with production-grade standards from day one.

---

## Core Principles
- Correctness over speed
- Explicitness over magic
- Backend-first design
- Schema-driven development
- Fintech-grade safety and validation
- No silent failures

---

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA (Hibernate)
- MySQL 8
- HikariCP
- JWT-based authentication
- REST APIs (versioned)
- Scheduled jobs and WebSocket broadcasting (for prices/alerts)

---

## Architecture Guidelines
- Controllers must be thin (request/response mapping only)
- Business logic must live in Service layer
- Transactions are managed ONLY in Service layer
- No `@Transactional` on controllers or schedulers
- Read operations must use `@Transactional(readOnly = true)`
- No manual JDBC connection handling
- No auto-commit usage

---

## Database & Migrations
- MySQL 8 is the source of truth
- Schema changes must be done via migration scripts
- Avoid `IF NOT EXISTS` where unsafe; prefer versioned migrations
- Indexes must be explicitly justified (query-driven)
- Financial data must never be silently overwritten

---

## UPI & PIN Handling Rules
- PINs must always be stored as BCrypt hashes
- Never store raw PINs or passwords
- Confirm fields (`confirmPin`) are only for validation, never persistence
- PIN format rules:
  - Numeric only
  - Length: 4 or 6 digits
  - Reject weak sequences (e.g., 1234, 0000)
- Old PIN must always be validated before change
- Rate-limit PIN attempts (future enhancement)

---

## Transactions
- Every transaction must have:
  - Internal reference ID
  - Status lifecycle (INITIATED → SUCCESS / FAILED)
  - Timestamps for creation and completion
- No destructive updates on financial records
- Prefer append-only patterns where possible

---

## Alerts & Notifications
- Alerts are system-generated, not user-written
- Notifications must be auditable
- Read/unread state must be explicit
- Metadata should be flexible but bounded
- Failure to send alerts must not break core flows

---

## Error Handling
- Fail fast for invalid state
- Never swallow exceptions silently
- Log with context (userId, transactionId where applicable)
- Avoid cascading failures by isolating error logging transactions

---

## Scheduling & Background Jobs
- Scheduled jobs must be idempotent
- No long-running DB transactions
- Read-only jobs must be explicitly marked
- WebSocket broadcasting must not block DB threads

---

## Security Expectations
- JWT is mandatory for protected endpoints
- Role-based access must be enforced at API level
- No sensitive data in logs
- Validation must happen at API boundary

---

## API Design
- Versioned APIs (`/api/v1/...`)
- Explicit request/response DTOs
- No entity exposure directly from controllers
- Predictable error responses

---

## Coding Expectations
- Prefer clarity over cleverness
- Avoid premature optimization
- Write code as if it will be audited
- Assume this system will handle real money

---

## How Gemini / AI Should Assist
When working on this project:
- Assume fintech-grade correctness is required
- Ask clarifying questions if behavior affects money or security
- Prefer safe defaults
- Point out hidden edge cases
- Avoid shortcuts that compromise integrity
- Treat this as a long-lived core system

---

## Non-Goals (For Now)
- No frontend/UI decisions
- No speculative features
- No trading advice logic
- No auto-rebalancing
- No recommendation engines

---

## Future Direction (Context Only)
- Merchant dashboards
- Reconciliation tooling
- Monitoring & anomaly detection
- API hub for financial integrations
- Gradual expansion into a full financial operating system