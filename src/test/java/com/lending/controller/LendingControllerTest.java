package com.lending.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Lending Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LendingControllerTest extends BaseApiTest {

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

    private Map<String, Object> createLendingData(String borrowerName, double amount) {
        Map<String, Object> lending = new HashMap<>();
        lending.put("userId", userId);
        lending.put("borrowerName", borrowerName);
        lending.put("amount", amount);
        lending.put("interestRate", 5.0);
        lending.put("lendingDate", "2024-01-15");
        lending.put("dueDate", "2025-01-15");
        lending.put("notes", "Personal loan to friend");
        return lending;
    }

    @Test
    @Order(1)
    @DisplayName("Should create lending record successfully")
    void testAddLending() {
        Map<String, Object> lendingData = createLendingData("John Doe", 50000.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .when()
                .post("/api/v1/lending");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(2)
    @DisplayName("Should get all lendings for user")
    void testGetLendings() {
        authHelper.getAuthenticatedSpec()
                .body(createLendingData("Jane Smith", 30000.0))
                .post("/api/v1/lending");

        Response response = authHelper.getAuthenticatedSpec()
                .param("userId", userId)
                .when()
                .get("/api/v1/lending");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get lending by ID")
    void testGetLendingById() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLendingData("Bob Johnson", 25000.0))
                .post("/api/v1/lending");
        Long lendingId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/lending/" + lendingId);
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "id", lendingId);
    }

    @Test
    @Order(4)
    @DisplayName("Should add repayment to lending record")
    void testAddRepayment() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLendingData("Alice Williams", 40000.0))
                .post("/api/v1/lending");
        Long lendingId = createResponse.jsonPath().getLong("id");

        Map<String, Object> repayment = new HashMap<>();
        repayment.put("amount", 10000.0);
        repayment.put("repaymentDate", "2024-06-15");
        repayment.put("notes", "Partial payment");

        Response response = authHelper.getAuthenticatedSpec()
                .body(repayment)
                .when()
                .post("/api/v1/lending/" + lendingId + "/repayment");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(5)
    @DisplayName("Should close lending record")
    void testCloseLending() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLendingData("Charlie Brown", 20000.0))
                .post("/api/v1/lending");
        Long lendingId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .put("/api/v1/lending/" + lendingId + "/close");
        ApiAssertions.assertStatusCode(response, 200);
    }
}
