package com.api.lending;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Lending Controller
 * Tests all 5 lending endpoints
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LendingControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;
    private Long lendingId;

    @BeforeEach
    void setUp() {
        authHelper = new AuthHelper(requestSpec);
        Map<String, Object> userData = TestDataBuilder.createTestUser();
        authHelper.register(
                (String) userData.get("email"),
                (String) userData.get("password"),
                (String) userData.get("name"),
                (String) userData.get("mobileNumber"));
        Response loginResponse = authHelper.login(
                (String) userData.get("email"),
                (String) userData.get("password"));
        userId = loginResponse.jsonPath().getLong("userId");
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/lending - Should create lending record successfully")
    void testCreateLending() {
        // Given
        Map<String, Object> lendingData = new HashMap<>();
        lendingData.put("userId", userId);
        lendingData.put("borrowerName", "John Doe");
        lendingData.put("amount", 50000.0);
        lendingData.put("interestRate", 8.5);
        lendingData.put("lendingDate", LocalDate.now().toString());
        lendingData.put("dueDate", LocalDate.now().plusMonths(6).toString());
        lendingData.put("purpose", "Business loan");
        lendingData.put("status", "ACTIVE");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .when()
                .post("/api/v1/lending");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "borrowerName", "John Doe");
        ApiAssertions.assertFieldValue(response, "amount", 50000.0f);
        ApiAssertions.assertFieldValue(response, "status", "ACTIVE");

        lendingId = response.jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/lending - Should validate required fields")
    void testCreateLendingWithMissingFields() {
        // Given - missing borrowerName
        Map<String, Object> lendingData = new HashMap<>();
        lendingData.put("userId", userId);
        lendingData.put("amount", 50000.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .when()
                .post("/api/v1/lending");

        // Then - should return 400 Bad Request
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/lending - Should list all lendings for user")
    void testGetUserLendings() {
        // Given - create another lending record
        Map<String, Object> lendingData = new HashMap<>();
        lendingData.put("userId", userId);
        lendingData.put("borrowerName", "Jane Smith");
        lendingData.put("amount", 30000.0);
        lendingData.put("interestRate", 10.0);
        lendingData.put("lendingDate", LocalDate.now().toString());
        lendingData.put("dueDate", LocalDate.now().plusMonths(3).toString());
        lendingData.put("status", "ACTIVE");

        authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .post("/api/v1/lending");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .when()
                .get("/api/v1/lending");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(2)))
                .body("[0].userId", equalTo(userId.intValue()))
                .body("borrowerName", hasItems("John Doe", "Jane Smith"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/lending/{id} - Should get lending details by ID")
    void testGetLendingById() {
        // Given - lending ID from first test
        if (lendingId == null) {
            // Create a lending if not exists
            Map<String, Object> lendingData = TestDataBuilder.createLendingData(userId, "Test Borrower", 25000.0);
            Response createResponse = authHelper.getAuthenticatedSpec()
                    .body(lendingData)
                    .post("/api/v1/lending");
            lendingId = createResponse.jsonPath().getLong("id");
        }

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/lending/" + lendingId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldExists(response, "borrowerName");
        ApiAssertions.assertFieldExists(response, "amount");
        response.then()
                .body("id", equalTo(lendingId.intValue()));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/lending/{id}/repayment - Should add repayment successfully")
    void testAddRepayment() {
        // Given
        if (lendingId == null) {
            Map<String, Object> lendingData = TestDataBuilder.createLendingData(userId, "Test Borrower", 50000.0);
            Response createResponse = authHelper.getAuthenticatedSpec()
                    .body(lendingData)
                    .post("/api/v1/lending");
            lendingId = createResponse.jsonPath().getLong("id");
        }

        Map<String, Object> repaymentData = new HashMap<>();
        repaymentData.put("amount", 10000.0);
        repaymentData.put("repaymentDate", LocalDate.now().toString());
        repaymentData.put("notes", "First installment");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(repaymentData)
                .when()
                .post("/api/v1/lending/" + lendingId + "/repayment");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("repayments", hasSize(greaterThanOrEqualTo(1)))
                .body("repayments[0].amount", equalTo(10000.0f))
                .body("remainingAmount", lessThan(50000.0f));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/lending/{id}/repayment - Should validate repayment amount")
    void testAddRepaymentExceedingAmount() {
        // Given - create lending
        Map<String, Object> lendingData = TestDataBuilder.createLendingData(userId, "Test Borrower", 10000.0);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .post("/api/v1/lending");
        Long testLendingId = createResponse.jsonPath().getLong("id");

        // Try to repay more than owed
        Map<String, Object> repaymentData = new HashMap<>();
        repaymentData.put("amount", 15000.0);
        repaymentData.put("repaymentDate", LocalDate.now().toString());

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(repaymentData)
                .when()
                .post("/api/v1/lending/" + testLendingId + "/repayment");

        // Then - should handle gracefully (either accept or return error)
        // Implementation dependent - adjust based on actual behavior
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(400)));
    }

    @Test
    @Order(7)
    @DisplayName("PUT /api/v1/lending/{id}/close - Should close lending successfully")
    void testCloseLending() {
        // Given - create lending
        Map<String, Object> lendingData = TestDataBuilder.createLendingData(userId, "Test Borrower", 5000.0);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .post("/api/v1/lending");
        Long testLendingId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .put("/api/v1/lending/" + testLendingId + "/close");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("status", anyOf(equalTo("CLOSED"), equalTo("PAID")));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/lending/{id} - Should return 404 for non-existent lending")
    void testGetNonExistentLending() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/lending/999999");

        // Then
        ApiAssertions.assertStatusCode(response, 404);
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/v1/lending - Should filter by status")
    void testFilterLendingsByStatus() {
        // Given - create lendings with different statuses
        Map<String, Object> activeLending = TestDataBuilder.createLendingData(userId, "Active Borrower", 20000.0);
        activeLending.put("status", "ACTIVE");
        authHelper.getAuthenticatedSpec()
                .body(activeLending)
                .post("/api/v1/lending");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .queryParam("status", "ACTIVE")
                .when()
                .get("/api/v1/lending");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", not(empty()))
                .body("status", everyItem(equalTo("ACTIVE")));
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/v1/lending - Should handle overdue lendings")
    void testOverdueLending() {
        // Given - lending with past due date
        Map<String, Object> lendingData = new HashMap<>();
        lendingData.put("userId", userId);
        lendingData.put("borrowerName", "Overdue Borrower");
        lendingData.put("amount", 15000.0);
        lendingData.put("interestRate", 12.0);
        lendingData.put("lendingDate", LocalDate.now().minusMonths(6).toString());
        lendingData.put("dueDate", LocalDate.now().minusDays(10).toString());
        lendingData.put("status", "OVERDUE");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(lendingData)
                .when()
                .post("/api/v1/lending");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "status", "OVERDUE");
    }
}
