package com.protection.insurance.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Insurance Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InsuranceControllerTest extends BaseApiTest {

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

    private Map<String, Object> createInsuranceData(String policyType, String providerName) {
        Map<String, Object> insurance = new HashMap<>();
        insurance.put("userId", userId);
        insurance.put("policyType", policyType);
        insurance.put("providerName", providerName);
        insurance.put("policyNumber", "POL" + System.currentTimeMillis());
        insurance.put("coverageAmount", 1000000.0);
        insurance.put("premiumAmount", 25000.0);
        insurance.put("premiumFrequency", "ANNUAL");
        insurance.put("startDate", "2024-01-01");
        insurance.put("endDate", "2034-01-01");
        return insurance;
    }

    @Test
    @Order(1)
    @DisplayName("Should create insurance policy successfully")
    void testCreateInsurance() {
        Map<String, Object> insuranceData = createInsuranceData("LIFE", "LIC");
        Response response = authHelper.getAuthenticatedSpec()
                .body(insuranceData)
                .when()
                .post("/api/v1/insurance");
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(2)
    @DisplayName("Should get all insurance policies")
    void testGetAllInsurance() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get insurance policies by user ID")
    void testGetInsuranceByUserId() {
        authHelper.getAuthenticatedSpec()
                .body(createInsuranceData("HEALTH", "Star Health"))
                .post("/api/v1/insurance");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/user/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(4)
    @DisplayName("Should get insurance policy by ID")
    void testGetInsuranceById() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createInsuranceData("TERM", "HDFC Life"))
                .post("/api/v1/insurance");
        Long insuranceId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/insurance/" + insuranceId);
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "id", insuranceId);
    }

    @Test
    @Order(5)
    @DisplayName("Should delete insurance policy")
    void testDeleteInsurance() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createInsuranceData("MOTOR", "ICICI Lombard"))
                .post("/api/v1/insurance");
        Long insuranceId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/insurance/" + insuranceId);
        ApiAssertions.assertStatusCode(response, 204);
    }

    @Test
    @Order(6)
    @DisplayName("Should record premium payment")
    void testRecordPremium() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createInsuranceData("LIFE", "SBI Life"))
                .post("/api/v1/insurance");
        Long insuranceId = createResponse.jsonPath().getLong("id");

        Map<String, Object> premium = new HashMap<>();
        premium.put("policyId", insuranceId);
        premium.put("amount", 25000.0);
        premium.put("paymentDate", "2024-03-15");
        premium.put("paymentMethod", "ONLINE");

        Response response = authHelper.getAuthenticatedSpec()
                .body(premium)
                .when()
                .post("/api/v1/insurance/premium");
        ApiAssertions.assertStatusCode(response, 201, 200);
    }
}
