# Testing Process & Guidelines

**Last Updated**: February 1, 2026  
**Status**: Active  
**Coverage Target**: 80% overall (70% unit, 90% integration)

---

## 📋 Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [End-to-End Testing](#end-to-end-testing)
5. [API Testing](#api-testing)
6. [Test Data Management](#test-data-management)
7. [Continuous Integration](#continuous-integration)
8. [Best Practices](#best-practices)

---

## Testing Strategy

### Test Pyramid
```
           /\
          /  \        E2E Tests (10%)
         /____\       - Critical user flows
        /      \      - Smoke tests
       /________\     Integration Tests (30%)
      /          \    - API endpoints
     /____________\   - Service layer
    /              \  Unit Tests (60%)
   /________________\ - Business logic
                      - Utility functions
```

### Coverage Goals
| Layer | Target | Priority |
|-------|--------|----------|
| Unit Tests | 70% | P0 |
| Integration Tests | 90% of critical paths | P0 |
| E2E Tests | Key user flows | P1 |
| API Tests | All public endpoints | P0 |

---

## Unit Testing

### Framework Setup
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
```

### Service Layer Tests

#### Example: BudgetServiceTest.java
```java
@SpringBootTest
class BudgetServiceTest {
    
    @Mock
    private BudgetRepository budgetRepository;
    
    @Mock
    private ExpenseRepository expenseRepository;
    
    @InjectMocks
    private BudgetServiceImpl budgetService;
    
    private Budget testBudget;
    private Expense testExpense;
    
    @BeforeEach
    void setUp() {
        testBudget = Budget.builder()
            .id(1L)
            .userId(100L)
            .category("FOOD")
            .monthlyLimit(new BigDecimal("15000"))
            .monthYear("2026-02")
            .build();
            
        testExpense = Expense.builder()
            .id(1L)
            .userId(100L)
            .amount(new BigDecimal("500"))
            .category("FOOD")
            .expenseDate(LocalDate.now())
            .build();
    }
    
    @Test
    @DisplayName("Should create budget successfully")
    void shouldCreateBudget() {
        // Given
        when(budgetRepository.save(any(Budget.class)))
            .thenReturn(testBudget);
        
        // When
        Budget result = budgetService.createBudget(testBudget);
        
        // Then
        assertNotNull(result);
        assertEquals(testBudget.getId(), result.getId());
        assertEquals(testBudget.getCategory(), result.getCategory());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }
    
    @Test
    @DisplayName("Should throw exception when budget not found")
    void shouldThrowExceptionWhenBudgetNotFound() {
        // Given
        Long budgetId = 999L;
        when(budgetRepository.findById(budgetId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> budgetService.getBudgetById(budgetId));
    }
    
    @Test
    @DisplayName("Should calculate budget variance correctly")
    void shouldCalculateBudgetVariance() {
        // Given
        Long userId = 100L;
        String monthYear = "2026-02";
        
        List<Budget> budgets = Arrays.asList(testBudget);
        List<Expense> expenses = Arrays.asList(testExpense);
        
        when(budgetRepository.findByUserIdAndMonthYear(userId, monthYear))
            .thenReturn(budgets);
        when(expenseRepository.findByUserIdAndMonthYear(userId, monthYear))
            .thenReturn(expenses);
        
        // When
        BudgetVarianceAnalysis result = 
            budgetService.getBudgetVsActualReport(userId, monthYear);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("15000"), result.getTotalBudget());
        assertEquals(new BigDecimal("500"), result.getTotalSpent());
        assertEquals(new BigDecimal("14500"), result.getTotalVariance());
        assertEquals(BudgetStatus.UNDER_BUDGET, result.getOverallStatus());
    }
}
```

### Repository Tests

#### Example: BudgetRepositoryTest.java
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BudgetRepositoryTest {
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("Should find budgets by user ID and month")
    void shouldFindBudgetsByUserIdAndMonth() {
        // Given
        Budget budget = Budget.builder()
            .userId(100L)
            .category("FOOD")
            .monthlyLimit(new BigDecimal("15000"))
            .monthYear("2026-02")
            .build();
        entityManager.persist(budget);
        entityManager.flush();
        
        // When
        List<Budget> result = budgetRepository
            .findByUserIdAndMonthYear(100L, "2026-02");
        
        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("FOOD", result.get(0).getCategory());
    }
    
    @Test
    @DisplayName("Should delete all budgets for user")
    void shouldDeleteAllBudgetsForUser() {
        // Given
        Long userId = 100L;
        Budget budget1 = createBudget(userId, "FOOD");
        Budget budget2 = createBudget(userId, "TRANSPORT");
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.flush();
        
        // When
        budgetRepository.deleteByUserId(userId);
        
        // Then
        List<Budget> result = budgetRepository
            .findByUserId(userId);
        assertTrue(result.isEmpty());
    }
    
    private Budget createBudget(Long userId, String category) {
        return Budget.builder()
            .userId(userId)
            .category(category)
            .monthlyLimit(new BigDecimal("10000"))
            .monthYear("2026-02")
            .build();
    }
}
```

### Utility Class Tests

```java
class DateUtilsTest {
    
    @Test
    void shouldFormatDateCorrectly() {
        LocalDate date = LocalDate.of(2026, 2, 1);
        String result = DateUtils.formatMonthYear(date);
        assertEquals("2026-02", result);
    }
    
    @Test
    void shouldParseMonthYearCorrectly() {
        String monthYear = "2026-02";
        LocalDate result = DateUtils.parseMonthYear(monthYear);
        assertEquals(2026, result.getYear());
        assertEquals(2, result.getMonthValue());
    }
}
```

---

## Integration Testing

### Controller Integration Tests

#### Example: BudgetControllerIntegrationTest.java
```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
class BudgetControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private String jwtToken;
    private Long testUserId;
    
    @BeforeEach
    void setUp() {
        // Clean database
        budgetRepository.deleteAll();
        
        // Create test user and get JWT token
        User testUser = createTestUser();
        testUserId = testUser.getId();
        jwtToken = generateJwtToken(testUser);
    }
    
    @Test
    @DisplayName("Should create budget with valid request")
    void shouldCreateBudgetWithValidRequest() throws Exception {
        // Given
        BudgetRequest request = BudgetRequest.builder()
            .userId(testUserId)
            .category("FOOD")
            .monthlyLimit(new BigDecimal("15000"))
            .monthYear("2026-02")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/budget/users/{userId}/budgets", testUserId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.category").value("FOOD"))
            .andExpect(jsonPath("$.monthlyLimit").value(15000))
            .andExpect(jsonPath("$.monthYear").value("2026-02"));
    }
    
    @Test
    @DisplayName("Should return 400 for invalid budget request")
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        // Given - negative monthly limit
        BudgetRequest request = BudgetRequest.builder()
            .userId(testUserId)
            .category("FOOD")
            .monthlyLimit(new BigDecimal("-1000"))
            .monthYear("2026-02")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/budget/users/{userId}/budgets", testUserId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
    
    @Test
    @DisplayName("Should get all budgets for user")
    void shouldGetAllBudgetsForUser() throws Exception {
        // Given
        Budget budget1 = createBudget(testUserId, "FOOD", "15000");
        Budget budget2 = createBudget(testUserId, "TRANSPORT", "5000");
        budgetRepository.saveAll(Arrays.asList(budget1, budget2));
        
        // When & Then
        mockMvc.perform(get("/api/v1/budget/users/{userId}/budgets", testUserId)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].category").value("FOOD"))
            .andExpect(jsonPath("$[1].category").value("TRANSPORT"));
    }
    
    @Test
    @DisplayName("Should return 401 without authentication")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/budget/users/{userId}/budgets", testUserId))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should return 403 for unauthorized user")
    void shouldReturn403ForUnauthorizedUser() throws Exception {
        // Given - different user's JWT token
        User otherUser = createTestUser("other@example.com");
        String otherUserToken = generateJwtToken(otherUser);
        
        // When & Then
        mockMvc.perform(get("/api/v1/budget/users/{userId}/budgets", testUserId)
                .header("Authorization", "Bearer " + otherUserToken))
            .andExpect(status().isForbidden());
    }
    
    private User createTestUser() {
        return createTestUser("test@example.com");
    }
    
    private User createTestUser(String email) {
        User user = User.builder()
            .email(email)
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .build();
        return userRepository.save(user);
    }
    
    private String generateJwtToken(User user) {
        // Implementation to generate JWT token
        return "test-jwt-token";
    }
    
    private Budget createBudget(Long userId, String category, String limit) {
        return Budget.builder()
            .userId(userId)
            .category(category)
            .monthlyLimit(new BigDecimal(limit))
            .monthYear("2026-02")
            .build();
    }
}
```

### Database Integration Tests

```java
@SpringBootTest
@Transactional
class BudgetServiceIntegrationTest {
    
    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Test
    @DisplayName("Should persist budget to database")
    void shouldPersistBudgetToDatabase() {
        // Given
        Budget budget = Budget.builder()
            .userId(100L)
            .category("FOOD")
            .monthlyLimit(new BigDecimal("15000"))
            .monthYear("2026-02")
            .build();
        
        // When
        Budget saved = budgetService.createBudget(budget);
        
        // Then
        assertNotNull(saved.getId());
        
        Optional<Budget> found = budgetRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(budget.getCategory(), found.get().getCategory());
    }
}
```

---

## End-to-End Testing

### Selenium WebDriver Setup

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BudgetE2ETest {
    
    @LocalServerPort
    private int port;
    
    private WebDriver driver;
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        baseUrl = "http://localhost:" + port;
    }
    
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    @DisplayName("Should create budget through UI")
    void shouldCreateBudgetThroughUI() {
        // Given - User logged in
        driver.get(baseUrl + "/login");
        login("test@example.com", "password");
        
        // When - Navigate to budget page and create budget
        driver.get(baseUrl + "/budget");
        driver.findElement(By.id("add-budget-btn")).click();
        
        driver.findElement(By.id("category")).sendKeys("FOOD");
        driver.findElement(By.id("monthlyLimit")).sendKeys("15000");
        driver.findElement(By.id("monthYear")).sendKeys("2026-02");
        driver.findElement(By.id("submit-btn")).click();
        
        // Then - Verify budget appears in list
        WebElement budgetList = driver.findElement(By.id("budget-list"));
        assertTrue(budgetList.getText().contains("FOOD"));
        assertTrue(budgetList.getText().contains("15000"));
    }
    
    private void login(String email, String password) {
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-btn")).click();
    }
}
```

---

## API Testing

### Postman/Newman Tests

#### Collection Structure
```json
{
  "info": {
    "name": "Budget API Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/v1/auth/login",
            "body": {
              "mode": "raw",
              "raw": "{\"email\": \"test@example.com\", \"password\": \"password123\"}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 200', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "pm.test('Token is present', function () {",
                  "    const response = pm.response.json();",
                  "    pm.expect(response.token).to.exist;",
                  "    pm.environment.set('authToken', response.token);",
                  "});"
                ]
              }
            }
          ]
        }
      ]
    },
    {
      "name": "Budgets",
      "item": [
        {
          "name": "Create Budget",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/v1/budget/users/{{userId}}/budgets",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{authToken}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"category\": \"FOOD\", \"monthlyLimit\": 15000, \"monthYear\": \"2026-02\"}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "pm.test('Status code is 201', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "pm.test('Budget is created', function () {",
                  "    const response = pm.response.json();",
                  "    pm.expect(response.id).to.exist;",
                  "    pm.expect(response.category).to.eql('FOOD');",
                  "    pm.environment.set('budgetId', response.id);",
                  "});"
                ]
              }
            }
          ]
        }
      ]
    }
  ]
}
```

### REST Assured Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BudgetAPITest {
    
    @LocalServerPort
    private int port;
    
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        
        // Login and get token
        jwtToken = given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"test@example.com\", \"password\": \"password\"}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }
    
    @Test
    void shouldCreateBudget() {
        BudgetRequest request = new BudgetRequest();
        request.setCategory("FOOD");
        request.setMonthlyLimit(new BigDecimal("15000"));
        request.setMonthYear("2026-02");
        
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/budget/users/1/budgets")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("category", equalTo("FOOD"))
            .body("monthlyLimit", equalTo(15000));
    }
}
```

