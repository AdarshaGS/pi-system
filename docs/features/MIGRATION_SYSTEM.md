# Hybrid Database Migration System

## Overview

A production-grade database migration system for Spring Boot applications that extends Flyway with enterprise features:

- **Baseline Generation** - Generate V1__baseline.sql from existing database schema
- **Versioned Migrations** - Immutable SQL scripts with strict version control
- **Repeatable Migrations** - Views, stored procedures, functions that can be re-executed
- **Modular Structure** - Organize migrations by domain module
- **Migration Validation** - Checksum verification, execution order validation
- **Schema Drift Detection** - Compare database vs expected schema
- **Safe Migration Practices** - Enforce best practices, detect destructive operations
- **Backward Compatibility** - Expand-migrate-contract pattern validation
- **Observability** - Actuator endpoints, execution metrics, audit trail

---

## Architecture

### Components

```
infrastructure/migration/
├── domain/
│   ├── MigrationHistory.java         # Extended migration tracking entity
│   ├── MigrationType.java            # VERSIONED | REPEATABLE | BASELINE
│   ├── MigrationStatus.java          # PENDING | SUCCESS | FAILED
│   └── SchemaDrift.java              # Drift detection result object
├── repository/
│   └── MigrationHistoryRepository.java
├── service/
│   ├── MigrationManagerService.java            # Main orchestrator
│   ├── BaselineGeneratorService.java           # Schema → SQL generator
│   ├── MigrationValidationService.java         # Validation engine
│   └── SchemaDriftDetectionService.java        # Drift detector
├── controller/
│   └── MigrationActuatorController.java        # REST API
└── config/
    └── MigrationProperties.java                # Configuration
```

### Database Tables

- **flyway_schema_history** - Flyway's built-in migration tracking
- **migration_history** - Extended tracking with audit trail, execution metrics

---

## Configuration

### application.yml

```yaml
migration:
  # Baseline generation
  baseline-enabled: false
  baseline-file-name: V1__baseline.sql
  
  # Validation
  validation-enabled: true
  fail-on-validation-error: true
  
  # Schema drift detection
  drift-detection-enabled: true
  fail-on-drift: false
  
  # Modular migrations
  modular-enabled: true
  modules:
    - user
    - loan
    - payment
    - audit
  
  # Execution mode
  async-execution: false
  long-running-threshold-seconds: 30
  
  # Safety checks
  backward-compatibility-checks: true
  allow-destructive-operations: false
  
  # Archival
  archival-enabled: false
  archive-after-days: 365
  archive-directory: archive
  repeatable-directory: repeatable
```

---

## Migration File Structure

### Versioned Migrations

```
src/main/resources/db/migration/
├── V1__baseline.sql                      # Initial schema (generated)
├── V2__add_users_table.sql
├── V3__add_loans_table.sql
├── V4__add_payments_table.sql
├── ...
├── V60__add_sub_feature_flags.sql
└── V61__create_migration_history_table.sql
```

**Naming Convention:**
- `V{version}__{description}.sql`
- Version: integer, incrementing (V1, V2, V3...)
- Description: snake_case, descriptive

**Rules:**
- Immutable - never modify after execution
- One logical change per file
- No destructive operations without explicit flag
- Include rollback plan in comments

### Modular Structure (Optional)

```
db/migration/
├── user/
│   ├── V1__user_module_baseline.sql
│   ├── V2__add_user_profiles.sql
│   └── V3__add_user_preferences.sql
├── loan/
│   ├── V1__loan_module_baseline.sql
│   ├── V2__add_loan_payments.sql
│   └── V3__add_loan_amortization.sql
├── payment/
│   └── V1__payment_module_baseline.sql
└── audit/
    └── V1__audit_module_baseline.sql
```

### Repeatable Migrations

```
db/migration/repeatable/
├── R__create_user_summary_view.sql
├── R__create_loan_analysis_view.sql
├── R__calculate_interest_function.sql
└── R__generate_amortization_schedule_proc.sql
```

**Properties:**
- Executed whenever checksum changes
- Typically for views, stored procedures, functions
- Re-runnable by design

---

## Usage

### 1. Baseline Generation

Generate V1__baseline.sql from existing database:

```bash
# Via actuator endpoint
curl -X POST http://localhost:8080/actuator/migrations/baseline -H "Authorization: Bearer {admin-token}"

# Or enable in application.yml
migration:
  baseline-enabled: true
```

