package com.api.budget;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for Income Management in Budget Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IncomeControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;

    @BeforeEach
    void setUp() {
        authHelper = new AuthHelper(requestSpec);
        Map<String, Object> userData = TestDataBuilder.createTestUser();
        authHelper.register(
                (String) userData.get("email"),
                (String) userData.get("password"),
                (String) userData.get("name"),
                (String) userData.get("mobileNumber"),
                (String) userData.get("roles"));
        Response loginResponse = authHelper.login(
                (String) userData.get("email"),
                (String) userData.get("password"));
        userId = loginResponse.jsonPath().getLong("userId");
    }

    @Test
    @Order(1)
    @DisplayName("Should add income entry")
    void testAddIncome() {
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("userId", userId);
        incomeData.put("source", "SALARY");
        incomeData.put("amount", 75000.0);
        incomeData.put("date", LocalDate.now().toString());
        incomeData.put("isRecurring", true);
        incomeData.put("isStable", true);

        Response response = authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "source", "SALARY");
    }

    @Test
    @Order(2)
    @DisplayName("Should add multiple income sources")
    void testAddMultipleIncomes() {
        // Add salary
        Map<String, Object> salary = new HashMap<>();
        salary.put("userId", userId);
        salary.put("source", "SALARY");
        salary.put("amount", 75000.0);
        salary.put("date", LocalDate.now().toString());
        salary.put("isRecurring", true);
        salary.put("isStable", true);

        Response salaryResponse = authHelper.getAuthenticatedSpec()
                .body(salary)
                .when()
                .post("/api/v1/budget/income");
        ApiAssertions.assertStatusCode(salaryResponse, 200);

        // Add dividend income
        Map<String, Object> dividend = new HashMap<>();
        dividend.put("userId", userId);
        dividend.put("source", "DIVIDEND");
        dividend.put("amount", 5000.0);
        dividend.put("date", LocalDate.now().toString());
        dividend.put("isRecurring", false);
        dividend.put("isStable", false);

        Response dividendResponse = authHelper.getAuthenticatedSpec()
                .body(dividend)
                .when()
                .post("/api/v1/budget/income");
        ApiAssertions.assertStatusCode(dividendResponse, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get user incomes")
    void testGetUserIncomes() {
        // Add income first
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("userId", userId);
        incomeData.put("source", "FREELANCE");
        incomeData.put("amount", 25000.0);
        incomeData.put("date", LocalDate.now().toString());
        incomeData.put("isRecurring", false);
        incomeData.put("isStable", false);

        authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");

        // Get incomes
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/income/" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        assertTrue(response.jsonPath().getList("$").size() > 0,
                "Expected array with at least one income entry");
    }

    @Test
    @Order(4)
    @DisplayName("Should get cash flow analysis")
    void testGetCashFlowAnalysis() {
        // Add income
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("userId", userId);
        incomeData.put("source", "SALARY");
        incomeData.put("amount", 80000.0);
        incomeData.put("date", LocalDate.now().toString());
        incomeData.put("isRecurring", true);
        incomeData.put("isStable", true);

        authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");

        // Add expense
        Map<String, Object> expenseData = TestDataBuilder.createExpenseData(userId, "FOOD", 5000.0);
        authHelper.getAuthenticatedSpec()
                .body(expenseData)
                .when()
                .post("/api/v1/budget/expense");

        // Get cash flow analysis
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/cashflow/" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "totalIncome");
        ApiAssertions.assertFieldExists(response, "totalExpenses");
        ApiAssertions.assertFieldExists(response, "netCashFlow");
        ApiAssertions.assertFieldExists(response, "savingsRate");
        ApiAssertions.assertFieldExists(response, "cashFlowStatus");
    }

    @Test
    @Order(5)
    @DisplayName("Should calculate correct savings rate")
    void testSavingsRateCalculation() {
        // Add income: 100,000
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("userId", userId);
        incomeData.put("source", "SALARY");
        incomeData.put("amount", 100000.0);
        incomeData.put("date", LocalDate.now().toString());
        incomeData.put("isRecurring", true);
        incomeData.put("isStable", true);

        authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");

        // Add expense: 70,000 (should result in 30% savings rate)
        Map<String, Object> expenseData = TestDataBuilder.createExpenseData(userId, "TOTAL", 70000.0);
        authHelper.getAuthenticatedSpec()
                .body(expenseData)
                .when()
                .post("/api/v1/budget/expense");

        // Get cash flow
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/cashflow/" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        Double savingsRate = response.jsonPath().getDouble("savingsRate");
        Assertions.assertTrue(savingsRate >= 29.0 && savingsRate <= 31.0,
                "Expected savings rate around 30%, got: " + savingsRate);
    }

    @Test
    @Order(6)
    @DisplayName("Should provide recommendations for low savings rate")
    void testRecommendationsForLowSavings() {
        // Add income
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("userId", userId);
        incomeData.put("source", "SALARY");
        incomeData.put("amount", 50000.0);
        incomeData.put("date", LocalDate.now().toString());
        incomeData.put("isRecurring", true);
        incomeData.put("isStable", true);

        authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");

        // Add high expenses (90% of income)
        Map<String, Object> expenseData = TestDataBuilder.createExpenseData(userId, "TOTAL", 45000.0);
        authHelper.getAuthenticatedSpec()
                .body(expenseData)
                .when()
                .post("/api/v1/budget/expense");

        // Get cash flow
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/cashflow/" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "recommendations");
        
        // Should have recommendation about increasing savings rate
        String recommendations = response.jsonPath().getString("recommendations");
        Assertions.assertTrue(recommendations.contains("savings rate"),
                "Expected savings rate recommendation");
    }

    @Test
    @Order(7)
    @DisplayName("Should handle negative cash flow")
    void testNegativeCashFlow() {
        // Add income: 30,000
        Map<String, Object> incomeData = new HashMap<>();
        incomeData.put("userId", userId);
        incomeData.put("source", "SALARY");
        incomeData.put("amount", 30000.0);
        incomeData.put("date", LocalDate.now().toString());
        incomeData.put("isRecurring", true);
        incomeData.put("isStable", true);

        authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");

        // Add expenses: 40,000 (exceeds income)
        Map<String, Object> expenseData = TestDataBuilder.createExpenseData(userId, "TOTAL", 40000.0);
        authHelper.getAuthenticatedSpec()
                .body(expenseData)
                .when()
                .post("/api/v1/budget/expense");

        // Get cash flow
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/cashflow/" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "cashFlowStatus", "NEGATIVE");
        
        Double netCashFlow = response.jsonPath().getDouble("netCashFlow");
        Assertions.assertTrue(netCashFlow < 0, "Expected negative cash flow");
    }
}
