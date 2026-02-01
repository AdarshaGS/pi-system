# 🔄 Migration Generator

Automated database migration generator that creates Flyway SQL scripts from JPA entity changes and tracks schema drift.

## 🎯 Overview

The Migration Generator tool helps you:
- **Auto-generate migrations** from JPA entity changes
- **Detect schema drift** between entities and database
- **Track schema history** with snapshots
- **Generate SQL** with proper Flyway versioning
- **Validate changes** before applying

## 🚀 Quick Start

### 1. Initial Setup

Save your current schema as a baseline:
```bash
npm run migrate:snapshot
```

This creates `src/main/resources/db/schema-snapshot.json` containing your current entity definitions.

### 2. Make Entity Changes

Modify your JPA entities:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    // NEW: Add a field
    @Column(length = 100)
    private String displayName;
}
```

### 3. Detect Drift

Check what's changed:
```bash
npm run migrate:drift
```

Output:
```
🔍 Detecting schema drift...

⚠️  Detected 1 schema changes:

ADD_COLUMN:
  • Add column: users.display_name

💡 Run "npm run migrate:generate" to create migration
```

### 4. Generate Migration

Create the migration SQL:
```bash
npm run migrate:generate "Add user display name"
```

Output:
```
🚀 Generating migration from entities...

✅ Migration generated: V29__Add_user_display_name.sql
📁 Location: /path/to/src/main/resources/db/migration/V29__Add_user_display_name.sql

📋 Changes included:
  • Add column: users.display_name

📄 Migration SQL:
────────────────────────────────────────────────────────────
-- Migration: Add user display name
-- Generated: 2026-02-01T10:30:00
-- Changes: 1

-- Add new columns
ALTER TABLE users ADD COLUMN display_name VARCHAR(100) NOT NULL;
────────────────────────────────────────────────────────────
```

### 5. Update Snapshot

After reviewing and running the migration, update your snapshot:
```bash
npm run migrate:snapshot
```

## 📋 CLI Commands

| Command | Description |
|---------|-------------|
| `npm run migrate:drift` | Detect schema changes |
| `npm run migrate:generate [description]` | Generate migration from changes |
| `npm run migrate:snapshot` | Save current schema as baseline |
| `npm run migrate:schema` | View current entity schema |
| `npm run migrate:help` | Show help message |

### Examples

```bash
# Detect drift
npm run migrate:drift

# Generate migration with description
npm run migrate:generate "Add email verification fields"

# View current schema
npm run migrate:schema

# Save snapshot after changes
npm run migrate:snapshot
```

## 🔌 API Endpoints

The migration generator also provides REST API endpoints:

### 1. Detect Drift
```http
GET /open/dev/migration/detect-drift
```

Response:
```json
{
  "hasChanges": true,
  "changeCount": 2,
  "changes": [
    {
      "type": "ADD_COLUMN",
      "tableName": "users",
      "columnName": "display_name",
      "description": "Add column: users.display_name"
    }
  ]
}
```

### 2. Generate Migration from Entities
```http
POST /open/dev/migration/generate-from-entities?description=Add_Display_Name
```

Response:
```json
{
  "fileName": "V29__Add_Display_Name.sql",
  "fullPath": "/path/to/migration/V29__Add_Display_Name.sql",
  "content": "-- Migration SQL...",
  "message": "Migration generated with 1 changes",
  "schemaChanges": [
    "Add column: users.display_name"
  ]
}
```

### 3. Get Current Schema
```http
GET /open/dev/migration/schema/current
```

Returns array of all entity schemas with tables, columns, indexes, and constraints.

### 4. Save Schema Snapshot
```http
POST /open/dev/migration/schema/snapshot
```

Saves current entity schema as baseline for future drift detection.

### 5. Manual Migration (SQL)
```http
POST /open/dev/migration/generate
Content-Type: application/json

