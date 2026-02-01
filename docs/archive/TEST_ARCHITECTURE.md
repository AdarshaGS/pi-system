# PI System - API Test Framework Architecture

## Directory Structure

```
pi-system/
├── src/
│   └── test/
│       ├── java/com/api/
│       │   ├── config/
│       │   │   └── BaseApiTest.java              # Base class for all tests
│       │   ├── helpers/
│       │   │   ├── AuthHelper.java               # Authentication utilities
│       │   │   ├── TestDataBuilder.java          # Test data generation
│       │   │   └── ApiAssertions.java            # Common assertions
│       │   ├── auth/
│       │   │   └── AuthControllerIntegrationTest.java
│       │   ├── savings/
│       │   │   ├── SavingsAccountControllerIntegrationTest.java
│       │   │   ├── FixedDepositControllerIntegrationTest.java
│       │   │   └── RecurringDepositControllerIntegrationTest.java (TODO)
│       │   ├── portfolio/
│       │   │   └── PortfolioControllerIntegrationTest.java
│       │   ├── investments/ (TODO)
│       │   ├── loan/ (TODO)
│       │   ├── budget/ (TODO)
│       │   └── ... (other modules)
│       └── resources/
│           └── application-test.yml              # Test configuration
├── build.gradle                                   # Dependencies + JaCoCo
├── run-api-tests.sh                               # Test execution script
├── API_TESTING.md                                 # Comprehensive guide
└── TEST_SUMMARY.md                                # Implementation summary
```

## Test Execution Flow

```
┌─────────────────────────────────────────────────────────────┐
│  1. Start Spring Boot with Test Profile                    │
│     @SpringBootTest(webEnvironment = RANDOM_PORT)           │
│     @ActiveProfiles("test")                                 │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  2. Initialize Test Database (H2 In-Memory)                 │
│     - Fast startup                                          │
│     - Isolated from production DB                           │
│     - Fresh state for each test run                         │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Configure REST Assured                                  │
│     - Base URL: http://localhost:{random-port}              │
│     - Content-Type: application/json                        │
│     - Request/Response logging enabled                      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Run Test: Example Flow                                  │
│                                                             │
│  ┌─────────────────────────────────────────────────┐       │
│  │ @BeforeEach: Setup                              │       │
│  │ • Register test user                            │       │
│  │ • Login to get JWT token                        │       │
│  └─────────────────────────────────────────────────┘       │
│                    │                                        │
│  ┌─────────────────▼───────────────────────────────┐       │
│  │ @Test: Create Savings Account                   │       │
│  │ • Prepare test data                             │       │
│  │ • Make POST /savings-accounts with JWT          │       │
│  │ • Assert 201 Created                            │       │
│  │ • Assert response fields                        │       │
│  └─────────────────────────────────────────────────┘       │
│                    │                                        │
│  ┌─────────────────▼───────────────────────────────┐       │
│  │ @Test: Create Duplicate (409 Exception)         │       │
│  │ • Create first savings account                  │       │
│  │ • Attempt to create duplicate                   │       │
│  │ • Assert 409 Conflict                           │       │
│  │ • Assert error message                          │       │
│  └─────────────────────────────────────────────────┘       │
│                    │                                        │
│  ┌─────────────────▼───────────────────────────────┐       │
│  │ @Test: Get Non-Existent (404 Exception)         │       │
│  │ • Request non-existent ID: /savings/999999      │       │
│  │ • Assert 404 Not Found                          │       │
│  │ • Assert error message                          │       │
│  └─────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  5. Generate Reports                                        │
│     • Test Results: build/reports/tests/test/index.html     │
│     • Coverage: build/reports/jacoco/test/html/index.html   │
└─────────────────────────────────────────────────────────────┘
```

## Request Flow - How Tests Call APIs

```
Test Class                      Spring Boot App
┌──────────────────┐           ┌──────────────────────────┐
│                  │           │                          │
│  Integration     │  HTTP     │   Controller Layer       │
│  Test            ├──────────►│   @RestController        │
│  (REST Assured)  │  Request  │   @PreAuthorize          │
│                  │           │                          │
└──────────────────┘           └────────┬─────────────────┘
                                        │
                                        ▼
                               ┌──────────────────────────┐
                               │   Service Layer          │
                               │   @Service               │
                               │   @Transactional         │
                               │   Custom Exceptions      │
                               └────────┬─────────────────┘
                                        │
                                        ▼
                               ┌──────────────────────────┐
                               │   Repository Layer       │
                               │   JpaRepository          │
                               └────────┬─────────────────┘
                                        │
                                        ▼
                               ┌──────────────────────────┐
                               │   H2 Database            │
                               │   (In-Memory)            │
                               └──────────────────────────┘
```

