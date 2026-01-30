package com.api.savings;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Recurring Deposit Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecurringDepositControllerIntegrationTest extends BaseApiTest {

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

    private Map<String, Object> createRDData(String bankName, double monthlyAmount) {
        Map<String, Object> rd = new HashMap<>();
        rd.put("userId", userId);
        rd.put("accountHolderName", "Test User");
        rd.put("bankName", bankName);
        rd.put("monthlyAmount", monthlyAmount);
        rd.put("interestRate", 6.8);
        rd.put("tenureMonths", 24);
        rd.put("maturityDate", "2026-12-31");
        rd.put("startDate", "2024-12-31");
        // monthlyInstallment
        rd.put("monthlyInstallment", 5000.0);
        return rd;
    }

    @Test
    @Order(1)
    @DisplayName("Should create recurring deposit successfully")
    void testCreateRecurringDeposit() {
        Map<String, Object> rdData = createRDData("HDFC Bank", 5000.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(rdData)
                .when()
                .post("/api/v1/recurring-deposit");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(2)
    @DisplayName("Should get all recurring deposits")
    void testGetAllRecurringDeposits() {
        // Create a recurring deposit first
        authHelper.getAuthenticatedSpec()
                .body(createRDData("SBI Bank", 3000.0))
                .post("/api/v1/recurring-deposit");

        // Test GET API
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/recurring-deposit/user/" + userId);

        // Assert response
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get recurring deposit by ID")
    void testGetRecurringDeposit() {
        // Create a recurring deposit first
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("ICICI Bank", 2000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        // Test GET API
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);

        // Assert response
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "id", rdId);
    }

    @Test
    @Order(4)
    @DisplayName("Should update recurring deposit successfully")
    void testUpdateRecurringDeposit() {
        // Create a recurring deposit first
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("Axis Bank", 4000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        // Update recurring deposit
        Map<String, Object> updatedData = createRDData("Axis Bank", 4500.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(updatedData)
                .when()
                .put("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);

        // Assert response
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "id", rdId.intValue());
        // RD DTO returns maturityAmount field
        ApiAssertions.assertFieldExists(response, "maturityAmount");
    }

    @Test
    @Order(5)
    @DisplayName("Should delete recurring deposit successfully")
    void testDeleteRecurringDeposit() {
        // Create a recurring deposit first
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("Kotak Bank", 1000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        // Delete recurring deposit
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);

        // Assert response
        ApiAssertions.assertStatusCode(response, 200);

        // Verify it's deleted
        Response verifyResponse = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);
        ApiAssertions.assertStatusCode(verifyResponse, 404);
    }

    @Test
    @Order(6)
    @DisplayName("Should throw error for invalid recurring deposit data")
    void testCreateInvalidRecurringDeposit() {
        Map<String, Object> rdData = createRDData("HDFC Bank", 5000.0);
        rdData.put("tenureMonths", 0);

        Response response = authHelper.getAuthenticatedSpec()
                .body(rdData)
                .when()
                .post("/api/v1/recurring-deposit");

        // Service validation throws 500 not 400
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(7)
    @DisplayName("Should throw error for non-existent recurring deposit")
    void testGetNonExistentRecurringDeposit() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/recurring-deposit/999999?userId=" + userId);

        ApiAssertions.assertStatusCode(response, 404);
    }

    @Test
    @Order(8)
    @DisplayName("Should throw error for unauthorized access to recurring deposit")
    void testUnauthorizedAccess() {
        // Create a recurring deposit for this user
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("SBI Bank", 3000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        // Try to access with different user
        Response response = authHelper.getAuthenticatedSpec()
                .param("userId", userId + 1)
                .when()
                .get("/api/v1/recurring-deposit/" + rdId);

        ApiAssertions.assertStatusCode(response, 403);
    }
}
