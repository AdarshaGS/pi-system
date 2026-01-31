# 🗺️ Budget Module - Implementation Roadmap

> **Created**: January 31, 2026  
> **Status**: Sprint 1 ✅ | Sprint 2 ✅ | Sprint 3 ✅ | Sprint 4 ✅ | Sprint 5-6 📋  
> **Current Module Completion**: 95%

---

## 📊 Overall Progress

```
Sprint 1: ████████████████████ 100% ✅ COMPLETE
Sprint 2: ████████████████████ 100% ✅ COMPLETE  
Sprint 3: ████████████████████ 100% ✅ COMPLETE
Sprint 4: ████████████████████ 100% ✅ COMPLETE
Sprint 5: ░░░░░░░░░░░░░░░░░░░░   0% 📋 PLANNED
Sprint 6: ░░░░░░░░░░░░░░░░░░░░   0% 📋 PLANNED

Overall Module: ███████████████████░ 95% 🟢
```

---

## ✅ SPRINT 1: Core CRUD Operations (COMPLETE)

**Duration**: Week 1 (5 days)  
**Status**: ✅ 100% COMPLETE  
**Completion Date**: January 31, 2026

### Objectives
Implement all missing CRUD operations to make the module fully functional.

### Deliverables ✅
- [x] 9 New backend REST API endpoints
- [x] 8 New service methods with authentication
- [x] 12 New frontend API client methods
- [x] Budget limit setup UI (modal with 10 categories)
- [x] Edit/Delete functionality for expenses
- [x] Edit/Delete functionality for incomes
- [x] Professional UI with icons and action buttons

### Technical Achievements
- **Backend**: Full CRUD for Expense, Income, Budget entities
- **Frontend**: Modals for Set Budget, Edit Expense, Edit Income
- **Security**: All endpoints authenticated and user-validated
- **UI/UX**: Professional action buttons with Edit/Trash icons

### Impact
- Module completeness: 32% → 65%
- Users can now fully manage their budget data
- Critical blocking issues resolved

### Files Modified
1. `src/main/java/com/budget/BudgetController.java` - 9 endpoints
2. `src/main/java/com/budget/BudgetService.java` - 8 methods
3. `frontend/src/api.js` - 12 API methods
4. `frontend/src/pages/Budget.jsx` - Set Budget + Edit/Delete UI
5. `frontend/src/pages/CashFlow.jsx` - Edit/Delete income UI

### Documentation
- ✅ [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)
- ✅ [Implementation_Status.md](Implementation_Status.md)
- ✅ [Budget_Features.md](Budget_Features.md) updated

---

## 🔄 SPRINT 2: Pagination, Filtering & Visualization (COMPLETE)

**Duration**: Week 2 (5 days)  
**Status**: ✅ 100% COMPLETE  
**Completion Date**: January 31, 2026

### Objectives
Add advanced data management features: pagination, filtering, sorting, and data visualization.

### Backend Deliverables ✅
- [x] Spring Data Pageable integration
- [x] JPA Specifications for dynamic filtering
- [x] Extended repositories with `JpaSpecificationExecutor`
- [x] Page<Expense> and Page<Income> response types
- [x] Query parameters: page, size, sortBy, order
- [x] Filter parameters: category, startDate, endDate, search
- [x] Dynamic Specification building in service layer
- [x] Default pagination: page=0, size=20, sort by date desc

### Frontend Deliverables ✅
- [x] API client updated with params support
- [x] State management for pagination and filters
- [x] Recharts library integrated
- [x] Filter UI controls section with category, date range, search
- [x] Pagination controls (Previous/Next, page indicator)
- [x] Updated fetchData() to use pagination API
- [x] Pie chart for category distribution (Recharts)
- [x] Show/Hide Filters button
- [x] Responsive filter section

### Technical Achievements
- **Backend**: Full pagination and filtering with JPA Specifications
- **Frontend**: Complete filter UI with Recharts pie chart visualization
- **Performance**: Efficient data loading for large datasets
- **UX**: Intuitive filter controls and visual data representation

### Impact
- Module completeness: 65% → 85%
- Improved performance with large datasets
- Better user experience with data exploration
- Visual insights with category breakdown chart

### Files Modified
1. ✅ `src/main/java/com/budget/BudgetController.java`
2. ✅ `src/main/java/com/budget/BudgetService.java`
3. ✅ `src/main/java/com/budget/ExpenseRepository.java`
4. ✅ `src/main/java/com/budget/IncomeRepository.java`
5. ✅ `frontend/src/api.js`
6. ✅ `frontend/src/pages/Budget.jsx`
7. ✅ `frontend/src/pages/CashFlow.jsx`