Output: `src/main/resources/db/migration/V1__baseline.sql`

### 2. Creating New Migrations

**Simple migration:**
```sql
-- V5__add_email_to_users.sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
CREATE INDEX idx_users_email ON users(email);
```

**Backward-compatible migration:**
```sql
-- V6__rename_amount_to_loan_amount.sql
-- Step 1: Add new column (expand)
ALTER TABLE loans ADD COLUMN loan_amount DECIMAL(15,2);

-- Step 2: Copy data (migrate)
UPDATE loans SET loan_amount = amount WHERE amount IS NOT NULL;

-- Step 3: Do NOT drop old column yet - wait for V7
-- This allows code to reference both columns during deployment
```

```sql
-- V7__remove_old_amount_column.sql (separate migration)
-- After code is deployed and using new column:
ALTER TABLE loans DROP COLUMN amount;
```

### 3. Repeatable Migrations

```sql
-- R__loan_summary_view.sql
CREATE OR REPLACE VIEW loan_summary AS
SELECT
    l.id,
    l.loan_amount,
    l.interest_rate,
    COUNT(p.id) as payment_count,
    SUM(p.amount) as total_paid
FROM loans l
LEFT JOIN payments p ON l.id = p.loan_id
GROUP BY l.id;
```

### 4. Running Migrations

**Automatic (on startup):**
```yaml
spring:
  flyway:
    enabled: true
```

**Manual:**
```bash
./gradlew flywayMigrate
```

**Async (non-blocking startup):**
```yaml
migration:
  async-execution: true
```

---

## Validation & Drift Detection

### Validation on Startup

Automatic checks:
- ✅ All migrations present
- ✅ No checksum mismatches
- ✅ Correct execution order
- ✅ No failed migrations
- ✅ No destructive operations (if disabled)

**Manual validation:**
```bash
curl http://localhost:8080/actuator/migrations/validate
```

Response:
```json
{
  "valid": true,
  "errors": [],
  "errorCount": 0
}
```

### Schema Drift Detection

Compares actual database schema vs expected:

```bash
curl http://localhost:8080/actuator/migrations/drift
```

Response:
```json
{
  "hasDrift": false,
  "missingTables": [],
  "extraTables": [],
  "columnMismatches": [],
  "missingIndexes": [],
  "extraIndexes": []
}
```

---

## Actuator Endpoints

Base URL: `/actuator/migrations`

### GET `/status`
Migration system status and statistics

```json
{
  "enabled": true,
  "totalMigrations": 61,
  "successfulMigrations": 60,
  "failedMigrations": 0,
  "pendingMigrations": 1,
  "lastExecutionStatus": "SUCCESS",
  "lastExecutionMessage": "Executed 1 migrations"
}
```

### GET `/history`
All executed migrations

Query params:
- `?module=user` - filter by module
- `?status=FAILED` - filter by status

### GET `/recent`
Migrations executed in last 24 hours

### GET `/validate`
Run validation checks

### GET `/drift`
Detect schema drift

### POST `/baseline`
Generate baseline (admin only)

### GET `/config`
Current migration configuration

### GET `/health`
Health check for migration system

---

## Best Practices

### 1. One Change Per Migration
✅ Good:
```sql
-- V5__add_email_to_users.sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
CREATE INDEX idx_users_email ON users(email);
```

❌ Bad:
```sql
-- V5__multiple_changes.sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE loans ADD COLUMN status VARCHAR(20);
CREATE TABLE payments (...);  -- Too many unrelated changes
```

### 2. Expand-Migrate-Contract Pattern

For backward-compatible changes:

**V1: Expand** (add new column)
```sql
ALTER TABLE users ADD COLUMN full_name VARCHAR(200);
```

**V2: Migrate** (copy data, deploy code)
```sql
UPDATE users SET full_name = CONCAT(first_name, ' ', last_name);
```

**V3: Contract** (remove old columns)
```sql
ALTER TABLE users DROP COLUMN first_name, DROP COLUMN last_name;
```

### 3. Avoid Destructive Operations

Use `allow-destructive-operations: false` (default) to prevent:
- DROP TABLE
- DROP COLUMN
- TRUNCATE
- DELETE FROM

