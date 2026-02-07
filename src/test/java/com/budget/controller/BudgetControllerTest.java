package com.budget.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Budget Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetControllerTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;

    @BeforeEach
    void setUp() {
        authHelper = new AuthHelper(requestSpec);
        Map<String, Object> userData = createTestUser();
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

    private Map<String, Object> createTestUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("email", "test" + System.currentTimeMillis() + "@example.com");
        user.put("password", "Test@1234");
        user.put("name", "Test User");
        user.put("mobileNumber", "9876543210");
        return user;
    }

    private Map<String, Object> createExpenseData(String category, double amount) {
        Map<String, Object> expense = new HashMap<>();
        expense.put("userId", userId);
        expense.put("category", category);
        expense.put("amount", amount);
        expense.put("description", "Test expense");
        expense.put("expenseDate", "2024-01-15");
        return expense;
    }

    private Map<String, Object> createIncomeData(String source, double amount) {
        Map<String, Object> income = new HashMap<>();
        income.put("userId", userId);
        income.put("source", source);
        income.put("amount", amount);
        income.put("description", "Test income");
        income.put("incomeDate", "2024-01-15");
        return income;
    }

    private Map<String, Object> createBudgetData(String category, double limit) {
        Map<String, Object> budget = new HashMap<>();
        budget.put("userId", userId);
        budget.put("category", category);
        budget.put("monthlyLimit", limit);
        budget.put("month", "2024-01");
        return budget;
    }

    @Test
    @Order(1)
    @DisplayName("Should add expense successfully")
    void testAddExpense() {
        Map<String, Object> expenseData = createExpenseData("FOOD", 500.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(expenseData)
                .when()
                .post("/api/v1/budget/expense");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(2)
    @DisplayName("Should get expenses for user")
    void testGetExpenses() {
        authHelper.getAuthenticatedSpec()
                .body(createExpenseData("TRANSPORT", 200.0))
                .post("/api/v1/budget/expense");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/expense/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should add income successfully")
    void testAddIncome() {
        Map<String, Object> incomeData = createIncomeData("SALARY", 50000.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(incomeData)
                .when()
                .post("/api/v1/budget/income");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(4)
    @DisplayName("Should get incomes for user")
    void testGetIncomes() {
        authHelper.getAuthenticatedSpec()
                .body(createIncomeData("FREELANCE", 10000.0))
                .post("/api/v1/budget/income");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/income/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(5)
    @DisplayName("Should create budget successfully")
    void testCreateBudget() {
        Map<String, Object> budgetData = createBudgetData("ENTERTAINMENT", 5000.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(budgetData)
                .when()
                .post("/api/v1/budget");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(6)
    @DisplayName("Should get budgets for user")
    void testGetBudgets() {
        authHelper.getAuthenticatedSpec()
                .body(createBudgetData("SHOPPING", 8000.0))
                .post("/api/v1/budget");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/budget/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(7)
    @DisplayName("Should get expense summary")
    void testGetExpenseSummary() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("userId", userId)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .when()
                .get("/api/v1/budget/expense/summary");
        ApiAssertions.assertStatusCode(response, 200);
    }
}