### Documentation
- ✅ [SPRINT2_COMPLETE.md](SPRINT2_COMPLETE.md)
- ✅ [Budget_Features.md](Budget_Features.md) updated

---

## ✅ SPRINT 3: Testing & Quality (COMPLETE)

**Duration**: Week 3 (5 days)  
**Status**: ✅ 100% COMPLETE  
**Completion Date**: January 31, 2026

### Objectives
Achieve production-ready quality with comprehensive testing and validation.

### Deliverables ✅
- [x] Controller integration tests for all 16 endpoints (21 tests)
- [x] Comprehensive test coverage:
  - [x] Expense CRUD operations (5 tests)
  - [x] Income CRUD operations (4 tests)
  - [x] Pagination testing (1 test)
  - [x] Filtering testing (4 tests)
  - [x] Budget limits testing (2 tests)
  - [x] Reporting & analytics (2 tests)
  - [x] Validation & error handling (3 tests)
- [x] Service layer unit tests (27 tests with Mockito)
- [x] Repository tests for custom queries (20 tests)
- [x] JPA Specification tests
- [x] Code coverage: 65-70% achieved (target: 80%)
- [x] Test documentation with @DisplayName

### Technical Achievements
**Test Suite Created:**
- BudgetControllerIntegrationTest: 21 integration tests
- BudgetServiceTest: 27 unit tests (NEW)
- ExpenseRepositoryTest: 10 repository tests (NEW)
- IncomeRepositoryTest: 10 repository tests (NEW)
- RepositoryTestConfig: Test configuration (NEW)

**Total: 68 comprehensive tests**

**Testing Patterns:**
- Arrange-Act-Assert pattern
- Mockito for dependency isolation
- @DataJpaTest for repository layer
- REST-assured for API testing
- Test ordering with @Order
- Descriptive test names with @DisplayName

### Impact
- Module completeness: 85% → 90%
- Test coverage: 15% → 65-70%
- Production-ready quality assurance
- Comprehensive API validation
- Edge case handling verified

### Files Created
1. ✅ `src/test/java/com/budget/service/BudgetServiceTest.java` (27 tests)
2. ✅ `src/test/java/com/budget/repo/ExpenseRepositoryTest.java` (10 tests)
3. ✅ `src/test/java/com/budget/repo/IncomeRepositoryTest.java` (10 tests)
4. ✅ `src/test/java/com/budget/repo/RepositoryTestConfig.java`
5. ✅ `src/test/java/com/api/budget/BudgetControllerIntegrationTest.java` (expanded to 21 tests)

### Documentation
- ✅ [SPRINT3_4_COMPLETE.md](SPRINT3_4_COMPLETE.md) - Comprehensive completion report
- ✅ Test documentation in code
- ✅ JavaDoc comments

---

## ✅ SPRINT 4: Validation & Error Handling (COMPLETE)

**Duration**: Week 4 (5 days)  
**Status**: ✅ 100% COMPLETE  
**Completion Date**: January 31, 2026 (Combined with Sprint 3)

### Objectives
Implement comprehensive validation and user-friendly error handling.

### Deliverables ✅
- [x] Custom exception classes (4 exceptions)
- [x] Global exception handler (@ControllerAdvice)
- [x] Standard error response structure
- [x] Field-specific error messages
- [x] HTTP status code mapping
- [x] Service layer exception updates
- [x] Exception handling tests

### Backend Achievements

**Custom Exceptions Created:**
1. ✅ ExpenseNotFoundException - 404 for missing expenses
2. ✅ IncomeNotFoundException - 404 for missing incomes
3. ✅ BudgetNotFoundException - 404 for missing budgets
4. ✅ InvalidBudgetException - 400 for invalid budget data

**Error Response Structure:**
```java
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
```

**Global Exception Handler:**
- @RestControllerAdvice for centralized error handling
- Handles all custom exceptions
- Handles MethodArgumentNotValidException (validation)
- Handles IllegalArgumentException
- Handles generic Exception (500 fallback)

**Service Layer Updates:**
- ✅ Updated 8 methods to use custom exceptions
- ✅ Enhanced validation logic
- ✅ Proper null checking
- ✅ Business rule validation

### Impact
- Module completeness: 90% → 95%
- User-friendly error messages
- Consistent error response format
- Proper HTTP status codes
- Reduced code duplication
- Better debugging information

### Files Created
1. ✅ `src/main/java/com/budget/exception/ExpenseNotFoundException.java`
2. ✅ `src/main/java/com/budget/exception/IncomeNotFoundException.java`
3. ✅ `src/main/java/com/budget/exception/BudgetNotFoundException.java`
4. ✅ `src/main/java/com/budget/exception/InvalidBudgetException.java`
5. ✅ `src/main/java/com/budget/exception/ErrorResponse.java`
6. ✅ `src/main/java/com/budget/exception/BudgetExceptionHandler.java`

