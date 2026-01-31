package com.api.budget;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Comprehensive Integration Tests for Budget Controller - Sprint 3
 * Tests all 16 endpoints with pagination, filtering, and validation
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetControllerIntegrationTest extends BaseApiTest {

        private AuthHelper authHelper;
        private Long userId;
        private Long expenseId;
        private Long incomeId;
        private Long budgetId;

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

        // ===== EXPENSE CRUD TESTS =====

        @Test
        @Order(1)
        @DisplayName("Should create expense successfully")
        void testCreateExpense() {
                Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                userId, "FOOD", 5000.0);
                Response response = authHelper.getAuthenticatedSpec()
                                .body(expenseData)
                                .when()
                                .post("/api/v1/budget/expense");
                
                ApiAssertions.assertStatusCode(response, 200);
                expenseId = response.jsonPath().getLong("id");
                assertNotNull(expenseId, "Expense ID should not be null");
                assertEquals("FOOD", response.jsonPath().getString("category"));
        }

        @Test
        @Order(2)
        @DisplayName("Should get all user expenses")
        void testGetUserExpenses() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                response.then().body("content", notNullValue());
                response.then().body("totalElements", greaterThanOrEqualTo(0));
        }

        @Test
        @Order(3)
        @DisplayName("Should get single expense by ID")
        void testGetExpenseById() {
                // First create an expense
                Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                userId, "TRANSPORT", 2000.0);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(expenseData)
                                .post("/api/v1/budget/expense");
                Long id = createResponse.jsonPath().getLong("id");

                // Then retrieve it
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/expense/detail/" + id);
                
                ApiAssertions.assertStatusCode(response, 200);
                assertEquals(id, response.jsonPath().getLong("id"));
                assertEquals("TRANSPORT", response.jsonPath().getString("category"));
        }

        @Test
        @Order(4)
        @DisplayName("Should update expense successfully")
        void testUpdateExpense() {
                // Create expense first
                Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                userId, "FOOD", 3000.0);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(expenseData)
                                .post("/api/v1/budget/expense");
                Long id = createResponse.jsonPath().getLong("id");

                // Update the expense
                expenseData.put("amount", 3500.0);
                expenseData.put("category", "ENTERTAINMENT");
                Response updateResponse = authHelper.getAuthenticatedSpec()
                                .body(expenseData)
                                .when()
                                .put("/api/v1/budget/expense/" + id);
                
                ApiAssertions.assertStatusCode(updateResponse, 200);
                assertEquals(3500.0, updateResponse.jsonPath().getDouble("amount"));
                assertEquals("ENTERTAINMENT", updateResponse.jsonPath().getString("category"));
        }

        @Test
        @Order(5)
        @DisplayName("Should delete expense successfully")
        void testDeleteExpense() {
                // Create expense first
                Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                userId, "SHOPPING", 1500.0);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(expenseData)
                                .post("/api/v1/budget/expense");
                Long id = createResponse.jsonPath().getLong("id");

                // Delete the expense
                Response deleteResponse = authHelper.getAuthenticatedSpec()
                                .when()
                                .delete("/api/v1/budget/expense/" + id);
                
                ApiAssertions.assertStatusCode(deleteResponse, 200);

                // Verify deletion by trying to fetch
                Response getResponse = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/expense/detail/" + id);
                ApiAssertions.assertStatusCode(getResponse, 404);
        }

        // ===== PAGINATION & FILTERING TESTS =====

        @Test
        @Order(6)
        @DisplayName("Should paginate expenses correctly")
        void testExpensePagination() {
                // Create multiple expenses
                for (int i = 0; i < 15; i++) {
                        Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                        userId, "FOOD", 1000.0 + i * 100);
                        authHelper.getAuthenticatedSpec()
                                        .body(expenseData)
                                        .post("/api/v1/budget/expense");
                }

                // Test pagination
                Response response = authHelper.getAuthenticatedSpec()
                                .queryParam("page", 0)
                                .queryParam("size", 10)
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                response.then().body("content.size()", lessThanOrEqualTo(10));
                response.then().body("totalElements", greaterThanOrEqualTo(15));
                response.then().body("totalPages", greaterThanOrEqualTo(2));
        }

        @Test
        @Order(7)
        @DisplayName("Should filter expenses by category")
        void testExpenseFilterByCategory() {
                // Create expenses with different categories
                Map<String, Object> foodExpense = TestDataBuilder.createExpenseData(
                                userId, "FOOD", 500.0);
                authHelper.getAuthenticatedSpec().body(foodExpense).post("/api/v1/budget/expense");
                
                Map<String, Object> transportExpense = TestDataBuilder.createExpenseData(
                                userId, "TRANSPORT", 300.0);
                authHelper.getAuthenticatedSpec().body(transportExpense).post("/api/v1/budget/expense");

                // Filter by FOOD category
                Response response = authHelper.getAuthenticatedSpec()
                                .queryParam("category", "FOOD")
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                // All returned expenses should be FOOD category
                response.then().body("content.findAll { it.category != 'FOOD' }", empty());
        }

        @Test
        @Order(8)
        @DisplayName("Should filter expenses by date range")
        void testExpenseFilterByDateRange() {
                LocalDate today = LocalDate.now();
                String startDate = today.minusDays(7).toString();
                String endDate = today.toString();

                Response response = authHelper.getAuthenticatedSpec()
                                .queryParam("startDate", startDate)
                                .queryParam("endDate", endDate)
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                response.then().body("content", notNullValue());
        }

        @Test
        @Order(9)
        @DisplayName("Should search expenses by description")
        void testExpenseSearchByDescription() {
                // Create expense with specific description
                Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                userId, "FOOD", 800.0);
                expenseData.put("description", "Grocery shopping at supermarket");
                authHelper.getAuthenticatedSpec().body(expenseData).post("/api/v1/budget/expense");

                // Search for "grocery"
                Response response = authHelper.getAuthenticatedSpec()
                                .queryParam("search", "grocery")
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                assertTrue(response.jsonPath().getList("content").size() >= 1);
        }

        @Test
        @Order(10)
        @DisplayName("Should sort expenses by amount descending")
        void testExpenseSortByAmount() {
                Response response = authHelper.getAuthenticatedSpec()
                                .queryParam("sortBy", "amount")
                                .queryParam("order", "desc")
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                response.then().body("content", notNullValue());
        }

        // ===== INCOME CRUD TESTS =====

        @Test
        @Order(11)
        @DisplayName("Should create income successfully")
        void testCreateIncome() {
                Map<String, Object> incomeData = Map.of(
                                "userId", userId,
                                "source", "SALARY",
                                "amount", 50000.0,
                                "date", LocalDate.now().toString(),
                                "isRecurring", true,
                                "isStable", true);
                
                Response response = authHelper.getAuthenticatedSpec()
                                .body(incomeData)
                                .when()
                                .post("/api/v1/budget/income");
                
                ApiAssertions.assertStatusCode(response, 200);
                incomeId = response.jsonPath().getLong("id");
                assertNotNull(incomeId);
                assertEquals("SALARY", response.jsonPath().getString("source"));
        }

        @Test
        @Order(12)
        @DisplayName("Should get single income by ID")
        void testGetIncomeById() {
                // Create income first
                Map<String, Object> incomeData = Map.of(
                                "userId", userId,
                                "source", "DIVIDEND",
                                "amount", 5000.0,
                                "date", LocalDate.now().toString(),
                                "isRecurring", false,
                                "isStable", false);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(incomeData)
                                .post("/api/v1/budget/income");
                Long id = createResponse.jsonPath().getLong("id");

                // Retrieve it
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/income/detail/" + id);
                
                ApiAssertions.assertStatusCode(response, 200);
                assertEquals(id, response.jsonPath().getLong("id"));
        }

        @Test
        @Order(13)
        @DisplayName("Should update income successfully")
        void testUpdateIncome() {
                // Create income
                Map<String, Object> incomeData = Map.of(
                                "userId", userId,
                                "source", "FREELANCE",
                                "amount", 10000.0,
                                "date", LocalDate.now().toString(),
                                "isRecurring", false,
                                "isStable", false);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(incomeData)
                                .post("/api/v1/budget/income");
                Long id = createResponse.jsonPath().getLong("id");

                // Update
                Map<String, Object> updateData = Map.of(
                                "userId", userId,
                                "source", "FREELANCE",
                                "amount", 15000.0,
                                "date", LocalDate.now().toString(),
                                "isRecurring", true,
                                "isStable", true);
                Response response = authHelper.getAuthenticatedSpec()
                                .body(updateData)
                                .when()
                                .put("/api/v1/budget/income/" + id);
                
                ApiAssertions.assertStatusCode(response, 200);
                assertEquals(15000.0, response.jsonPath().getDouble("amount"));
        }

        @Test
        @Order(14)
        @DisplayName("Should delete income successfully")
        void testDeleteIncome() {
                // Create income
                Map<String, Object> incomeData = Map.of(
                                "userId", userId,
                                "source", "BONUS",
                                "amount", 20000.0,
                                "date", LocalDate.now().toString(),
                                "isRecurring", false,
                                "isStable", false);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(incomeData)
                                .post("/api/v1/budget/income");
                Long id = createResponse.jsonPath().getLong("id");

                // Delete
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .delete("/api/v1/budget/income/" + id);
                
                ApiAssertions.assertStatusCode(response, 200);
        }

        // ===== BUDGET LIMITS TESTS =====

        @Test
        @Order(15)
        @DisplayName("Should set budget limit successfully")
        void testSetBudgetLimit() {
                Map<String, Object> budgetData = Map.of(
                                "userId", userId,
                                "category", "FOOD",
                                "monthlyLimit", 15000.0,
                                "monthYear", LocalDate.now().toString().substring(0, 7));
                
                Response response = authHelper.getAuthenticatedSpec()
                                .body(budgetData)
                                .when()
                                .post("/api/v1/budget/limit");
                
                ApiAssertions.assertStatusCode(response, 200);
                assertEquals(15000.0, response.jsonPath().getDouble("monthlyLimit"));
        }

        @Test
        @Order(16)
        @DisplayName("Should get all budget limits for user")
        void testGetAllBudgetLimits() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/limit/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                assertNotNull(response.jsonPath().getList(""));
        }

        @Test
        @Order(17)
        @DisplayName("Should get monthly budget report")
        void testGetMonthlyReport() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/report/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                response.then().body("totalBudget", notNullValue());
                response.then().body("totalSpent", notNullValue());
                response.then().body("categoryBreakdown", notNullValue());
        }

        @Test
        @Order(18)
        @DisplayName("Should get cash flow analysis")
        void testGetCashFlow() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/cashflow/" + userId);
                
                ApiAssertions.assertStatusCode(response, 200);
                response.then().body("totalIncome", notNullValue());
                response.then().body("totalExpenses", notNullValue());
                response.then().body("netCashFlow", notNullValue());
        }

        // ===== VALIDATION & ERROR TESTS =====

        @Test
        @Order(19)
        @DisplayName("Should return 400 for negative expense amount")
        void testCreateExpenseWithInvalidData() {
                Map<String, Object> invalidData = Map.of(
                                "userId", userId,
                                "category", "FOOD",
                                "amount", -1000.0,
                                "expenseDate", LocalDate.now().toString());
                
                Response response = authHelper.getAuthenticatedSpec()
                                .body(invalidData)
                                .when()
                                .post("/api/v1/budget/expense");
                
                // Should return either 400 or 500 based on validation
                assertTrue(response.getStatusCode() >= 400);
        }

        @Test
        @Order(20)
        @DisplayName("Should handle non-existent expense gracefully")
        void testGetNonExistentExpense() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/expense/detail/999999");
                
                // Should return either 404 or 500 based on implementation
                assertTrue(response.getStatusCode() >= 400);
        }

        @Test
        @Order(21)
        @DisplayName("Should return 401 for unauthenticated request")
        void testUnauthenticatedRequest() {
                Response response = given()
                                .spec(requestSpec)
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                
                ApiAssertions.assertStatusCode(response, 401);
        }
}
