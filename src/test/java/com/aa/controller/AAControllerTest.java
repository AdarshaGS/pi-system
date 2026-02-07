package com.aa.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Account Aggregator Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AAControllerTest extends BaseApiTest {

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

    @Test
    @Order(1)
    @DisplayName("Should get consent templates")
    void testGetTemplates() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/aa/consent/templates");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(2)
    @DisplayName("Should create consent")
    void testCreateConsent() {
        Map<String, Object> consentRequest = new HashMap<>();
        consentRequest.put("userId", userId.toString());
        consentRequest.put("purpose", "WEALTH_MANAGEMENT");
        consentRequest.put("fiTypes", new String[]{"DEPOSIT", "TERM_DEPOSIT"});
        consentRequest.put("dataRange", Map.of(
            "from", "2023-01-01",
            "to", "2024-12-31"
        ));

        Response response = authHelper.getAuthenticatedSpec()
                .body(consentRequest)
                .when()
                .post("/api/v1/aa/consent");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "consentId");
    }

    @Test
    @Order(3)
    @DisplayName("Should get consent status")
    void testGetConsentStatus() {
        // First create a consent
        Map<String, Object> consentRequest = new HashMap<>();
        consentRequest.put("userId", userId.toString());
        consentRequest.put("purpose", "WEALTH_MANAGEMENT");
        consentRequest.put("fiTypes", new String[]{"DEPOSIT"});
        consentRequest.put("dataRange", Map.of(
            "from", "2023-01-01",
            "to", "2024-12-31"
        ));

        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(consentRequest)
                .post("/api/v1/aa/consent");
        String consentId = createResponse.jsonPath().getString("consentId");

        // Get consent status
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/aa/consent/" + consentId + "/status");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(4)
    @DisplayName("Should get user consents")
    void testGetUserConsents() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/aa/consents/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }
}