### Files Modified
1. ✅ `src/main/java/com/budget/service/BudgetService.java` - Updated with custom exceptions

### Documentation
- ✅ [SPRINT3_4_COMPLETE.md](SPRINT3_4_COMPLETE.md) - Combined sprint report
- ✅ JavaDoc comments for all exception classes
- ✅ Error handling documentation

### Scope
**Controller Tests** (30 tests):
- 16 endpoint tests (1 happy path each)
- 10 validation error tests
- 4 authentication failure tests

**Service Tests** (25 tests):
- Test all 14 service methods
- Mock repository responses
- Test authentication validation
- Test business logic edge cases

**Repository Tests** (10 tests):
- Test custom query methods
- Test JPA Specifications
- Test date range filtering
- Test sorting and pagination

**Estimated Effort**: 15-20 hours

---

## 📋 SPRINT 4: Validation & Error Handling (PLANNED)

**Duration**: Week 4 (5 days)  
**Status**: 📋 PLANNED  
**Priority**: MEDIUM

### Objectives
Implement comprehensive validation and user-friendly error handling.

### Deliverables (Planned)
- [ ] Backend validation annotations (@Min, @Max, @NotNull, etc.)
- [ ] Custom exceptions (ExpenseNotFoundException, InvalidBudgetException)
- [ ] Global exception handler (@ControllerAdvice)
- [ ] Field-specific error messages
- [ ] Frontend form validation
- [ ] Toast notifications (replace alert())
- [ ] Inline error messages
- [ ] Error boundary component
- [ ] Loading states and spinners

### Backend Tasks
**Validation Layer**:
```java
@Min(value = 0, message = "Amount must be positive")
@NotNull(message = "Category is required")
private BigDecimal amount;

@PostMapping("/expense")
public ResponseEntity<?> addExpense(@Valid @RequestBody Expense expense) {
    // Validation errors automatically caught
}
```

**Custom Exceptions**:
```java
public class ExpenseNotFoundException extends RuntimeException { }
public class InvalidBudgetException extends RuntimeException { }
public class UnauthorizedAccessException extends RuntimeException { }
```

**Exception Handler**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Frontend Tasks
**Toast Notifications**:
- Install react-hot-toast or similar
- Replace all `alert()` calls
- Success toasts (green)
- Error toasts (red)
- Info toasts (blue)

**Form Validation**:
- Min/max amount validation
- Required field validation
- Date format validation
- Pattern validation (e.g., description length)
- Real-time validation feedback

**Estimated Effort**: 8-10 hours

---

## 📋 SPRINT 5: Export & Reports (PLANNED)

**Duration**: Week 5 (5-7 days)  
**Status**: 📋 PLANNED  
**Priority**: MEDIUM

### Objectives
Enable data export and report generation with full UI implementation.

### Backend Deliverables (Planned)
- [ ] CSV export endpoint for expenses
- [ ] CSV export endpoint for incomes  
- [ ] CSV export endpoint for budgets
- [ ] PDF monthly report generation endpoint
- [ ] Excel export endpoint with formatting
- [ ] Email report service (optional)
- [ ] Export service layer with file generation
- [ ] Date range and filter support in exports
- [ ] Proper file naming convention

### Frontend Deliverables (Planned)
**Budget.jsx Enhancements:**
- [ ] Export button in header (next to "Add Expense")
- [ ] Export dropdown menu with options:
  - Export Expenses (CSV)
  - Export Expenses (Excel)
  - Download Monthly Report (PDF)
  - Email Report
- [ ] Export modal with options:
  - Date range selector (Start Date, End Date)
  - Category filter (All / Specific categories)
  - Format selection (CSV, Excel, PDF)
  - Include/exclude filters toggle
- [ ] Download progress indicator
- [ ] Success/error toast notifications
- [ ] File download handling (browser download)

**CashFlow.jsx Enhancements:**
- [ ] Export button in header
- [ ] Export dropdown for income data:
  - Export Income (CSV)
  - Export Income (Excel)
  - Cash Flow Report (PDF)
- [ ] Same export modal structure
- [ ] Combined Income + Expense export option

**New Components:**
- [ ] ExportModal.jsx
  - Reusable export configuration modal
  - Date range pickers
  - Filter checkboxes
  - Format radio buttons
  - Preview button
  - Export button with loading state
- [ ] ReportPreview.jsx (optional)
  - Preview report before download
  - PDF preview in modal
  - Print button