Override only when intentional:
```sql
-- V10__remove_deprecated_table.sql
-- WARNING: Destructive operation
-- Ticket: JIRA-123
-- Approved by: Tech Lead
DROP TABLE IF EXISTS deprecated_table;
```

### 4. Include Rollback Plans

```sql
-- V8__add_verified_column.sql
-- Rollback: ALTER TABLE users DROP COLUMN verified;

ALTER TABLE users ADD COLUMN verified BOOLEAN DEFAULT FALSE;
```

### 5. Test Migrations in Lower Environments

1. Run on local dev database
2. Deploy to staging
3. Validate schema drift
4. Deploy to production with monitoring

---

## Monitoring & Observability

### Logging

All migration events logged with:
- Migration start/end time
- Execution duration
- Success/failure status
- Error messages

Log levels:
- `INFO` - Normal execution
- `WARN` - Long-running migrations, drift detected
- `ERROR` - Validation failures, execution errors

### Metrics

Access via `/actuator/migrations/status`:
- Total migrations
- Success/failure counts
- Pending count
- Last execution status
- Recent execution history

### Audit Trail

`migration_history` table tracks:
- What migrated (version, description, script)
- When executed (timestamp, duration)
- Who executed (user, application version)
- How it went (status, error message, checksum)

---

## Testing

### Integration Tests

```java
@SpringBootTest
@Testcontainers
class MigrationIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    
    @Autowired
    private MigrationValidationService validationService;
    
    @Test
    void shouldValidateMigrationsSuccessfully() {
        List<String> errors = validationService.validateMigrations();
        assertThat(errors).isEmpty();
    }
    
    @Test
    void shouldDetectChecksumMismatch() {
        // Modify a migration file
        // Verify validation catches it
    }
}
```

---

## Troubleshooting

### Checksum Mismatch

**Problem:** Migration file was modified after execution

**Solution:**
```sql
-- Option 1: Repair Flyway history (only if safe!)
UPDATE flyway_schema_history 
SET checksum = {new-checksum} 
WHERE version = '{version}';

-- Option 2: Create new migration to correct issue
-- (Preferred - maintains immutability)
```

### Out-of-Order Migration

**Problem:** New migration has version lower than already-executed ones

**Solution:**
```yaml
spring:
  flyway:
    out-of-order: true  # Allow but not recommended
```

Better: Renumber migration to latest version

### Failed Migration

**Problem:** Migration failed mid-execution

**Solution:**
1. Check `migration_history` for error details
2. Fix root cause (missing table, syntax error)
3. Manually complete/rollback transaction
4. Re-run migration

### Schema Drift

**Problem:** Database schema doesn't match expected

**Solution:**
1. Run drift detection: `/actuator/migrations/drift`
2. Review differences
3. Create migration to align schema
4. Or update migrations to match actual state

---

## Production Deployment Checklist

- [ ] Test migrations in staging environment identical to production
- [ ] Backup production database before migration
- [ ] Review execution plan (which migrations will run)
- [ ] Monitor long-running migrations (>30s threshold)
- [ ] Validate schema afterward `/actuator/migrations/validate`
- [ ] Check drift detection `/actuator/migrations/drift`
- [ ] Verify application health checks pass
- [ ] Monitor error logs for migration issues
- [ ] Keep rollback plan ready

---

## Advanced Features

### Baseline for New Environments

When onboarding new environment with existing database:

1. Generate baseline: `/actuator/migrations/baseline`
2. Mark existing migrations as executed: `flyway.baseline()`
3. Future migrations apply incrementally

### Archive Old Migrations

For long-lived projects with 100+ migrations:

```yaml
migration:
  archival-enabled: true
  archive-after-days: 365
```

Moves old migrations to `archive/` folder, baseline replaces them.

### Modular Migrations

Organize by bounded context:

```yaml
migration:
  modular-enabled: true
  modules:
    - user
    - loan
    - payment
```

Each module has independent migration sequence.

---

## API Reference

See [MigrationActuatorController.java](src/main/java/com/pisystem/infrastructure/migration/controller/MigrationActuatorController.java) for full API documentation.

---

## Contributing

When adding new migration capabilities:

1. Update domain models if needed
2. Implement service layer
3. Add actuator endpoint
4. Update configuration properties
5. Write integration tests
6. Document in this README

---

## License

Internal use - PI System project

---

## Support

Contact: DevOps Team
Docs: [Flyway Documentation](https://flywaydb.org/documentation/)
