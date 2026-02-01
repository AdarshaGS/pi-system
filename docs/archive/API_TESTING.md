# API Automation Testing Guide

## Overview

This document provides a comprehensive guide for the API automation testing framework implemented for the PI System. The framework uses REST Assured for API testing with Spring Boot Test integration.

## Test Framework Components

### 1. Base Configuration
- **BaseApiTest.java** - Base class for all API tests with common setup
- **application-test.yml** - Test-specific configuration (H2 database, test JWT secrets)

### 2. Helper Classes
- **AuthHelper.java** - Authentication utilities (login, register, token management)
- **TestDataBuilder.java** - Test data generation (users, portfolios, savings, etc.)
- **ApiAssertions.java** - Common assertion methods for API responses

### 3. Integration Tests
- **AuthControllerIntegrationTest** - Authentication flow tests
- **SavingsAccountControllerIntegrationTest** - Savings account CRUD with exception validation
- **PortfolioControllerIntegrationTest** - Portfolio management tests

## Running Tests

### Prerequisites
1. **Redis must be running** (required for caching)
   ```bash
   brew services start redis
   ```

### Run Tests Using Script
```bash
# Run all API tests
./run-api-tests.sh all

# Run specific test suites
./run-api-tests.sh auth
./run-api-tests.sh savings
./run-api-tests.sh portfolio

# Run with coverage report
./run-api-tests.sh coverage
```

### Run Tests Using Gradle
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.api.auth.AuthControllerIntegrationTest"

# Run specific test method
./gradlew test --tests "com.api.auth.AuthControllerIntegrationTest.testSuccessfulLogin"

# Run all API integration tests
./gradlew test --tests "com.api.*"

# Run with coverage
./gradlew test jacocoTestReport
```

### Run Tests in IDE
1. Right-click on test class/method in IntelliJ/Eclipse
2. Select "Run" or "Debug"
3. Ensure Redis is running first

## Test Reports

### HTML Test Report
After running tests, open:
```
build/reports/tests/test/index.html
```

### Coverage Report (if jacoco configured)
```
build/reports/jacoco/test/html/index.html
```

## Writing New Tests

### Example: Creating a New Controller Test

```java
package com.api.newmodule;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;

    @BeforeEach
    void setUp() {
        authHelper = new AuthHelper(requestSpec);
        
        // Setup test user
        Map<String, Object> userData = TestDataBuilder.createTestUser();
        authHelper.register(/*...*/);
        Response loginResponse = authHelper.login(/*...*/);
        userId = loginResponse.jsonPath().getLong("user.id");
    }

    @Test
    @Order(1)
    @DisplayName("Should perform operation successfully")
    void testOperation() {
        // Given
        String requestBody = """
            {
                "field": "value"
            }
            """;

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(requestBody)
                .when()
                .post("/api/endpoint");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
    }
}
```

## Test Coverage Goals

### Current Coverage (Before API Tests)
- ~40% overall coverage
- Mostly unit tests with mocked dependencies

### Target Coverage (After Full Implementation)
- **80%+ overall coverage**
- Integration tests for all 19 controllers
- End-to-end API flows

### Priority Controllers to Test
1. ✅ **Auth Controller** - Authentication flows (DONE)
2. ✅ **Savings Account Controller** - Exception handling validation (DONE)
3. ✅ **Portfolio Controller** - Stock management (DONE)
4. ⏳ **Fixed Deposit Controller** - FD CRUD operations
5. ⏳ **Recurring Deposit Controller** - RD CRUD operations
6. ⏳ **Mutual Fund Controller** - MF portfolio
7. ⏳ **ETF Controller** - ETF portfolio
8. ⏳ **Loan Controller** - Loan tracking
9. ⏳ **Lending Controller** - P2P lending
10. ⏳ **Insurance Controller** - Insurance policies
11. ⏳ **Budget Controller** - Budget management
12. ⏳ **Tax Controller** - Tax calculations
13. ⏳ **NetWorth Controller** - Net worth aggregation
14. ⏳ **AA Controller** - Account Aggregator integration
15. ⏳ **Stock Controller** - Stock data APIs
16. ⏳ **Developer Tools Controller** - Admin utilities
17. ⏳ **Health Check Controller** - System health
18. ⏳ **User Controller** - User management
19. ⏳ **Settings Controller** - User preferences

## Best Practices

### 1. Test Isolation
- Each test should be independent
- Use `@BeforeEach` to set up fresh state
- Clean up resources after tests if needed

### 2. Meaningful Test Names
```java
@DisplayName("Should return 409 when creating duplicate savings account")
void testDuplicateSavingsAccount() { }
```

### 3. Arrange-Act-Assert Pattern
```java
// Given (Arrange)
String requestBody = "...";