### Implementation Plan

**Backend Endpoints**:
```java
// Export Endpoints
@GetMapping("/expense/{userId}/export/csv")
public ResponseEntity<byte[]> exportExpensesCSV(
    @PathVariable Long userId,
    @RequestParam(required = false) LocalDate startDate,
    @RequestParam(required = false) LocalDate endDate,
    @RequestParam(required = false) String category)

@GetMapping("/expense/{userId}/export/excel")
public ResponseEntity<byte[]> exportExpensesExcel(...)

@GetMapping("/income/{userId}/export/csv")
public ResponseEntity<byte[]> exportIncomesCSV(...)

@GetMapping("/report/{userId}/pdf")
public ResponseEntity<byte[]> generatePDFReport(
    @PathVariable Long userId,
    @RequestParam(required = false) String monthYear)

@PostMapping("/report/{userId}/email")
public ResponseEntity<?> emailReport(
    @PathVariable Long userId,
    @RequestBody EmailReportRequest request)
```

**Libraries to Add**:
```xml
<!-- Backend -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
</dependency>
```

**Frontend API Updates**:
```javascript
// frontend/src/api.js additions
export const budgetApi = {
    // ... existing methods
    
    // Export methods
    exportExpensesCSV: (userId, token, params = {}) => 
        apiCall(`/v1/budget/expense/${userId}/export/csv?${new URLSearchParams(params)}`, 'GET', null, token, 'blob'),
    
    exportExpensesExcel: (userId, token, params = {}) => 
        apiCall(`/v1/budget/expense/${userId}/export/excel?${new URLSearchParams(params)}`, 'GET', null, token, 'blob'),
    
    exportIncomesCSV: (userId, token, params = {}) => 
        apiCall(`/v1/budget/income/${userId}/export/csv?${new URLSearchParams(params)}`, 'GET', null, token, 'blob'),
    
    exportIncomesExcel: (userId, token, params = {}) => 
        apiCall(`/v1/budget/income/${userId}/export/excel?${new URLSearchParams(params)}`, 'GET', null, token, 'blob'),
    
    downloadPDFReport: (userId, token, monthYear) => 
        apiCall(`/v1/budget/report/${userId}/pdf?monthYear=${monthYear}`, 'GET', null, token, 'blob'),
    
    emailReport: (userId, token, data) => 
        apiCall(`/v1/budget/report/${userId}/email`, 'POST', data, token)
};
```

**UI Components Structure**:
```jsx
// Budget.jsx header update
<div className="header">
    <h1>Budget Tracker</h1>
    <div className="header-actions">
        <button onClick={() => setShowExportModal(true)}>
            <Download /> Export
        </button>
        <button onClick={() => setShowAddModal(true)}>
            <Plus /> Add Expense
        </button>
    </div>
</div>

// ExportModal.jsx structure
<Modal show={showExportModal} onClose={() => setShowExportModal(false)}>
    <h2>Export Data</h2>
    
    <div className="export-type">
        <label>Export Type:</label>
        <select value={exportType} onChange={(e) => setExportType(e.target.value)}>
            <option value="expenses">Expenses</option>
            <option value="income">Income</option>
            <option value="combined">Combined Report</option>
        </select>
    </div>
    
    <div className="date-range">
        <input type="date" value={startDate} onChange={...} placeholder="Start Date" />
        <input type="date" value={endDate} onChange={...} placeholder="End Date" />
    </div>
    
    <div className="category-filter">
        <label>Categories:</label>
        <MultiSelect options={categories} selected={selectedCategories} onChange={...} />
    </div>
    
    <div className="format-selection">
        <label>Format:</label>
        <div className="radio-group">
            <input type="radio" id="csv" value="csv" checked={format === 'csv'} />
            <label htmlFor="csv">CSV</label>
            
            <input type="radio" id="excel" value="excel" checked={format === 'excel'} />
            <label htmlFor="excel">Excel</label>
            
            <input type="radio" id="pdf" value="pdf" checked={format === 'pdf'} />
            <label htmlFor="pdf">PDF</label>
        </div>
    </div>
    
    <div className="modal-actions">
        <button onClick={handleExport} disabled={loading}>
            {loading ? 'Exporting...' : 'Export'}
        </button>
        <button onClick={() => setShowExportModal(false)}>Cancel</button>
    </div>
</Modal>
```

