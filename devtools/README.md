# 🛠️ Developer Tools

This directory contains tools to help organize and manage the Pi System project more effectively.

## 📦 Tools Overview

### 1. 📚 Documentation Index Generator

**File**: `doc-index-generator.js`

Automatically scans all markdown files in the project and generates:
- Complete searchable documentation index
- Navigation sidebar
- Categories and metadata for each document
- Flags outdated documents (not updated in 90+ days)

**Usage**:
```bash
node devtools/doc-index-generator.js
```

**Output**:
- `DOC_INDEX.md` - Main index with all documentation organized by category
- `DOC_NAVIGATION.md` - Quick navigation sidebar

**Features**:
- ✅ Categorizes docs automatically (API, Testing, Product, etc.)
- ✅ Shows last modified date from git history
- ✅ Identifies API docs, code examples, and tables
- ✅ Highlights outdated documentation
- ✅ Provides file size and heading count
- ✅ Creates quick navigation anchors

---

### 2. 🚀 API Documentation Dashboard

**File**: `api-doc-generator.js`

Scans all Spring Boot controllers and generates a comprehensive API documentation dashboard.

**Usage**:
```bash
node devtools/api-doc-generator.js
```

**Output**:
- `API_DASHBOARD.md` - Complete API inventory with:
  - All endpoints grouped by domain
  - HTTP methods, paths, and descriptions
  - Path variables and request parameters
  - Controller references
  - Statistics and quick navigation

**Features**:
- ✅ Parses `@RestController` annotations
- ✅ Extracts `@GetMapping`, `@PostMapping`, etc.
- ✅ Groups endpoints by domain (Budget, Stocks, etc.)
- ✅ Shows HTTP method distribution
- ✅ Links to controller source files
- ✅ Displays path variables and request params

**Domains Tracked**:
- Authentication & Users
- Budget Management
- Investments (Stocks, Mutual Funds, ETF)
- Savings & Insurance
- Lending & Tax
- External Services
- Health & Monitoring

---

### 3. 🎯 Local Development Dashboard

**File**: `dev-dashboard.html`

A single-page web dashboard that brings everything together in one place.

**Usage**:
```bash
# Open directly in browser
open devtools/dev-dashboard.html

# Or use a simple HTTP server
python3 -m http.server 8000
# Then navigate to http://localhost:8000/devtools/dev-dashboard.html
```

**Features**:
- ✅ **Service Status Monitoring**
  - Backend (Spring Boot) health check
  - Frontend (React/Vite) status
  - Database (H2) console access
  - Real-time status updates
  
- ✅ **Quick Links Hub**
  - API Dashboard
  - Documentation Index
  - Test Reports
  - Swagger UI
  - README files
  
- ✅ **Project Statistics**
  - Controller count
  - API endpoints
  - Documentation files
  - Domain status
  
- ✅ **Developer Actions**
  - Regenerate documentation
  - Run tests
  - Refresh service status
  
- ✅ **Log Viewer**
  - Recent application logs
  - Color-coded by severity

**Auto-Refresh**: Status checks run every 30 seconds automatically

---

### 4. 🔄 Migration Generator

**File**: `migration-cli.js`

Automated database migration generator that creates Flyway SQL scripts from JPA entity changes.

**Usage**:
```bash
# Detect schema drift
npm run migrate:drift

# Generate migration from entity changes
npm run migrate:generate "Add new fields"

# Save schema snapshot
npm run migrate:snapshot

# View current schema
npm run migrate:schema
```

**Features**:
- ✅ **Auto-generates migrations** from JPA entity changes
- ✅ **Detects schema drift** between entities and database
- ✅ **Tracks schema history** with snapshots
- ✅ **Generates proper Flyway SQL** with versioning
- ✅ **Supports all JPA annotations** (@Column, @Index, etc.)
- ✅ **CLI and REST API** interfaces

**Workflow**:
1. Make changes to JPA entities
2. Run `npm run migrate:drift` to detect changes
3. Run `npm run migrate:generate` to create migration SQL
4. Review and apply migration
5. Run `npm run migrate:snapshot` to update baseline

**Supported Changes**:
- Create/drop tables
- Add/drop/modify columns
- Add/drop indexes
- Add unique constraints
- Foreign key relationships

📖 **[Full Documentation](MIGRATION_GENERATOR.md)**

---

## 🚦 Quick Start

### First Time Setup

1. **Install Node.js** (if not already installed)
   ```bash
   # Check if Node is installed
   node --version
   ```

2. **Generate Documentation**
   ```bash
   # Generate doc index
   node devtools/doc-index-generator.js
   
   # Generate API dashboard
   node devtools/api-doc-generator.js
   ```

