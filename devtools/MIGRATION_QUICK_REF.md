# 🔄 Migration Generator - Quick Reference Card

## 🚀 Quick Commands

```bash
# Detect what changed
npm run migrate:drift

# Generate migration
npm run migrate:generate "Your description"

# Save baseline
npm run migrate:snapshot

# View schema
npm run migrate:schema
```

## 📋 Typical Workflow

### 1️⃣ First Time Setup
```bash
# Save current state as baseline
npm run migrate:snapshot
```

### 2️⃣ After Entity Changes
```bash
# Check what changed
npm run migrate:drift

# Generate migration SQL
npm run migrate:generate "Add user preferences"

# Review the file
cat src/main/resources/db/migration/V*.sql

# Apply migration
./gradlew flywayMigrate

# Update baseline
npm run migrate:snapshot
```

### 3️⃣ After Pulling Changes
```bash
# Apply team's migrations
./gradlew flywayMigrate

# Update your baseline
npm run migrate:snapshot
```

## 🎯 What It Detects

| Change | Detection | SQL Generated |
|--------|-----------|---------------|
| New Entity | ✅ | `CREATE TABLE` |
| New Field | ✅ | `ALTER TABLE ADD COLUMN` |
| Modified Field | ✅ | `ALTER TABLE MODIFY COLUMN` |
| Removed Field | ✅ | `ALTER TABLE DROP COLUMN` (commented) |
| New Index | ✅ | `CREATE INDEX` |
| Unique Constraint | ✅ | `ALTER TABLE ADD CONSTRAINT` |
| Dropped Table | ✅ | `DROP TABLE` (commented) |

## 🔌 REST API Quick Reference

### Detect Drift
```http
GET /open/dev/migration/detect-drift
```

### Generate from Entities
```http
POST /open/dev/migration/generate-from-entities?description=My_Changes
```

### View Schema
```http
GET /open/dev/migration/schema/current
```

### Save Snapshot
```http
POST /open/dev/migration/schema/snapshot
```

### Manual SQL Migration
```http
POST /open/dev/migration/generate
Content-Type: application/json

{
  "description": "Custom_Migration",
  "sql": "CREATE INDEX idx_email ON users(email);"
}
```

## 📁 File Locations

| File | Purpose |
|------|---------|
| `src/main/resources/db/migration/V*.sql` | Migration files |
| `src/main/resources/db/schema-snapshot.json` | Baseline schema |
| `devtools/migration-cli.js` | CLI tool |

## ⚡ Examples

### Example 1: Add New Column
```java
@Entity
public class User {
    // Existing fields...
    
    @Column(length = 100)
    private String displayName;  // NEW
}
```

```bash
npm run migrate:drift
# Output: Add column: users.display_name

npm run migrate:generate "Add display name"
# Creates: V29__Add_display_name.sql
```

### Example 2: Add Index
```java
@Entity
@Table(indexes = {
    @Index(name = "idx_user_email", columnList = "email")  // NEW
})
public class User {
    // fields...
}
```

```bash
npm run migrate:drift
# Output: Add index: idx_user_email on users

npm run migrate:generate "Add email index"
```

### Example 3: Change Field Type
```java
@Column(precision = 19, scale = 4)  // Changed from 15,2
private BigDecimal amount;
```

```bash
npm run migrate:drift
# Output: Modify column: expenses.amount (DECIMAL(15,2) -> DECIMAL(19,4))

npm run migrate:generate "Increase amount precision"
```

## ⚙️ Configuration

### Custom Backend URL
```bash
export API_URL=http://localhost:9090
npm run migrate:drift
```

### Snapshot Location
```
src/main/resources/db/schema-snapshot.json
```
**Tip**: Commit this file to share with team!

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "No changes detected" | Run `npm run migrate:snapshot` to reset baseline |
| Connection error | Check backend: `curl http://localhost:8080/actuator/health` |
| Wrong version number | Auto-detected from existing migrations |
| Snapshot conflicts | Delete and regenerate: `rm schema-snapshot.json && npm run migrate:snapshot` |

## 📚 Learn More

[Full Documentation](MIGRATION_GENERATOR.md)

---

**💡 Pro Tips**
- Always review generated SQL before applying
- Commit snapshot file with migrations
- One migration per logical feature
- Drop statements are commented for safety