### File Structure
**New Files to Create**:
1. `src/main/java/com/budget/service/ExportService.java` - Export logic
2. `src/main/java/com/budget/service/ReportGenerationService.java` - PDF generation
3. `src/main/java/com/budget/dto/EmailReportRequest.java` - Email request DTO
4. `frontend/src/components/ExportModal.jsx` - Export modal component
5. `frontend/src/components/MultiSelect.jsx` - Multi-select dropdown
6. `frontend/src/utils/fileDownload.js` - File download utility

**Files to Modify**:
1. `src/main/java/com/budget/controller/BudgetController.java` - Add export endpoints
2. `frontend/src/pages/Budget.jsx` - Add export button and modal
3. `frontend/src/pages/CashFlow.jsx` - Add export functionality
4. `frontend/src/api.js` - Add export API methods

### Testing Requirements
- [ ] Test CSV file generation and download
- [ ] Test Excel file with proper formatting
- [ ] Test PDF report generation
- [ ] Test export with date range filters
- [ ] Test export with category filters
- [ ] Test file download in browser
- [ ] Test error handling for large exports
- [ ] Test email report delivery (if implemented)

**Estimated Effort**: 
- Backend: 6-8 hours
- Frontend: 6-8 hours
- Testing: 2-3 hours
- **Total: 14-19 hours**

---

## 📋 SPRINT 6: Advanced Features (PLANNED)

**Duration**: Week 6 (7-10 days)  
**Status**: 📋 PLANNED  
**Priority**: LOW

### Objectives
Implement advanced budget management features with comprehensive UI.

### Backend Deliverables (Planned)
**Recurring Transactions:**
- [ ] RecurringTemplate entity with JPA mapping
- [ ] RecurrencePattern enum (DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY)
- [ ] RecurringTemplateRepository with custom queries
- [ ] RecurringService with CRUD operations
- [ ] Scheduled job for auto-generation (@Scheduled)
- [ ] REST endpoints for template management
- [ ] Skip/pause logic for specific occurrences
- [ ] Transaction history tracking

**Tags System:**
- [ ] Tag entity with color support
- [ ] ManyToMany relationship with Expense/Income
- [ ] TagRepository and TagService
- [ ] Tag CRUD endpoints
- [ ] Tag assignment/removal endpoints
- [ ] Tag-based filtering in expense queries

**Attachments:**
- [ ] Receipt entity with file metadata
- [ ] File upload service (local or S3)
- [ ] File size validation (max 5MB)
- [ ] Supported formats: JPG, PNG, PDF
- [ ] Attachment endpoints (upload, download, delete)
- [ ] Association with expenses

**Bulk Operations:**
- [ ] Bulk delete expenses endpoint
- [ ] Bulk update category endpoint
- [ ] Bulk tag assignment endpoint
- [ ] Transaction support for atomicity

### Frontend Deliverables (Planned)

**1. Recurring Transactions UI**

**New Page: RecurringTransactions.jsx**
- [ ] Header with "Add Recurring Transaction" button
- [ ] List of all recurring templates with cards:
  - Template name
  - Amount and category
  - Frequency badge (Monthly, Weekly, etc.)
  - Active/Inactive toggle
  - Next occurrence date
  - Edit and Delete buttons
- [ ] Filters: Type (Expense/Income), Status (Active/Inactive)
- [ ] Calendar view showing upcoming auto-generated transactions

**Add/Edit Recurring Modal:**
```jsx
<Modal title="Add Recurring Transaction">
    <div className="form-group">
        <label>Type</label>
        <select>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
        </select>
    </div>
    
    <div className="form-group">
        <label>Name</label>
        <input type="text" placeholder="e.g., Monthly Rent" />
    </div>
    
    <div className="form-group">
        <label>Category</label>
        <select>{/* categories */}</select>
    </div>
    
    <div className="form-group">
        <label>Amount</label>
        <input type="number" />
    </div>
    
    <div className="form-group">
        <label>Frequency</label>
        <select>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
            <option value="QUARTERLY">Quarterly</option>
            <option value="YEARLY">Yearly</option>
        </select>
    </div>
    
    <div className="form-group">
        <label>Start Date</label>
        <input type="date" />
    </div>
    
    <div className="form-group">
        <label>End Date (Optional)</label>
        <input type="date" />
    </div>
    
    <div className="form-group">
        <label>
            <input type="checkbox" />
            Active
        </label>
    </div>
</Modal>
```

**Budget.jsx Updates:**
- [ ] "Recurring" badge on expenses from templates
- [ ] "View Recurring Transactions" link in menu
- [ ] Auto-refresh when scheduled job runs

**2. Tags System UI**

**Tag Management Modal:**
- [ ] "Manage Tags" button in Budget.jsx header
- [ ] Tag list with color indicators
- [ ] Add new tag form (name + color picker)
- [ ] Edit/Delete tag actions
- [ ] Tag usage count display

