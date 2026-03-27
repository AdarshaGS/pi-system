# Development Standards & Guidelines

**Last Updated**: February 1, 2026  
**Status**: Active  
**Scope**: All Backend & Frontend Development

---

## 📋 Table of Contents

1. [Code Structure](#code-structure)
2. [Naming Conventions](#naming-conventions)
3. [API Design Standards](#api-design-standards)
4. [Database Guidelines](#database-guidelines)
5. [Security Standards](#security-standards)
6. [Error Handling](#error-handling)
7. [Testing Requirements](#testing-requirements)
8. [Documentation Requirements](#documentation-requirements)

---

## Code Structure

### Backend (Spring Boot)

#### Package Organization
```
com.pifinance.
├── config/          - Configuration classes
├── controller/      - REST endpoints
├── service/         - Business logic
├── repo/            - Data access (repositories)
├── data/            - Entity classes
├── dto/             - Request/Response objects
├── exception/       - Custom exceptions
├── security/        - Security configuration
└── util/            - Utility classes
```

#### Module Organization
Each module should follow this structure:
```
module_name/
├── controller/
│   └── ModuleController.java
├── service/
│   ├── ModuleService.java
│   └── ModuleServiceImpl.java
├── repo/
│   └── ModuleRepository.java
├── data/
│   └── ModuleEntity.java
└── dto/
    ├── ModuleRequest.java
    └── ModuleResponse.java
```

### Frontend (React)

#### Directory Structure
```
src/
├── api/             - API client functions
├── components/      - Reusable components
├── layouts/         - Page layouts
├── pages/           - Full page components
├── utils/           - Utility functions
├── assets/          - Images, icons, fonts
└── App.jsx          - Main application
```

---

## Naming Conventions

### Java (Backend)

#### Classes
- **PascalCase** for class names
- Descriptive, singular nouns
```java
// ✅ Good
public class BudgetController
public class ExpenseService
public class AlertRepository

// ❌ Bad
public class budgetController
public class expenseserviceimpl
public class alerts_repo
```

#### Methods
- **camelCase** for method names
- Start with verb (get, create, update, delete, check, calculate, etc.)
```java
// ✅ Good
public void createBudget()
public List<Alert> getUnreadAlerts()
public boolean checkThresholdExceeded()

// ❌ Bad
public void CreateBudget()
public List<Alert> UnreadAlerts()
public boolean ThresholdExceeded()
```

#### Variables
- **camelCase** for variables
- Descriptive names, avoid abbreviations
```java
// ✅ Good
private BigDecimal monthlyLimit;
private String categoryName;
private LocalDate expenseDate;

// ❌ Bad
private BigDecimal ml;
private String cat;
private LocalDate ed;
```

#### Constants
- **UPPER_SNAKE_CASE** for constants
```java
// ✅ Good
public static final String DEFAULT_CURRENCY = "INR";
public static final int MAX_PAGE_SIZE = 100;

// ❌ Bad
public static final String defaultCurrency = "INR";
public static final int maxPageSize = 100;
```

### JavaScript/React (Frontend)

#### Components
- **PascalCase** for component names
```javascript
// ✅ Good
export default function BudgetCard() {}
export default function AlertList() {}

// ❌ Bad
export default function budgetCard() {}
export default function alert_list() {}
```

#### Functions/Variables
- **camelCase** for functions and variables
```javascript
// ✅ Good
const calculateTotal = () => {};
const userId = getCurrentUserId();

// ❌ Bad
const CalculateTotal = () => {};
const user_id = getCurrentUserId();
```

#### Constants
- **UPPER_SNAKE_CASE** for constants
```javascript
// ✅ Good
const API_BASE_URL = "http://localhost:8080/api/v1";
const MAX_RETRIES = 3;

// ❌ Bad
const apiBaseUrl = "http://localhost:8080/api/v1";
const maxRetries = 3;
```

---

## API Design Standards

### RESTful Principles

#### Endpoint Naming
- Use **plural nouns** for resources
- Use **kebab-case** for multi-word resources
- Use **path parameters** for IDs
- Use **query parameters** for filters

```
✅ Good Examples:
GET    /api/v1/budgets
POST   /api/v1/budgets
GET    /api/v1/budgets/{id}
PUT    /api/v1/budgets/{id}
DELETE /api/v1/budgets/{id}
GET    /api/v1/budgets?userId={id}&monthYear={yyyy-MM}
GET    /api/v1/recurring-transactions

❌ Bad Examples:
GET    /api/v1/getBudget
POST   /api/v1/createBudget
GET    /api/v1/budget/{id}  (singular)
GET    /api/v1/recurringTransactions  (camelCase)
```

#### HTTP Methods
| Method | Usage | Example |
|--------|-------|---------|
| GET | Retrieve resource(s) | `GET /budgets` |
| POST | Create new resource | `POST /budgets` |
| PUT | Update entire resource | `PUT /budgets/{id}` |
| PATCH | Update partial resource | `PATCH /budgets/{id}` |
| DELETE | Delete resource | `DELETE /budgets/{id}` |

#### Status Codes
Use appropriate HTTP status codes:
```
200 OK          - Successful GET/PUT
201 Created     - Successful POST
204 No Content  - Successful DELETE
400 Bad Request - Invalid input
401 Unauthorized - Missing/invalid JWT
403 Forbidden   - Access denied
404 Not Found   - Resource doesn't exist
500 Internal Server Error - Server error
```

#### Response Structure
Always return consistent response format:

**Success Response**:
```json
{
  "data": {
    "id": 123,
    "name": "Budget"
  },
  "message": "Budget created successfully",
  "timestamp": "2026-02-01T10:00:00Z"
}
```

**Error Response**:
```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Budget with ID 123 not found",
  "timestamp": "2026-02-01T10:00:00Z",
  "path": "/api/v1/budgets/123"
}
```

#### Pagination
Use consistent pagination parameters:
```
GET /api/v1/expenses?page=0&size=20&sort=expenseDate,desc
```

Response with pagination metadata:
```json
{
  "content": [ /* array of items */ ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true }
  },
  "totalPages": 5,
  "totalElements": 98,
  "last": false,
  "first": true
}
```

#### Filtering
Use query parameters for filtering:
```
GET /api/v1/expenses?category=FOOD&startDate=2026-01-01&endDate=2026-01-31
```

---

## Database Guidelines

### Table Naming
- Use **lowercase** with **underscores**
- Use **plural** nouns
```sql
-- ✅ Good
CREATE TABLE budgets
CREATE TABLE recurring_templates
CREATE TABLE custom_categories

-- ❌ Bad
CREATE TABLE Budget
CREATE TABLE recurringTemplate
CREATE TABLE customcategory
```

### Column Naming
- Use **lowercase** with **underscores**
- Use descriptive names
```sql
-- ✅ Good
user_id
monthly_limit
created_at
is_active

-- ❌ Bad
userId
ml
createdat
active
```

### Primary Keys
- Always use `id` as primary key
- Use `BIGINT AUTO_INCREMENT`
```sql
id BIGINT AUTO_INCREMENT PRIMARY KEY
```

### Foreign Keys
- Name foreign keys with `_id` suffix
- Add proper constraints
```sql
user_id BIGINT NOT NULL,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
```

### Indexes
- Add indexes on foreign keys
- Add indexes on frequently queried columns
- Use descriptive index names
```sql
-- ✅ Good
INDEX idx_user_alerts (user_id, created_at DESC)
INDEX idx_active_next_run (is_active, next_run_date)

-- ❌ Bad
INDEX idx1 (user_id)
INDEX index2 (is_active)
```

### Migrations (Flyway)
- Use version format: `V{number}__{description}.sql`
- Never modify existing migrations
- Always test migrations in development first
```
V1__Initial_Schema.sql
V2__Add_Budgets_Table.sql
V30__Create_Alerts_Table.sql
```

---

## Security Standards

### Authentication
- All API endpoints require JWT token (except login/register)
- Use `@PreAuthorize` for role-based access
```java
@PreAuthorize("hasRole('USER')")
@GetMapping("/api/v1/budgets")
public ResponseEntity<List<Budget>> getBudgets() {}
```

### Authorization
- Always validate user access to resources
- Check userId in request matches authenticated user
```java
// ✅ Good
if (!budget.getUserId().equals(authenticatedUserId)) {
    throw new UnauthorizedException("Access denied");
}

// ❌ Bad
// No validation - security risk!
```

### Input Validation
- Use `@Valid` annotation for request bodies
- Validate all user inputs
```java
@PostMapping("/api/v1/budgets")
public ResponseEntity<Budget> createBudget(
    @Valid @RequestBody BudgetRequest request) {
    // ...
}
```

### SQL Injection Prevention
- Always use JPA/Hibernate or prepared statements
- Never concatenate user input into queries
```java
// ✅ Good
@Query("SELECT e FROM Expense e WHERE e.userId = :userId")
List<Expense> findByUserId(@Param("userId") Long userId);

// ❌ Bad
String query = "SELECT * FROM expenses WHERE user_id = " + userId;
```

---

## Error Handling

### Exception Hierarchy
```
RuntimeException
├── ResourceNotFoundException (404)
├── UnauthorizedException (401)
├── BadRequestException (400)
└── InternalServerException (500)
```

### Custom Exceptions
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s with ID %d not found", resource, id));
    }
}
```

### Global Exception Handler
Use `@ControllerAdvice` for centralized error handling:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Logging
- Use SLF4J logger
- Log at appropriate levels
```java
private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

log.debug("Fetching budget with ID: {}", id);
log.info("Budget created successfully: {}", budget.getId());
log.warn("Budget limit exceeded for category: {}", category);
log.error("Failed to create budget", exception);
```

---

## Testing Requirements

### Test Coverage Targets
- **Unit Tests**: 70% minimum coverage
- **Integration Tests**: All critical paths
- **End-to-End Tests**: Major user flows

### Unit Tests
```java
@SpringBootTest
class BudgetServiceTest {
    
    @Mock
    private BudgetRepository budgetRepository;
    
    @InjectMocks
    private BudgetServiceImpl budgetService;
    
    @Test
    void shouldCreateBudget() {
        // Given
        Budget budget = new Budget();
        
        // When
        when(budgetRepository.save(any())).thenReturn(budget);
        Budget result = budgetService.createBudget(budget);
        
        // Then
        assertNotNull(result);
        verify(budgetRepository).save(budget);
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class BudgetControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldGetBudgets() throws Exception {
        mockMvc.perform(get("/api/v1/budgets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

### Test Data
- Use `@Sql` for test data setup
- Clean up after tests
```java
@Sql("/data.sql")
@Test
void shouldFindBudget() {
    // Test using data from data.sql
}
```

---

## Documentation Requirements

### Code Comments
- Use JavaDoc for public methods
- Explain **why**, not **what**
```java
/**
 * Calculates budget variance for a specific month.
 * 
 * Uses total spending vs budget limit to determine
 * if user is over/under budget. Returns detailed
 * breakdown by category.
 * 
 * @param userId User ID to analyze
 * @param monthYear Month in format "YYYY-MM"
 * @return BudgetVarianceAnalysis with metrics
 * @throws ResourceNotFoundException if user not found
 */
public BudgetVarianceAnalysis calculateVariance(Long userId, String monthYear) {
    // Implementation
}
```

### README Files
Every module should have a README with:
- Overview
- Features
- API endpoints
- Usage examples
- Configuration

### API Documentation
Use Swagger/OpenAPI annotations:
```java
@Operation(summary = "Get all budgets", description = "Returns all budgets for authenticated user")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
})
@GetMapping("/api/v1/budgets")
public ResponseEntity<List<Budget>> getBudgets() {}
```

---

## Git Workflow

### Branch Naming
```
feature/budget-alerts
bugfix/circular-dependency
hotfix/security-patch
release/v1.2.0
```

### Commit Messages
Use conventional commits format:
```
feat: Add budget variance analysis endpoint
fix: Resolve circular dependency in AlertService
docs: Update budget module documentation
test: Add integration tests for alerts
refactor: Simplify recurring transaction logic
```

### Pull Request Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No new warnings
```

---

## Performance Guidelines

### Database Queries
- Use indexes on frequently queried columns
- Avoid N+1 queries (use JOIN FETCH)
- Limit result sets with pagination

### Caching
- Use `@Cacheable` for expensive operations
- Set appropriate TTL
```java
@Cacheable(value = "budgets", key = "#userId")
public List<Budget> getBudgets(Long userId) {}
```

### API Response Time
- Target: < 200ms for simple queries
- Target: < 1s for complex calculations
- Use async processing for long-running tasks

---

## Deployment Checklist

### Before Deployment
- [ ] All tests passing
- [ ] Code reviewed and approved
- [ ] Documentation updated
- [ ] Database migrations tested
- [ ] Environment variables configured
- [ ] Security scan completed
- [ ] Performance testing done

### After Deployment
- [ ] Health check endpoint responding
- [ ] Logs monitored for errors
- [ ] Database connections healthy
- [ ] API response times normal
- [ ] Alert for any issues

---

## Tools & Resources

### Development Tools
- **IDE**: IntelliJ IDEA / VS Code
- **Database**: MySQL Workbench
- **API Testing**: Postman / Insomnia
- **Git**: GitHub Desktop / CLI

### Code Quality
- **Linting**: Checkstyle / ESLint
- **Formatting**: Google Java Format / Prettier
- **Static Analysis**: SonarQube

### Monitoring
- **Logs**: Logback / ELK Stack
- **Metrics**: Spring Actuator / Prometheus
- **APM**: New Relic / DataDog

---

## Contact & Support

For questions or clarifications, contact:
- **Backend Team**: backend@pifinance.com
- **Frontend Team**: frontend@pifinance.com
- **DevOps**: devops@pifinance.com

---

**Document Owner**: Engineering Team  
**Review Cycle**: Quarterly  
**Next Review**: May 1, 2026
