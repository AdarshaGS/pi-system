# Migration System Quick Reference

## Common Commands

### Development

```bash
# Run migrations locally
./gradlew bootRun

# Run only Flyway migrations
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo

# Validate migrations
./gradlew flywayValidate

# Clean database (DANGER!)
./gradlew flywayClean
```

### Production

```bash
# Get migration status
curl http://localhost:8080/actuator/migrations/status

# Validate all migrations
curl http://localhost:8080/actuator/migrations/validate

# Detect schema drift
curl http://localhost:8080/actuator/migrations/drift

# Get migration history
curl http://localhost:8080/actuator/migrations/history

# Get recent migrations (last 24h)
curl http://localhost:8080/actuator/migrations/recent

# Health check
curl http://localhost:8080/actuator/migrations/health
```

## Migration File Templates

### Versioned Migration

```sql
-- V{version}__{description}.sql
-- Example: V62__add_email_verification.sql

-- Add email verification column
ALTER TABLE users 
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Add verification timestamp
ALTER TABLE users
ADD COLUMN verified_at TIMESTAMP NULL;

-- Create index for quick lookup
CREATE INDEX idx_users_email_verified ON users(email_verified);
```

### Repeatable Migration

```sql
-- R__{description}.sql
-- Example: R__user_statistics_view.sql

CREATE OR REPLACE VIEW user_statistics AS
SELECT
    u.id,
    u.username,
    COUNT(DISTINCT e.id) as expense_count,
    COUNT(DISTINCT i.id) as income_count,
    SUM(e.amount) as total_expenses,
    SUM(i.amount) as total_income
FROM users u
LEFT JOIN expenses e ON u.id = e.user_id
LEFT JOIN incomes i ON u.id = i.user_id
GROUP BY u.id, u.username;
```

### Backward-Compatible Migration

```sql
-- V63__migrate_name_columns.sql
-- Part 1: EXPAND - Add new column

ALTER TABLE users ADD COLUMN full_name VARCHAR(200);

-- Note: Deploy code that populates full_name and reads from it
-- Next migration will drop old columns
```

```sql
-- V64__remove_old_name_columns.sql
-- Part 2: CONTRACT - Remove old columns
-- (Only after code deployment)

ALTER TABLE users DROP COLUMN first_name;
ALTER TABLE users DROP COLUMN last_name;
```

## Troubleshooting Checklist

### Migration Failed

1. Check error in logs: `migration_history` table
2. Review SQL syntax
3. Check for missing dependencies (foreign keys, tables)
4. Manually rollback if needed
5. Fix and re-run

### Checksum Mismatch

1. **NEVER** modify existing migration files
2. Create new migration to fix issue
3. If absolutely required, repair Flyway:
   ```sql
   UPDATE flyway_schema_history 
   SET checksum = {new-checksum}
   WHERE version = '{version}';
   ```

### Permission Denied

```sql
-- Grant permissions to migration user
GRANT ALL PRIVILEGES ON pisystem.* TO 'migration_user'@'%';
FLUSH PRIVILEGES;
```

## Best Practices Checklist

- [ ] One logical change per migration
- [ ] Test on local database first
- [ ] Include rollback plan in comments
- [ ] No destructive operations by default
- [ ] Use expand-migrate-contract for schema changes
- [ ] Name migrations descriptively
- [ ] Never modify executed migrations
- [ ] Backup database before production migration
- [ ] Monitor long-running migrations

## Migration Workflow

```
1. Create migration file
   ├─→ V{next-version}__{description}.sql
   └─→ Place in src/main/resources/db/migration/

2. Test locally
   ├─→ ./gradlew bootRun
   └─→ Verify schema changes

3. Code Review
   ├─→ Review SQL syntax
   ├─→ Check for destructive operations
   └─→ Verify backward compatibility

4. Deploy to Staging
   ├─→ Run migration
   ├─→ Validate: curl /actuator/migrations/validate
   └─→ Check drift: curl /actuator/migrations/drift

5. Deploy to Production
   ├─→ Backup database
   ├─→ Run migration
   ├─→ Monitor logs
   ├─→ Validate schema
   └─→ Health check

6. Post-Deployment
   ├─→ Verify application health
   ├─→ Check migration_history table
   └─→ Monitor for errors
```

## Configuration Presets

### Development (Fast & Loose)

```yaml
migration:
  validation-enabled: false
  fail-on-validation-error: false
  drift-detection-enabled: false
  async-execution: false
  allow-destructive-operations: true
```

### Staging (Validation Focused)

```yaml
migration:
  validation-enabled: true
  fail-on-validation-error: true
  drift-detection-enabled: true
  fail-on-drift: false
  async-execution: false
  allow-destructive-operations: false
```

### Production (Strict & Safe)

```yaml
migration:
  validation-enabled: true
  fail-on-validation-error: true
  drift-detection-enabled: true
  fail-on-drift: false  # Log but don't fail
  async-execution: true  # Non-blocking startup
  allow-destructive-operations: false
  backward-compatibility-checks: true
```

## Monitoring Queries

```sql
-- Check last 10 migrations
SELECT version, description, status, executed_at, execution_time_ms
FROM migration_history
ORDER BY executed_at DESC
LIMIT 10;

-- Failed migrations
SELECT * FROM migration_history
WHERE status = 'FAILED'
ORDER BY executed_at DESC;

-- Long-running migrations (>30s)
SELECT version, description, execution_time_ms/1000 as seconds
FROM migration_history
WHERE execution_time_ms > 30000
ORDER BY execution_time_ms DESC;

-- Migrations by module
SELECT module, COUNT(*) as count, SUM(execution_time_ms) as total_time_ms
FROM migration_history
WHERE status = 'SUCCESS'
GROUP BY module;
```

## Emergency Procedures

### Rollback Migration

```sql
-- 1. Manually reverse changes
START TRANSACTION;

-- Execute rollback SQL
ALTER TABLE users DROP COLUMN email_verified;

COMMIT;

-- 2. Update Flyway history
DELETE FROM flyway_schema_history WHERE version = '{version}';
DELETE FROM migration_history WHERE version = '{version}';
```

### Skip Failed Migration

```sql
-- Mark as success (only if safe!)
UPDATE flyway_schema_history 
SET success = TRUE 
WHERE version = '{version}';

UPDATE migration_history
SET status = 'SKIPPED'
WHERE version = '{version}';
```

### Reset Flyway (DANGER!)

```bash
# This will DROP all tables!
./gradlew flywayClean

# Then reapply all migrations
./gradlew flywayMigrate
```

---

Last Updated: 2026-04-09
Version: 1.0.0