**Tag Assignment:**
- [ ] Tag selector in Add/Edit Expense modal
- [ ] Multi-select dropdown with color badges
- [ ] Quick tag buttons in expense list
- [ ] Tag filter in expense list
- [ ] Tag-based grouping view

**Tag Component:**
```jsx
<div className="tags-section">
    <label>Tags</label>
    <div className="tag-selector">
        {availableTags.map(tag => (
            <span 
                key={tag.id} 
                className={`tag ${selectedTags.includes(tag.id) ? 'selected' : ''}`}
                style={{ backgroundColor: tag.color }}
                onClick={() => toggleTag(tag.id)}
            >
                {tag.name}
            </span>
        ))}
        <button onClick={() => setShowAddTagModal(true)}>
            + New Tag
        </button>
    </div>
</div>
```

**3. Receipt Attachments UI**

**File Upload in Add/Edit Expense Modal:**
- [ ] "Attach Receipt" button with upload icon
- [ ] Drag-and-drop file upload zone
- [ ] File preview (thumbnail for images, icon for PDF)
- [ ] File size indicator
- [ ] Remove attachment button
- [ ] Multiple file support (up to 3 files)

**Attachment Display:**
- [ ] Receipt icon next to expenses with attachments
- [ ] Click to view/download attachment
- [ ] Lightbox modal for image preview
- [ ] PDF viewer modal or download button
- [ ] Attachment count badge

**AttachmentUpload Component:**
```jsx
<div className="attachment-upload">
    <div 
        className="dropzone"
        onDrop={handleDrop}
        onDragOver={handleDragOver}
    >
        <Upload size={32} />
        <p>Drag & drop receipt or click to upload</p>
        <p className="hint">JPG, PNG, PDF (max 5MB)</p>
        <input 
            type="file" 
            ref={fileInputRef}
            accept=".jpg,.jpeg,.png,.pdf"
            multiple
            onChange={handleFileSelect}
        />
    </div>
    
    {attachments.length > 0 && (
        <div className="attachment-list">
            {attachments.map(file => (
                <div key={file.id} className="attachment-item">
                    <img src={getPreview(file)} alt={file.name} />
                    <span>{file.name}</span>
                    <button onClick={() => removeFile(file.id)}>×</button>
                </div>
            ))}
        </div>
    )}
</div>
```

**4. Bulk Operations UI**

**Expense List Enhancements:**
- [ ] Checkbox for each expense row
- [ ] "Select All" checkbox in header
- [ ] Bulk actions toolbar (appears when items selected):
  - Delete Selected
  - Change Category
  - Add Tags
  - Export Selected
- [ ] Confirmation modal for bulk delete
- [ ] Progress indicator for bulk operations

**Bulk Actions Toolbar:**
```jsx
{selectedExpenses.length > 0 && (
    <div className="bulk-actions-toolbar">
        <span>{selectedExpenses.length} selected</span>
        <button onClick={handleBulkDelete}>
            <Trash /> Delete
        </button>
        <button onClick={() => setShowBulkCategoryModal(true)}>
            <Edit /> Change Category
        </button>
        <button onClick={() => setShowBulkTagModal(true)}>
            <Tag /> Add Tags
        </button>
        <button onClick={handleBulkExport}>
            <Download /> Export
        </button>
        <button onClick={clearSelection}>
            Clear Selection
        </button>
    </div>
)}
```

**5. Transaction Notes UI**

**Notes in Add/Edit Expense Modal:**
- [ ] "Notes" textarea field (optional)
- [ ] Character counter (max 500 chars)
- [ ] Rich text formatting (optional: bold, italic, lists)
- [ ] Auto-save draft functionality

**Notes Display:**
- [ ] Notes icon/badge on expenses with notes
- [ ] Expandable notes section in expense list
- [ ] Notes preview (first 50 chars) in list
- [ ] Full notes in detail view modal

### Backend Implementation

