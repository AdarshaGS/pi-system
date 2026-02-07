package com.savings.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Recurring Deposit Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecurringDepositControllerTest extends BaseApiTest {

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
        rd.put("monthlyInstallment", monthlyAmount);
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
        authHelper.getAuthenticatedSpec()
                .body(createRDData("SBI Bank", 3000.0))
                .post("/api/v1/recurring-deposit");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/recurring-deposit/user/" + userId);

        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get recurring deposit by ID")
    void testGetRecurringDeposit() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("ICICI Bank", 2000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "id", rdId);
    }

    @Test
    @Order(4)
    @DisplayName("Should update recurring deposit successfully")
    void testUpdateRecurringDeposit() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("Axis Bank", 4000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        Map<String, Object> updatedData = createRDData("Axis Bank", 4500.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(updatedData)
                .when()
                .put("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(5)
    @DisplayName("Should delete recurring deposit successfully")
    void testDeleteRecurringDeposit() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createRDData("Kotak Bank", 1000.0))
                .post("/api/v1/recurring-deposit");
        Long rdId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/recurring-deposit/" + rdId + "?userId=" + userId);

        ApiAssertions.assertStatusCode(response, 200);
    }
}