// When (Act)
Response response = authHelper.getAuthenticatedSpec()
        .body(requestBody)
        .post("/endpoint");

// Then (Assert)
ApiAssertions.assertStatusCode(response, 201);
```

### 4. Test Edge Cases
- ✅ Happy path (successful operations)
- ✅ Validation errors (400 Bad Request)
- ✅ Unauthorized access (401 Unauthorized)
- ✅ Forbidden operations (403 Forbidden)
- ✅ Not found (404 Not Found)
- ✅ Duplicate resources (409 Conflict)
- ✅ Server errors (500 Internal Server Error)

### 5. Use Test Helpers
```java
// Instead of this:
Response response = given()
        .header("Authorization", "Bearer " + token)
        .body(data)
        .post("/endpoint");

// Use this:
Response response = authHelper.getAuthenticatedSpec()
        .body(data)
        .post("/endpoint");
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: API Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      redis:
        image: redis:7
        ports:
          - 6379:6379
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run API Tests
        run: ./gradlew test --tests "com.api.*"
      
      - name: Publish Test Report
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: API Test Results
          path: build/test-results/test/*.xml
          reporter: java-junit
```

## Troubleshooting

### Redis Connection Error
```
Error: Redis is not running!
```
**Solution:**
```bash
brew services start redis
# or
docker run -d -p 6379:6379 redis:7
```

### Port Already in Use
```
Address already in use
```
**Solution:** Tests use random port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`)

### Test Data Conflicts
**Issue:** Tests fail due to duplicate data
**Solution:** Each test creates unique data using `TestDataBuilder.generateUniqueEmail()`

### Authentication Failures
**Issue:** 401 Unauthorized errors
**Solution:** Ensure `authHelper.login()` is called before accessing protected endpoints

## Adding Test Coverage for Remaining Controllers

### Template for New Tests

1. **Create test file**: `src/test/java/com/api/<module>/<Controller>IntegrationTest.java`

2. **Extend BaseApiTest**: Inherit common setup

3. **Use AuthHelper**: Handle authentication

4. **Test all CRUD operations**:
   - POST (Create) - Happy path + validation errors
   - GET (Read) - Single + List + Not found
   - PUT (Update) - Success + validation
   - DELETE (Delete) - Success + not found

5. **Test security**: Unauthorized access, user isolation

6. **Test edge cases**: Duplicates, invalid data, boundary conditions

## Next Steps

1. ✅ **Phase 1 Complete**: Base framework + Auth/Savings/Portfolio tests
2. ⏳ **Phase 2**: Fixed Deposit, Recurring Deposit, Loan tests
3. ⏳ **Phase 3**: Mutual Fund, ETF, Insurance tests
4. ⏳ **Phase 4**: Budget, Tax, NetWorth tests
5. ⏳ **Phase 5**: Developer Tools, Health Check, User tests
6. ⏳ **Phase 6**: CI/CD pipeline integration
7. ⏳ **Phase 7**: Performance tests, load tests

## Questions?

For issues or questions about the API testing framework:
1. Check test execution logs in `build/reports/tests/test/`
2. Review helper classes in `src/test/java/com/api/helpers/`
3. Examine existing tests for examples
4. Ensure all prerequisites (Redis) are running

## Benefits of This Framework

1. ✅ **Real HTTP Testing** - Tests actual API endpoints, not mocked responses
2. ✅ **Fast Execution** - H2 in-memory database for speed
3. ✅ **Isolated Tests** - Each test is independent
4. ✅ **Easy to Extend** - Simple patterns for new tests
5. ✅ **Comprehensive Coverage** - All HTTP methods, status codes, edge cases
6. ✅ **CI/CD Ready** - Can run in GitHub Actions, Jenkins, etc.
7. ✅ **Validates Recent Changes** - Catches regressions from exception handling updates