{
  "description": "Add_Custom_Index",
  "sql": "CREATE INDEX idx_user_email ON users(email);"
}
```

## 🎓 How It Works

### 1. Entity Scanning

The tool scans all JPA entities using Spring's `ClassPathScanningCandidateComponentProvider`:

```java
@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, expense_date")
})
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
}
```

Extracts:
- Table name
- Columns (name, type, constraints)
- Indexes
- Unique constraints
- Foreign keys

### 2. Schema Comparison

Compares current entities with saved snapshot to detect:
- **New tables** - CREATE TABLE statements
- **Dropped tables** - DROP TABLE (commented)
- **New columns** - ALTER TABLE ADD COLUMN
- **Dropped columns** - ALTER TABLE DROP COLUMN (commented)
- **Modified columns** - ALTER TABLE MODIFY COLUMN
- **Index changes** - CREATE/DROP INDEX

### 3. SQL Generation

Generates Flyway-compatible SQL with:
- Proper versioning (V29, V30, etc.)
- Formatted SQL with uppercase keywords
- Comments explaining changes
- Safe defaults (IF NOT EXISTS, commented drops)

### 4. Snapshot Management

Stores schema snapshots in `src/main/resources/db/schema-snapshot.json`:

```json
[
  {
    "className": "Expense",
    "tableName": "expenses",
    "columns": [
      {
        "columnName": "id",
        "sqlType": "BIGINT",
        "primaryKey": true,
        "autoIncrement": true,
        "nullable": false
      },
      {
        "columnName": "amount",
        "sqlType": "DECIMAL(15, 2)",
        "nullable": false
      }
    ]
  }
]
```

## 📚 Supported Entity Features

### Column Types
- ✅ String (VARCHAR, TEXT)
- ✅ Numbers (INT, BIGINT, DECIMAL, DOUBLE, FLOAT)
- ✅ Boolean
- ✅ Dates (DATE, TIMESTAMP, TIME)
- ✅ Enums (STRING or ORDINAL)
- ✅ BigDecimal with precision/scale

### Annotations
- ✅ `@Entity` - Entity detection
- ✅ `@Table` - Table name, indexes, unique constraints
- ✅ `@Column` - Column properties
- ✅ `@Id` - Primary key
- ✅ `@GeneratedValue` - Auto increment
- ✅ `@Enumerated` - Enum mapping
- ✅ `@ManyToOne`, `@JoinColumn` - Foreign keys
- ✅ `@Index` - Index definitions
- ✅ `@UniqueConstraint` - Unique constraints

## 🔧 Configuration

### Backend URL

Set custom backend URL:
```bash
export API_URL=http://localhost:9090
npm run migrate:drift
```

### Snapshot Location

Snapshot file: `src/main/resources/db/schema-snapshot.json`

### Migration Location

Migrations: `src/main/resources/db/migration/V{version}__{description}.sql`

## 🎯 Workflow

### Development Workflow

```bash
# 1. Start development
npm run migrate:snapshot  # Save baseline

# 2. Make entity changes
# Edit your JPA entities...

# 3. Check what changed
npm run migrate:drift

# 4. Generate migration
npm run migrate:generate "My changes"

# 5. Review generated SQL
cat src/main/resources/db/migration/V*.sql

# 6. Run migration
./gradlew flywayMigrate

# 7. Update snapshot
npm run migrate:snapshot
```

### Team Workflow

**When pulling changes:**
```bash
git pull
./gradlew flywayMigrate      # Apply new migrations
npm run migrate:snapshot     # Update your snapshot
```

**Before committing:**
```bash
npm run migrate:drift        # Check for uncommitted changes
npm run migrate:generate     # Generate if needed
git add src/main/resources/db/migration/
git commit -m "Add migration for..."
```

## ⚠️ Important Notes

### Schema Snapshot

- **Commit snapshot file** (`schema-snapshot.json`) to version control
- Each team member should run migrations and update their snapshot
- Snapshot tracks the expected state, not database state

### Drop Operations

Drop statements are **commented by default** for safety:
```sql
-- ALTER TABLE users DROP COLUMN old_field;
-- DROP TABLE old_table;
```

Uncomment manually after verification.

### Complex Changes

For complex schema changes:
1. Generate migration as starting point
2. Manually edit SQL file
3. Add data migrations if needed
4. Test thoroughly

### Multiple Changes

Generate one migration per logical feature:
```bash
npm run migrate:generate "Add user preferences"
# Review and test
npm run migrate:snapshot

npm run migrate:generate "Add notification settings"  
# Next feature
```

## 🐛 Troubleshooting

### "No schema changes detected" but I made changes

- Verify entity has `@Entity` annotation
- Check if snapshot file exists
- Run `npm run migrate:schema` to see current state
- Delete snapshot and regenerate: `npm run migrate:snapshot`

### Backend connection error

```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Set correct URL
export API_URL=http://localhost:8080
```

### Migration version conflicts

Check existing versions:
```bash
ls src/main/resources/db/migration/ | grep "^V"
```

Latest version is auto-detected and incremented.

### Snapshot out of sync

Reset snapshot:
```bash
rm src/main/resources/db/schema-snapshot.json
npm run migrate:snapshot
```

## 🎓 Advanced Usage

### View Detailed Schema

```bash
npm run migrate:schema
```

Shows all tables with columns, types, and constraints.

### Generate Custom SQL Migration

Use API or manual request:
```bash
curl -X POST http://localhost:8080/open/dev/migration/generate \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Custom_Migration",
    "sql": "ALTER TABLE users ADD INDEX idx_email (email);"
  }'
```

### Programmatic Access

```javascript
const { detectDrift, generateMigration } = require('./migration-cli');

async function myTool() {
  const drift = await detectDrift();
  if (drift.hasChanges) {
    await generateMigration('Auto migration');
  }
}
```

## 📖 Related Documentation

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [JPA Annotations Reference](https://docs.oracle.com/javaee/7/api/javax/persistence/package-summary.html)
- [Project Testing Guide](../TESTING_GUIDE.md)

---

**Last Updated**: February 2026  
**Maintainer**: Pi System Development Team