## Authentication Flow in Tests

```
┌─────────────────────────────────────────────────────────────┐
│  Step 1: Register User (POST /auth/register)               │
│  ┌───────────────────────────────────────────────────┐     │
│  │ {                                                 │     │
│  │   "email": "test1234567890@example.com",         │     │
│  │   "password": "Test@1234",                       │     │
│  │   "firstName": "Test",                           │     │
│  │   "lastName": "User"                             │     │
│  │ }                                                 │     │
│  └───────────────────────────────────────────────────┘     │
│                     │                                       │
│                     ▼                                       │
│  ┌───────────────────────────────────────────────────┐     │
│  │ Response 201 Created                              │     │
│  │ { "id": 1, "email": "test...", ... }             │     │
│  └───────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 2: Login (POST /auth/login)                          │
│  ┌───────────────────────────────────────────────────┐     │
│  │ {                                                 │     │
│  │   "email": "test1234567890@example.com",         │     │
│  │   "password": "Test@1234"                        │     │
│  │ }                                                 │     │
│  └───────────────────────────────────────────────────┘     │
│                     │                                       │
│                     ▼                                       │
│  ┌───────────────────────────────────────────────────┐     │
│  │ Response 200 OK                                   │     │
│  │ {                                                 │     │
│  │   "token": "eyJhbGc...",  ◄─ Store this          │     │
│  │   "refreshToken": "...",                         │     │
│  │   "user": { "id": 1, ... }                       │     │
│  │ }                                                 │     │
│  └───────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  Step 3: Call Protected Endpoint                            │
│  ┌───────────────────────────────────────────────────┐     │
│  │ POST /savings-accounts                            │     │
│  │ Headers:                                          │     │
│  │   Authorization: Bearer eyJhbGc...  ◄─ Use token │     │
│  │   Content-Type: application/json                 │     │
│  │                                                   │     │
│  │ Body: { "bankName": "HDFC", ... }                │     │
│  └───────────────────────────────────────────────────┘     │
│                     │                                       │
│                     ▼                                       │
│  ┌───────────────────────────────────────────────────┐     │
│  │ Response 201 Created                              │     │
│  │ { "id": 10, "bankName": "HDFC", ... }            │     │
│  └───────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Test Data Generation Strategy

```
┌─────────────────────────────────────────────────────────────┐
│  TestDataBuilder Pattern                                    │
│                                                             │
│  Problem: Tests need unique data to avoid conflicts         │
│  Solution: Generate unique values using timestamps          │
│                                                             │
│  Example:                                                   │
│  ┌───────────────────────────────────────────────────┐     │
│  │ public static String generateUniqueEmail() {     │     │
│  │   return "test" +                                 │     │
│  │          System.currentTimeMillis() +            │     │
│  │          "@example.com";                         │     │
│  │ }                                                 │     │
│  │                                                   │     │
│  │ // Produces:                                     │     │
│  │ test1704123456789@example.com                    │     │
│  │ test1704123457890@example.com  ◄─ Always unique  │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
│  Benefits:                                                  │
│  ✓ No manual cleanup needed                                │
│  ✓ Tests can run in any order                              │
│  ✓ Parallel execution possible                             │
│  ✓ No database state dependencies                          │
└─────────────────────────────────────────────────────────────┘
```

## Exception Handling Validation

```
┌─────────────────────────────────────────────────────────────┐
│  Test: Duplicate Savings Account (409 Conflict)            │
│                                                             │
│  1. Create First Account                                    │
│     POST /savings-accounts                                  │
│     { "bankName": "HDFC", "amount": 50000 }                │
│     Response: 201 Created ✓                                 │
│                                                             │
│  2. Attempt Duplicate                                       │
│     POST /savings-accounts                                  │
│     { "bankName": "HDFC", "amount": 50000 }  ◄─ Same data  │
│                                                             │
│  3. Service Layer Catches                                   │
│     try {                                                   │
│       savingsAccountRepository.save(account);              │
│     } catch (DataIntegrityViolationException e) {          │
│       throw new DuplicateSavingsEntityException(           │
│         "Savings account already exists"                   │
│       );                                                    │
│     }                                                       │
│                                                             │
│  4. Controller Returns                                      │
│     HTTP 409 Conflict                                       │
│     {                                                       │
│       "status": 409,                                        │
│       "message": "Savings account already exists",         │
│       "timestamp": "2024-01-15T10:30:00"                   │
│     }                                                       │
│                                                             │
│  5. Test Asserts                                            │
│     ApiAssertions.assertStatusCode(response, 409);         │
│     response.then()                                         │
│       .body("message", containsString("already exists"));  │
│                                                             │
│  ✓ Validates recent exception handling changes!            │
└─────────────────────────────────────────────────────────────┘
```

## Helper Classes Usage

```
┌──────────────────────────────────────────────────────────┐
│  AuthHelper - Simplifies Authentication                  │
│                                                          │
│  Without Helper (Verbose):                              │
│  ┌────────────────────────────────────────────────┐    │
│  │ String registerBody = String.format("""        │    │
│  │   { "email": "%s", "password": "%s", ... }     │    │
│  │   """, email, password);                       │    │
│  │ Response regResponse = given()                 │    │
│  │   .spec(requestSpec)                           │    │
│  │   .body(registerBody)                          │    │
│  │   .post("/auth/register");                     │    │
│  │                                                │    │
│  │ String loginBody = String.format("""           │    │
│  │   { "email": "%s", "password": "%s" }          │    │
│  │   """, email, password);                       │    │
│  │ Response loginResponse = given()               │    │
│  │   .spec(requestSpec)                           │    │
│  │   .body(loginBody)                             │    │
│  │   .post("/auth/login");                        │    │
│  │                                                │    │
│  │ String token = loginResponse                   │    │
│  │   .jsonPath().getString("token");              │    │
│  │                                                │    │
│  │ Response dataResponse = given()                │    │
│  │   .spec(requestSpec)                           │    │
│  │   .header("Authorization", "Bearer " + token)  │    │
│  │   .body(data)                                  │    │
│  │   .post("/savings-accounts");                  │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  With Helper (Clean):                                   │
│  ┌────────────────────────────────────────────────┐    │
│  │ authHelper.register(email, password,           │    │
│  │   firstName, lastName);                        │    │
│  │ authHelper.login(email, password);             │    │
│  │                                                │    │
│  │ Response response = authHelper                 │    │
│  │   .getAuthenticatedSpec()                      │    │
│  │   .body(data)                                  │    │
│  │   .post("/savings-accounts");                  │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  Benefits: Less code, more readable, reusable           │
└──────────────────────────────────────────────────────────┘
```

## Test Coverage Tracking

```
┌─────────────────────────────────────────────────────────────┐
│  JaCoCo Coverage Report                                     │
│                                                             │
│  Configuration (build.gradle):                              │
│  • Minimum coverage threshold: 60%                          │
│  • Excludes: config/, entity/, dto/ (data classes)         │
│  • Includes: service/, controller/, repository/             │
│                                                             │
│  Generate Report:                                           │
│  ./gradlew test jacocoTestReport                           │
│                                                             │
│  View Report:                                               │
│  open build/reports/jacoco/test/html/index.html            │
│                                                             │
│  Report Shows:                                              │
│  ┌───────────────────────────────────────────────────┐     │
│  │ Package             Coverage    Branches           │     │
│  ├───────────────────────────────────────────────────┤     │
│  │ com.auth.service    ██████████  95% (20/21 lines) │     │
│  │ com.savings.service ████████░░  85% (85/100 lines)│     │
│  │ com.portfolio.srv   ██████░░░░  75% (45/60 lines) │     │
│  │ com.budget.service  ████░░░░░░  45% (18/40 lines) │     │
│  │                                                    │     │
│  │ OVERALL:            ██████░░░░  68% (168/247)     │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
│  Green = Good coverage, Red = Needs more tests              │
└─────────────────────────────────────────────────────────────┘
```

## Quick Reference Commands

```bash
# Start Redis (Required)
brew services start redis