3. **Open Development Dashboard**
   ```bash
   open devtools/dev-dashboard.html
   ```

4. **Initialize Migration Tool**
   ```bash
   # Save current schema as baseline
   npm run migrate:snapshot
   ```

### Daily Workflow

**Morning Setup**:
```bash
# Open the dev dashboard
open devtools/dev-dashboard.html

# Start backend
./gradlew bootRun

# Start frontend (in another terminal)
cd frontend && npm run dev
```

**After Making Changes**:
```bash
# Update documentation
node devtools/doc-index-generator.js
node devtools/api-doc-generator.js

# Check for schema changes
npm run migrate:drift

# Generate migration if needed
npm run migrate:generate "Description of changes"

# Refresh the dashboard in your browser
```

---

## 📋 Common Tasks

### Regenerate All Documentation
```bash
# Run both generators
node devtools/doc-index-generator.js && node devtools/api-doc-generator.js
```

### Find Outdated Documentation
```bash
# Generate index (it will flag outdated docs)
node devtools/doc-index-generator.js

# Then check DOC_INDEX.md for 🔴 red flags
```

### Check API Endpoint Inventory
```bash
# Generate API dashboard
node devtools/api-doc-generator.js

# Open API_DASHBOARD.md to see all endpoints
```

### Generate Database Migration
```bash
# 1. Check what changed
npm run migrate:drift

# 2. Generate migration
npm run migrate:generate "Your description"

# 3. Review the SQL file
cat src/main/resources/db/migration/V*.sql

# 4. Apply migration
./gradlew flywayMigrate

# 5. Update snapshot
npm run migrate:snapshot
```

### View All Project Statistics
```bash
# Open dev dashboard
open devtools/dev-dashboard.html

# Check the "Project Stats" section
```

---

## 🔧 Customization

### Adding New Document Categories

Edit `doc-index-generator.js`:
```javascript
const CATEGORIES = {
  'Your New Category': ['KEYWORD1', 'KEYWORD2'],
  // ... existing categories
};
```

### Adding New API Domains

Edit `api-doc-generator.js`:
```javascript
const DOMAINS = {
  'Your New Domain': ['/api/v1/your-path'],
  // ... existing domains
};
```

### Customizing Dashboard Colors

Edit `dev-dashboard.html` - modify the `<style>` section:
```css
body {
  background: linear-gradient(135deg, #your-color 0%, #another-color 100%);
}
```

---

## 📊 Generated Files

These files are automatically generated by the tools:

- `DOC_INDEX.md` - Documentation index
- `DOC_NAVIGATION.md` - Navigation sidebar
- `API_DASHBOARD.md` - API endpoint inventory
- `src/main/resources/db/schema-snapshot.json` - Database schema snapshot

**Note**: You may want to add these to `.gitignore` if you prefer to generate them locally:
```bash
# Add to .gitignore
DOC_INDEX.md
DOC_NAVIGATION.md
API_DASHBOARD.md
```

Or commit `schema-snapshot.json` to share with the team:
```bash
# Keep this for team collaboration
src/main/resources/db/schema-snapshot.json
```

---

## 🐛 Troubleshooting

### "Cannot find module" error
```bash
# Make sure you're in the project root
cd /Users/adarshgs/Documents/Stocks/App/pi-system

# Run the command
node devtools/doc-index-generator.js
```

### Dashboard shows all services as "stopped"
- Check if services are actually running
- Check the port numbers (default: 8080 for backend, 5173 for frontend)
- Check browser console for CORS errors

### Documentation not updating
```bash
# Make sure you have git initialized
git status

# The tools use git history for metadata
```

### Migration tool can't connect
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Set custom URL if needed
export API_URL=http://localhost:8080
npm run migrate:drift
```

### "No schema changes detected" but I made changes
```bash
# View current schema
npm run migrate:schema

