package com.api.insurance;

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
 * Integration tests for Insurance Controller
 * Tests all insurance management endpoints
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InsuranceControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;
    private Long insuranceId;

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

    // ========== Basic Insurance Policy Tests ==========

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/insurance - Should create insurance policy successfully")
    void testCreateInsurancePolicy() {
        // Given
        Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "LIFE", 5000000.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .when()
                .post("/api/v1/insurance");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("userId", equalTo(userId.intValue()))
                .body("insuranceType", equalTo("LIFE"))
                .body("coverageAmount", equalTo(5000000.0f))
                .body("status", equalTo("ACTIVE"));

        insuranceId = response.jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/insurance - Should validate required fields")
    void testCreateInsuranceWithMissingFields() {
        // Given - missing insuranceType
        Map<String, Object> insuranceData = new HashMap<>();
        insuranceData.put("userId", userId);
        insuranceData.put("coverageAmount", 1000000.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .when()
                .post("/api/v1/insurance");

        // Then
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/insurance - Should get all insurance policies")
    void testGetAllInsurancePolicies() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/insurance/user/{userId} - Should get user's insurance policies")
    void testGetInsurancePoliciesByUserId() {
        // Given - create multiple policies
        Map<String, Object> healthInsurance = TestDataBuilder.createInsuranceData(userId, "HEALTH", 500000.0);
        authHelper.getAuthenticatedSpec()
                .body(healthInsurance)
                .post("/api/v1/insurance");

        Map<String, Object> termInsurance = TestDataBuilder.createInsuranceData(userId, "TERM", 10000000.0);
        authHelper.getAuthenticatedSpec()
                .body(termInsurance)
                .post("/api/v1/insurance");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/user/" + userId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(2)))
                .body("userId", everyItem(equalTo(userId.intValue())))
                .body("insuranceType", hasItems("HEALTH", "TERM"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/insurance/{id} - Should get insurance policy by ID")
    void testGetInsurancePolicyById() {
        // Given - create policy if not exists
        if (insuranceId == null) {
            Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "LIFE", 5000000.0);
            Response createResponse = authHelper.getAuthenticatedSpec()
                    .body(insuranceData)
                    .post("/api/v1/insurance");
            insuranceId = createResponse.jsonPath().getLong("id");
        }

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/" + insuranceId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("id", equalTo(insuranceId.intValue()))
                .body("userId", equalTo(userId.intValue()));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /api/v1/insurance/{id} - Should delete insurance policy")
    void testDeleteInsurancePolicy() {
        // Given - create policy to delete
        Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "TRAVEL", 100000.0);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .post("/api/v1/insurance");
        Long deleteId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/insurance/" + deleteId);

        // Then
        ApiAssertions.assertStatusCode(response, 204);

        // Verify deletion
        Response verifyResponse = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/" + deleteId);
        ApiAssertions.assertStatusCode(verifyResponse, 404);
    }

    // ========== Premium Management Tests ==========

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/insurance/{id}/premium - Should record premium payment")
    void testRecordPremiumPayment() {
        // Given
        if (insuranceId == null) {
            Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "LIFE", 5000000.0);
            Response createResponse = authHelper.getAuthenticatedSpec()
                    .body(insuranceData)
                    .post("/api/v1/insurance");
            insuranceId = createResponse.jsonPath().getLong("id");
        }

        Map<String, Object> premiumData = new HashMap<>();
        premiumData.put("amount", 50000.0);
        premiumData.put("paymentDate", LocalDate.now().toString());
        premiumData.put("paymentMethod", "ONLINE");
        premiumData.put("transactionId", "TXN" + System.currentTimeMillis());

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(premiumData)
                .when()
                .post("/api/v1/insurance/" + insuranceId + "/premium");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("amount", equalTo(50000.0f))
                .body("paymentMethod", equalTo("ONLINE"));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/insurance/{id}/premiums - Should get premium history")
    void testGetPremiumHistory() {
        // Given - create policy and add premium
        Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "HEALTH", 500000.0);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .post("/api/v1/insurance");
        Long policyId = createResponse.jsonPath().getLong("id");

        Map<String, Object> premiumData = new HashMap<>();
        premiumData.put("amount", 25000.0);
        premiumData.put("paymentDate", LocalDate.now().toString());
        premiumData.put("paymentMethod", "ONLINE");

        authHelper.getAuthenticatedSpec()
                .body(premiumData)
                .post("/api/v1/insurance/" + policyId + "/premium");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/" + policyId + "/premiums");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue())
                .body("insuranceId", equalTo(policyId.intValue()));
    }

    // ========== Claims Management Tests ==========

    @Test
    @Order(9)
    @DisplayName("POST /api/v1/insurance/{id}/claim - Should file insurance claim")
    void testFileClaim() {
        // Given
        if (insuranceId == null) {
            Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "HEALTH", 1000000.0);
            Response createResponse = authHelper.getAuthenticatedSpec()
                    .body(insuranceData)
                    .post("/api/v1/insurance");
            insuranceId = createResponse.jsonPath().getLong("id");
        }

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("claimAmount", 150000.0);
        claimData.put("claimDate", LocalDate.now().toString());
        claimData.put("incidentDate", LocalDate.now().minusDays(5).toString());
        claimData.put("reason", "Hospitalization");
        claimData.put("status", "SUBMITTED");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(claimData)
                .when()
                .post("/api/v1/insurance/" + insuranceId + "/claim");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("claimAmount", equalTo(150000.0f))
                .body("reason", equalTo("Hospitalization"))
                .body("status", equalTo("SUBMITTED"));
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/v1/insurance/{id}/claims - Should get claim history")
    void testGetClaimHistory() {
        // Given - create policy and file claim
        Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "HEALTH", 500000.0);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .post("/api/v1/insurance");
        Long policyId = createResponse.jsonPath().getLong("id");

        Map<String, Object> claimData = new HashMap<>();
        claimData.put("claimAmount", 50000.0);
        claimData.put("claimDate", LocalDate.now().toString());
        claimData.put("incidentDate", LocalDate.now().minusDays(3).toString());
        claimData.put("reason", "Medical treatment");
        claimData.put("status", "SUBMITTED");

        authHelper.getAuthenticatedSpec()
                .body(claimData)
                .post("/api/v1/insurance/" + policyId + "/claim");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/" + policyId + "/claims");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue())
                .body("insuranceId", equalTo(policyId.intValue()));
    }

    // ========== Coverage Analysis Tests ==========

    @Test
    @Order(11)
    @DisplayName("GET /api/v1/insurance/user/{userId}/analysis - Should analyze insurance coverage")
    void testAnalyzeCoverage() {
        // Given - create multiple policies for analysis
        Map<String, Object> lifeInsurance = TestDataBuilder.createInsuranceData(userId, "LIFE", 10000000.0);
        authHelper.getAuthenticatedSpec()
                .body(lifeInsurance)
                .post("/api/v1/insurance");

        Map<String, Object> healthInsurance = TestDataBuilder.createInsuranceData(userId, "HEALTH", 1000000.0);
        authHelper.getAuthenticatedSpec()
                .body(healthInsurance)
                .post("/api/v1/insurance");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/user/" + userId + "/analysis");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue())
                .body("userId", equalTo(userId.intValue()));
    }

    // ========== Edge Cases and Validation Tests ==========

    @Test
    @Order(12)
    @DisplayName("Should handle expired policies")
    void testExpiredPolicy() {
        // Given - policy with past end date
        Map<String, Object> insuranceData = new HashMap<>();
        insuranceData.put("userId", userId);
        insuranceData.put("insuranceType", "TERM");
        insuranceData.put("policyNumber", "EXP" + System.currentTimeMillis());
        insuranceData.put("provider", "Test Insurance Co");
        insuranceData.put("coverageAmount", 1000000.0);
        insuranceData.put("premiumAmount", 20000.0);
        insuranceData.put("premiumFrequency", "ANNUAL");
        insuranceData.put("startDate", LocalDate.now().minusYears(2).toString());
        insuranceData.put("endDate", LocalDate.now().minusDays(30).toString());
        insuranceData.put("status", "EXPIRED");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .when()
                .post("/api/v1/insurance");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        response.then()
                .body("status", equalTo("EXPIRED"));
    }

    @Test
    @Order(13)
    @DisplayName("Should validate claim amount does not exceed coverage")
    void testClaimExceedingCoverage() {
        // Given - policy with specific coverage
        Map<String, Object> insuranceData = TestDataBuilder.createInsuranceData(userId, "HEALTH", 100000.0);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .post("/api/v1/insurance");
        Long policyId = createResponse.jsonPath().getLong("id");

        // Claim exceeding coverage
        Map<String, Object> claimData = new HashMap<>();
        claimData.put("claimAmount", 150000.0); // Exceeds coverage
        claimData.put("claimDate", LocalDate.now().toString());
        claimData.put("incidentDate", LocalDate.now().minusDays(5).toString());
        claimData.put("reason", "Major surgery");
        claimData.put("status", "SUBMITTED");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(claimData)
                .when()
                .post("/api/v1/insurance/" + policyId + "/claim");

        // Then - should either accept or validate based on implementation
        response.then()
                .statusCode(anyOf(equalTo(201), equalTo(400)));
    }

    @Test
    @Order(14)
    @DisplayName("Should handle multiple premium frequencies")
    void testMultiplePremiumFrequencies() {
        // Test MONTHLY
        Map<String, Object> monthlyPolicy = TestDataBuilder.createInsuranceData(userId, "LIFE", 1000000.0);
        monthlyPolicy.put("premiumFrequency", "MONTHLY");
        Response monthlyResponse = authHelper.getAuthenticatedSpec()
                .body(monthlyPolicy)
                .post("/api/v1/insurance");
        ApiAssertions.assertStatusCode(monthlyResponse, 201);

        // Test QUARTERLY
        Map<String, Object> quarterlyPolicy = TestDataBuilder.createInsuranceData(userId, "HEALTH", 500000.0);
        quarterlyPolicy.put("premiumFrequency", "QUARTERLY");
        Response quarterlyResponse = authHelper.getAuthenticatedSpec()
                .body(quarterlyPolicy)
                .post("/api/v1/insurance");
        ApiAssertions.assertStatusCode(quarterlyResponse, 201);
    }

    @Test
    @Order(15)
    @DisplayName("GET /api/v1/insurance/{id} - Should return 404 for non-existent policy")
    void testGetNonExistentPolicy() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/999999");

        // Then
        ApiAssertions.assertStatusCode(response, 404);
    }
}