---

## Test Data Management

### Test Data Builder Pattern

```java
public class BudgetTestDataBuilder {
    private Long id = 1L;
    private Long userId = 100L;
    private String category = "FOOD";
    private BigDecimal monthlyLimit = new BigDecimal("15000");
    private String monthYear = "2026-02";
    
    public BudgetTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public BudgetTestDataBuilder withUserId(Long userId) {
        this.userId = userId;
        return this;
    }
    
    public BudgetTestDataBuilder withCategory(String category) {
        this.category = category;
        return this;
    }
    
    public Budget build() {
        return Budget.builder()
            .id(id)
            .userId(userId)
            .category(category)
            .monthlyLimit(monthlyLimit)
            .monthYear(monthYear)
            .build();
    }
}

// Usage
Budget budget = new BudgetTestDataBuilder()
    .withUserId(200L)
    .withCategory("TRANSPORT")
    .build();
```

### SQL Test Data

```sql
-- src/test/resources/data.sql

-- Test users
INSERT INTO users (id, email, password, first_name, last_name) 
VALUES (100, 'test@example.com', 'password', 'Test', 'User');

-- Test budgets
INSERT INTO budgets (id, user_id, category, monthly_limit, month_year)
VALUES 
    (1, 100, 'FOOD', 15000.00, '2026-02'),
    (2, 100, 'TRANSPORT', 5000.00, '2026-02');

-- Test expenses
INSERT INTO expenses (id, user_id, amount, category, expense_date, description)
VALUES 
    (1, 100, 500.00, 'FOOD', '2026-02-01', 'Groceries'),
    (2, 100, 200.00, 'TRANSPORT', '2026-02-02', 'Bus fare');
```

