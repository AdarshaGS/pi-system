package com.api.budget;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

/**
 * Integration tests for Budget Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetControllerIntegrationTest extends BaseApiTest {

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
        @DisplayName("Should create expense")
        void testCreateExpense() {
                Map<String, Object> expenseData = TestDataBuilder.createExpenseData(
                                userId, "FOOD", 5000.0);
                Response response = authHelper.getAuthenticatedSpec()
                                .body(expenseData)
                                .when()
                                .post("/api/v1/budget/expense");
                ApiAssertions.assertStatusCode(response, 200);
        }

        @Test
        @Order(2)
        @DisplayName("Should get user expenses")
        void testGetUserExpenses() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/budget/expense/" + userId);
                ApiAssertions.assertStatusCode(response, 200);
        }
}