**Recurring Transactions Backend**:
```java
// Entity
@Entity
@Table(name = "recurring_templates")
public class RecurringTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private TransactionType type; // EXPENSE or INCOME
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
    
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private RecurrencePattern pattern; // DAILY, WEEKLY, MONTHLY, etc.
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDate lastGenerated;
}

// Service with Scheduled Job
@Service
public class RecurringTransactionService {
    
    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
    public void generateRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTemplate> activeTemplates = 
            recurringTemplateRepository.findByIsActiveTrue();
        
        for (RecurringTemplate template : activeTemplates) {
            if (shouldGenerate(template, today)) {
                createTransaction(template);
                updateLastGenerated(template, today);
            }
        }
    }
    
    private boolean shouldGenerate(RecurringTemplate template, LocalDate date) {
        // Logic based on pattern and lastGenerated
    }
}

// Controller
@RestController
@RequestMapping("/api/v1/budget/recurring")
public class RecurringTransactionController {
    
    @PostMapping
    public RecurringTemplate create(@RequestBody RecurringTemplate template) { }
    
    @GetMapping("/{userId}")
    public List<RecurringTemplate> getUserTemplates(@PathVariable Long userId) { }
    
    @PutMapping("/{id}")
    public RecurringTemplate update(@PathVariable Long id, @RequestBody RecurringTemplate template) { }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { }
    
    @PostMapping("/{id}/toggle")
    public RecurringTemplate toggleActive(@PathVariable Long id) { }
    
    @GetMapping("/{id}/upcoming")
    public List<LocalDate> getUpcomingDates(@PathVariable Long id, @RequestParam int months) { }
}
```

**Tags Backend**:
```java
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private String name;
    private String color; // Hex color code
}

// In Expense entity
@ManyToMany
@JoinTable(
    name = "expense_tags",
    joinColumns = @JoinColumn(name = "expense_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private Set<Tag> tags = new HashSet<>();
```

**Attachments Backend**:
```java
@Entity
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long expenseId;
    private String fileName;
    private String originalFileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}

@PostMapping("/expense/{expenseId}/attachment")
public Receipt uploadAttachment(
    @PathVariable Long expenseId,
    @RequestParam("file") MultipartFile file) {
    // File upload logic
}
```

### File Structure
**New Backend Files**:
1. `src/main/java/com/budget/data/RecurringTemplate.java`
2. `src/main/java/com/budget/data/RecurrencePattern.java` (enum)
3. `src/main/java/com/budget/data/TransactionType.java` (enum)
4. `src/main/java/com/budget/data/Tag.java`
5. `src/main/java/com/budget/data/Receipt.java`
6. `src/main/java/com/budget/repo/RecurringTemplateRepository.java`
7. `src/main/java/com/budget/repo/TagRepository.java`
8. `src/main/java/com/budget/repo/ReceiptRepository.java`
9. `src/main/java/com/budget/service/RecurringTransactionService.java`
10. `src/main/java/com/budget/service/TagService.java`
11. `src/main/java/com/budget/service/FileStorageService.java`
12. `src/main/java/com/budget/controller/RecurringTransactionController.java`
13. `src/main/java/com/budget/controller/TagController.java`
14. `src/main/resources/db/migration/V27__Create_Recurring_Templates.sql`
15. `src/main/resources/db/migration/V28__Create_Tags_And_Receipts.sql`

**New Frontend Files**:
1. `frontend/src/pages/RecurringTransactions.jsx`
2. `frontend/src/components/RecurringModal.jsx`
3. `frontend/src/components/TagSelector.jsx`
4. `frontend/src/components/TagManagementModal.jsx`
5. `frontend/src/components/AttachmentUpload.jsx`
6. `frontend/src/components/BulkActionsToolbar.jsx`
7. `frontend/src/components/NotesEditor.jsx`
8. `frontend/src/utils/fileUpload.js`

**Modified Files**:
1. `frontend/src/pages/Budget.jsx` - Recurring, tags, bulk actions
2. `frontend/src/pages/CashFlow.jsx` - Same features
3. `frontend/src/api.js` - New API methods
4. `frontend/src/App.jsx` - Add recurring transactions route

### Testing Requirements
- [ ] Test recurring template CRUD
- [ ] Test scheduled job execution
- [ ] Test pattern calculations (weekly, monthly, etc.)
- [ ] Test tag CRUD and assignment
- [ ] Test file upload (size, format validation)
- [ ] Test file download and preview
- [ ] Test bulk delete with rollback
- [ ] Test bulk category update
- [ ] Test notes save and display

**Estimated Effort**: 
- Backend recurring: 8-10 hours
- Backend tags: 3-4 hours
- Backend attachments: 4-5 hours
- Backend bulk ops: 2-3 hours
- Frontend recurring UI: 8-10 hours
- Frontend tags UI: 4-5 hours
- Frontend attachments UI: 4-5 hours
- Frontend bulk ops UI: 3-4 hours
- Testing: 4-5 hours
- **Total: 40-55 hours (7-10 days)**

---

## 🎯 Milestone Targets

### By End of Sprint 2 (Current)
- ✅ 16 REST API endpoints
- ✅ Full CRUD operations
- ✅ Pagination & filtering backend
- ⏳ Data visualizations
- **Target**: 85% complete

### By End of Sprint 3
- ✅ 80%+ test coverage
- ✅ Comprehensive validation
- ✅ Production-ready quality
- **Target**: 90% complete

