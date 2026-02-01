# Quick Feature Reference - Sprint 5 & 6

## 🎯 Quick Access

- **Frontend**: http://localhost:5174
- **Backend API**: http://localhost:8082
- **API Docs**: http://localhost:8082/swagger-ui.html (if enabled)

---

## 📝 Sprint 5: Export & Reports

### Export Expenses (Budget Page)
1. Click green **"Export"** button
2. Select date range
3. Choose categories (optional)
4. Select format: **CSV**, **Excel**, or **PDF**
5. Click **Export** → File downloads automatically

### Export Incomes (Cash Flow Page)
- Same process as expenses
- Export button available in Cash Flow page header

### Notes Field
- Available in Add/Edit Expense forms
- Up to 500 characters
- Optional field for additional details

---

## 🔄 Sprint 6: Recurring Transactions

### Access
- Navigate to **"Recurring"** in sidebar (🔄 icon)

### Create Recurring Template
1. Click **"Add Recurring Template"**
2. Fill form:
   - **Type**: Expense or Income
   - **Name**: Template name
   - **Category/Source**: Budget category or income source
   - **Amount**: Transaction amount
   - **Pattern**: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
   - **Start Date**: When to begin generation
   - **End Date**: When to stop (optional)
   - **Description**: Additional details (optional)
3. Click **"Save Template"**

### Manage Templates
- **Toggle Active/Inactive**: Click toggle button on card
- **Edit**: Click edit icon (✏️)
- **Delete**: Click trash icon (🗑️)
- **Filter**: Use dropdowns to filter by type and status

### How It Works
- Templates run automatically **daily at 1:00 AM**
- Transactions generated based on pattern
- Active templates only
- `last_generated` timestamp updated after each run

---

## 🏷️ Sprint 6: Tags System

### Create Tags
**Method 1: Tag Manager**
1. Click **"Manage Tags"** button in expense modal
2. Click **"Create New Tag"**
3. Enter tag name
4. Select color (12 options)
5. Click **"Create"**

**Method 2: Quick Create**
1. In expense form, click **"Add Tag"**
2. Type new tag name
3. Click **"Create [name]"**
4. Tag created with random color

### Use Tags
1. In Add/Edit Expense form
2. Click **"Add Tag"** button
3. Select from existing tags or create new
4. Tags appear as colored chips
5. Remove by clicking **X** on chip

### Manage Tags
- **Edit**: Change name or color
- **Delete**: Removes from all expenses
- **Search**: Type to filter tags

### Tag Display
- Tags show as colored chips in expense table
- Color-coded for easy identification
- Multiple tags per expense supported

---

## ⚡ Sprint 6: Bulk Operations

### Select Expenses
**Method 1: Individual Selection**
- Check boxes next to expenses in table

**Method 2: Select All**
- Check box in table header
- Selects all expenses on current page

### Bulk Actions
When items selected, floating toolbar appears at bottom:

**Delete Selected**
1. Click red **"Delete Selected"** button
2. Confirm deletion
3. All selected expenses removed

**Change Category**
1. Click **"Change Category"** button
2. Select new category from dropdown
3. Click **"Update"**
4. All selected expenses updated

**Clear Selection**
- Click **X** button on toolbar
- Or uncheck "Select All" in header

---

## 🎨 UI Elements

### New Components

**ExportModal**
- Date range picker
- Category multi-select
- Format selection (CSV/Excel/PDF)
- Export summary

**RecurringTransactions Page**
- Template cards grid
- Type & status filters
- Pattern badges (color-coded)
- Active/inactive indicators
- Create/Edit modal

**TagSelector**
- Colored tag chips
- Search/filter dropdown
- Quick create option
- Remove tags easily

**TagManagementModal**
- Full CRUD interface
- 12 color options
- Tag list with edit/delete
- Empty state messaging

**BulkActionsToolbar**
- Fixed bottom position
- Animated slide-up
- Count badge
- Action buttons
- Clear selection

---

## 🗄️ Database Tables

### New Tables (V28 Migration)

**recurring_templates**
- Stores recurring transaction templates
- Fields: type, name, category, amount, pattern, dates, is_active

**tags**
- User-defined tags with colors
- Unique constraint: user_id + name

**expense_tags**
- Junction table for expense-tag relationship
- Cascade delete on both sides