# Run All API Tests
./run-api-tests.sh all

# Run Specific Test Suite
./run-api-tests.sh auth
./run-api-tests.sh savings
./run-api-tests.sh portfolio

# Run with Coverage
./run-api-tests.sh coverage

# Run Single Test Class
./gradlew test --tests "com.api.auth.AuthControllerIntegrationTest"

# Run Single Test Method
./gradlew test --tests "*.testSuccessfulLogin"

# Clean and Test
./gradlew clean test

# View Test Report
open build/reports/tests/test/index.html

# View Coverage Report
open build/reports/jacoco/test/html/index.html
```

## File Locations Quick Map

```
Key Files to Know:
• Base test class: src/test/java/com/api/config/BaseApiTest.java
• Auth helper: src/test/java/com/api/helpers/AuthHelper.java
• Test data: src/test/java/com/api/helpers/TestDataBuilder.java
• Assertions: src/test/java/com/api/helpers/ApiAssertions.java
• Test config: src/test/resources/application-test.yml
• Build config: build.gradle (dependencies + JaCoCo)
• Run script: run-api-tests.sh
• Documentation: API_TESTING.md, TEST_SUMMARY.md
• Example tests: src/test/java/com/api/auth/AuthControllerIntegrationTest.java
```

---

**Visual Guide Version**: 1.0  
**Last Updated**: 2024-01-15  
**For**: PI System API Automation Testing Framework