# Reset snapshot if needed
rm src/main/resources/db/schema-snapshot.json
npm run migrate:snapshot
```

---

## 🎯 NPM Scripts Reference

| Script | Description |
|--------|-------------|
| `npm run gen:docs` | Generate documentation index |
| `npm run gen:api` | Generate API dashboard |
| `npm run gen:all` | Generate all documentation |
| `npm run dashboard` | Open dev dashboard |
| `npm run migrate:drift` | Detect schema drift |
| `npm run migrate:generate` | Generate migration from entities |
| `npm run migrate:snapshot` | Save schema snapshot |
| `npm run migrate:schema` | View current schema |
| `npm run migrate:help` | Show migration help |

---

## 📚 Additional Resources

- [Migration Generator Documentation](MIGRATION_GENERATOR.md)
- [Project README](../README.md)
- [API Testing Guide](../API_TESTING.md)
- [Deployment Guide](../DEPLOYMENT.md)
- [Testing Guide](../TESTING_GUIDE.md)

---

## 🤝 Contributing

When adding new developer tools:

1. Create the tool file in `devtools/`
2. Add usage instructions to this README
3. Update the dev dashboard if needed
4. Test thoroughly before committing
5. Add to `package.json` scripts if it's a CLI tool

---

**Last Updated**: February 2026  
**Maintainer**: Pi System Development Team

## 🚦 Quick Start

### First Time Setup

1. **Install Node.js** (if not already installed)
   ```bash
   # Check if Node is installed
   node --version
   ```

2. **Generate Documentation**
   ```bash
   # Generate doc index
   node devtools/doc-index-generator.js
   
   # Generate API dashboard
   node devtools/api-doc-generator.js
   ```

3. **Open Development Dashboard**
   ```bash
   open devtools/dev-dashboard.html
   ```

### Daily Workflow

**Morning Setup**:
```bash
# Open the dev dashboard
open devtools/dev-dashboard.html

# Start backend
./gradlew bootRun

# Start frontend (in another terminal)
cd frontend && npm run dev
```

**After Making Changes**:
```bash
# Update documentation
node devtools/doc-index-generator.js
node devtools/api-doc-generator.js

# Refresh the dashboard in your browser
```

---

## 📋 Common Tasks

### Regenerate All Documentation
```bash
# Run both generators
node devtools/doc-index-generator.js && node devtools/api-doc-generator.js
```

### Find Outdated Documentation
```bash
# Generate index (it will flag outdated docs)
node devtools/doc-index-generator.js

# Then check DOC_INDEX.md for 🔴 red flags
```

### Check API Endpoint Inventory
```bash
# Generate API dashboard
node devtools/api-doc-generator.js

# Open API_DASHBOARD.md to see all endpoints
```

### View All Project Statistics
```bash
# Open dev dashboard
open devtools/dev-dashboard.html

# Check the "Project Stats" section
```

---

## 🔧 Customization

### Adding New Document Categories

Edit `doc-index-generator.js`:
```javascript
const CATEGORIES = {
  'Your New Category': ['KEYWORD1', 'KEYWORD2'],
  // ... existing categories
};
```

### Adding New API Domains

Edit `api-doc-generator.js`:
```javascript
const DOMAINS = {
  'Your New Domain': ['/api/v1/your-path'],
  // ... existing domains
};
```

### Customizing Dashboard Colors

Edit `dev-dashboard.html` - modify the `<style>` section:
```css
body {
  background: linear-gradient(135deg, #your-color 0%, #another-color 100%);
}
```

---

## 📊 Generated Files

These files are automatically generated by the tools:

- `DOC_INDEX.md` - Documentation index
- `DOC_NAVIGATION.md` - Navigation sidebar
- `API_DASHBOARD.md` - API endpoint inventory

**Note**: You may want to add these to `.gitignore` if you prefer to generate them locally:
```bash
# Add to .gitignore
DOC_INDEX.md
DOC_NAVIGATION.md
API_DASHBOARD.md
```

Or commit them to help team members who don't run the generators.

---

## 🐛 Troubleshooting

### "Cannot find module" error
```bash
# Make sure you're in the project root
cd /Users/adarshgs/Documents/Stocks/App/pi-system

# Run the command
node devtools/doc-index-generator.js
```

### Dashboard shows all services as "stopped"
- Check if services are actually running
- Check the port numbers (default: 8080 for backend, 5173 for frontend)
- Check browser console for CORS errors

### Documentation not updating
```bash
# Make sure you have git initialized
git status

# The tools use git history for metadata
```

---

## 🎯 Future Enhancements

Potential improvements to consider:

1. **Test Data Builder** - Generate realistic test fixtures
2. **Configuration Validator** - Check consistency across environments
3. **Migration Generator** - Auto-generate database migrations
4. **API Contract Monitor** - Detect breaking changes
5. **Dependency Analyzer** - Visualize module dependencies
6. **Performance Profiler** - Track API response times

---

## 📚 Additional Resources

- [Project README](../README.md)
- [API Testing Guide](../API_TESTING.md)
- [Deployment Guide](../DEPLOYMENT.md)
- [Testing Guide](../TESTING_GUIDE.md)

---

## 🤝 Contributing

When adding new developer tools:

1. Create the tool file in `devtools/`
2. Add usage instructions to this README
3. Update the dev dashboard if needed
4. Test thoroughly before committing

---

**Last Updated**: February 2026  
**Maintainer**: Pi System Development Team