**receipts**
- File attachments for expenses
- Backend ready, UI pending

### Updated Tables (V27 Migration)

**expenses**
- Added: `notes VARCHAR(500)`
- Added: tags relationship (via expense_tags)

**incomes**
- Added: `notes VARCHAR(500)`

---

## 📊 API Endpoints

### Export Endpoints
```
GET  /api/budget/export/expenses/csv
GET  /api/budget/export/expenses/excel
GET  /api/budget/export/incomes/csv
GET  /api/budget/export/incomes/excel
GET  /api/budget/export/report/pdf
POST /api/budget/export/report/email
```

### Recurring Endpoints
```
GET    /api/recurring/templates
GET    /api/recurring/templates/active
POST   /api/recurring/templates
PUT    /api/recurring/templates/{id}
DELETE /api/recurring/templates/{id}
POST   /api/recurring/templates/{id}/toggle
GET    /api/recurring/templates/{id}/upcoming
POST   /api/recurring/templates/{id}/generate
```

### Tag Endpoints
```
GET    /api/tags
GET    /api/tags/{id}
POST   /api/tags
PUT    /api/tags/{id}
DELETE /api/tags/{id}
```

### Bulk Operation Endpoints
```
DELETE /api/budget/bulk/delete
PUT    /api/budget/bulk/update-category
```

---

## 🔍 Testing Checklist

### Sprint 5
- [ ] Export expenses to CSV
- [ ] Export expenses to Excel  
- [ ] Export incomes to CSV
- [ ] Export incomes to Excel
- [ ] Generate PDF report
- [ ] Date range filtering works
- [ ] Category filtering works
- [ ] Add notes to expense
- [ ] Edit expense with notes
- [ ] Notes persist after save

### Sprint 6 - Recurring
- [ ] Create daily template
- [ ] Create weekly template
- [ ] Create monthly template
- [ ] Edit template
- [ ] Toggle active/inactive
- [ ] Delete template
- [ ] Filter by type
- [ ] Filter by status
- [ ] View upcoming dates
- [ ] Verify job runs at 1 AM

### Sprint 6 - Tags
- [ ] Create tag via manager
- [ ] Quick create tag
- [ ] Add tags to expense
- [ ] Remove tags from expense
- [ ] Edit tag name
- [ ] Change tag color
- [ ] Delete tag
- [ ] Tags display on expense list
- [ ] Multiple tags per expense

### Sprint 6 - Bulk
- [ ] Select single expense
- [ ] Select all expenses
- [ ] Toolbar appears when selected
- [ ] Bulk delete works
- [ ] Bulk category change works
- [ ] Clear selection works
- [ ] Toolbar hides after action

---

## 💡 Pro Tips

1. **Export Large Datasets**: Use date ranges to export specific periods
2. **Recurring Templates**: Start with simple patterns (MONTHLY) before complex ones
3. **Tag Organization**: Use consistent naming (e.g., "Work", "Personal", "Family")
4. **Tag Colors**: Assign similar colors to related tags
5. **Bulk Operations**: Use filters first to narrow down expenses before bulk actions
6. **Notes Field**: Add reference numbers, invoice IDs, or context details
7. **Pattern Types**:
   - DAILY: Coffee, commute
   - WEEKLY: Groceries, gym
   - MONTHLY: Rent, subscriptions
   - QUARTERLY: Insurance
   - YEARLY: Memberships

---

## 🐛 Troubleshooting

### Export Not Working
- Check browser console for errors
- Verify backend is running (port 8082)
- Check network tab in DevTools

### Recurring Job Not Running
- Job runs at 1:00 AM server time
- Check `last_generated` timestamp in database
- Verify template is `is_active = true`

### Tags Not Showing
- Refresh page after creating tags
- Check if tags were saved (check database)
- Verify expense has tags assigned

### Bulk Operations Failing
- Ensure items are selected (checkbox checked)
- Check backend logs for errors
- Verify you have permission for the operation

---

## 🎉 You're All Set!

All Sprint 5 & 6 features are now available. Explore the Budget Module and enjoy the new capabilities!

**Need Help?** Check:
- Browser console (F12)
- Backend logs (terminal running bootRun)
- Database tables (MySQL)
- TESTING_GUIDE.md for detailed instructions