### By End of Sprint 4
- ✅ User-friendly error handling
- ✅ Toast notifications
- ✅ Professional UX
- **Target**: 95% complete

### By End of Sprint 5
- ✅ Export functionality
- ✅ PDF reports
- ✅ Data portability
- **Target**: 98% complete

### By End of Sprint 6
- ✅ Recurring transactions
- ✅ Tags and attachments
- ✅ Feature-rich module
- **Target**: 100% complete

---

## 🚀 Future Enhancements (Post Sprint 6)

### Phase 2: Intelligence
- AI-powered budget recommendations
- Anomaly detection (unusual spending)
- Predictive analytics (forecast spending)
- Smart categorization (auto-categorize transactions)

### Phase 3: Integration
- Bank account integration (Plaid API)
- Credit card transaction import
- Investment portfolio integration
- Tax calculation integration

### Phase 4: Collaboration
- Shared budgets (household/team)
- Budget approval workflows
- Multi-user collaboration
- Comments and discussions

### Phase 5: Mobile
- React Native mobile app
- Offline support
- Push notifications
- Camera receipt scanning

---

## 📈 Success Metrics

### Technical Metrics
- **API Response Time**: < 200ms (target)
- **Test Coverage**: 80%+ (target: 90%)
- **Code Quality**: SonarQube A rating
- **Bug Count**: < 5 critical bugs

### Business Metrics
- **Feature Adoption**: > 80% users set budgets
- **Daily Active Users**: Track engagement
- **Data Entry Rate**: Transactions per user/month
- **User Satisfaction**: > 4.5/5 stars

### Performance Metrics
- **Page Load Time**: < 2 seconds
- **API Availability**: 99.9% uptime
- **Database Query Time**: < 100ms
- **Concurrent Users**: Support 1000+

---

## 🛠️ Technical Debt

### Current Debt
1. **Testing**: Only ~15% coverage (need 80%+)
2. **Validation**: Basic validation, need comprehensive
3. **Error Handling**: Generic exceptions, need custom
4. **UI/UX**: Using alert(), need toast notifications
5. **Performance**: No query optimization yet

### Debt Resolution Plan
- Sprint 3: Address testing debt
- Sprint 4: Address validation & error handling
- Sprint 5: No new debt introduced
- Sprint 6: Performance optimization

---

## 📚 Documentation Status

### Current Documentation ✅
- [x] [Budget_Features.md](Budget_Features.md) - Comprehensive analysis
- [x] [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md) - Sprint 1 details
- [x] [Implementation_Status.md](Implementation_Status.md) - Current status
- [x] [SPRINT2_STATUS.md](SPRINT2_STATUS.md) - Sprint 2 guide
- [x] [Budget_Roadmap.md](Budget_Roadmap.md) - This file

### Planned Documentation
- [ ] API_REFERENCE.md - Complete API documentation
- [ ] USER_GUIDE.md - End-user documentation
- [ ] TESTING_GUIDE.md - Testing procedures
- [ ] DEPLOYMENT_GUIDE.md - Deployment instructions

---

## 👥 Team & Responsibilities

### Backend Development
- API implementation
- Service layer logic
- Database optimization
- Security implementation

### Frontend Development
- UI component development
- State management
- API integration
- Responsive design

### QA & Testing
- Test case development
- Integration testing
- Performance testing
- Bug tracking

### DevOps
- CI/CD pipeline
- Deployment automation
- Monitoring setup
- Performance optimization

---

## 📞 Support & Maintenance

### Issue Tracking
- GitHub Issues for bugs
- Feature requests in backlog
- Priority labels: P0 (critical) to P3 (low)

### Release Process
- Sprint releases every week
- Hotfix releases as needed
- Semantic versioning (v1.0.0, v1.1.0, v2.0.0)

### Monitoring
- Application logs (Logback)
- Error tracking (Sentry or similar)
- Performance monitoring (New Relic or similar)
- User analytics (Google Analytics)

---

## 🎉 Summary

The Budget Module has made **exceptional progress**:
- **Sprint 1**: 32% → 65% (CRUD operations)
- **Sprint 2**: 65% → 75% (Pagination & filtering backend)
- **Target**: 100% feature-complete by Sprint 6

**Current Status**: Production-ready for basic usage. Advanced features and polish in remaining sprints.

**Next Immediate Action**: Complete Sprint 2 frontend (filters, pagination, charts) - **2-3 hours remaining**

---

*Roadmap maintained by: PI System Development Team*  
*Last Updated: January 31, 2026*  
*Next Review: After Sprint 2 completion*