---

## Continuous Integration

### GitHub Actions Workflow

```yaml
# .github/workflows/tests.yml
name: Run Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: test_db
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'adopt'
      
      - name: Run unit tests
        run: ./gradlew test
      
      - name: Run integration tests
        run: ./gradlew integrationTest
      
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./build/reports/jacoco/test/jacocoTestReport.xml
      
      - name: Publish test results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: build/test-results/**/*.xml
```

---

## Best Practices

### Test Naming
Use descriptive test method names:
```java
// ✅ Good
@Test
void shouldCreateBudgetWhenValidInputProvided() {}

@Test
void shouldThrowExceptionWhenBudgetNotFound() {}

// ❌ Bad
@Test
void test1() {}

@Test
void budgetTest() {}
```

### Test Organization
- One assertion per test (when possible)
- Use Given-When-Then structure
- Keep tests independent
- Clean up after tests

### Mocking Guidelines
- Mock external dependencies only
- Don't mock entities or DTOs
- Use real objects when possible

### Performance
- Use `@Transactional` for rollback
- Use in-memory H2 for fast tests
- Parallel execution when appropriate

---

## Troubleshooting

### Common Issues

**Test fails with "Connection refused"**
- Ensure database is running
- Check connection properties in `application-test.yml`

**Flaky tests (random failures)**
- Check for timing issues
- Ensure tests are independent
- Clean up shared state

**Slow tests**
- Use `@DataJpaTest` instead of `@SpringBootTest`
- Mock external calls
- Use test containers wisely

---

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [REST Assured](https://rest-assured.io/)

---

**Document Owner**: QA Team  
**Review Cycle**: Quarterly  
**Next Review**: May 1, 2026
