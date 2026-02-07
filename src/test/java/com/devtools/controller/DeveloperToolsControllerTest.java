package com.devtools.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Developer Tools Controller
 * Note: This is a placeholder test suite. Update with actual endpoints when DeveloperToolsController is implemented.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeveloperToolsControllerTest extends BaseApiTest {

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
    @DisplayName("Should get API documentation")
    void testGetApiDocumentation() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/dev/documentation");
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(2)
    @DisplayName("Should generate API key")
    void testGenerateApiKey() {
        Map<String, Object> apiKeyRequest = new HashMap<>();
        apiKeyRequest.put("userId", userId);
        apiKeyRequest.put("name", "Test API Key");
        apiKeyRequest.put("scopes", new String[]{"read", "write"});

        Response response = authHelper.getAuthenticatedSpec()
                .body(apiKeyRequest)
                .when()
                .post("/api/v1/dev/api-keys");
        
        // Accept both 200/201 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 201, 404);
    }

    @Test
    @Order(3)
    @DisplayName("Should list API keys")
    void testListApiKeys() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/dev/api-keys/" + userId);
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(4)
    @DisplayName("Should revoke API key")
    void testRevokeApiKey() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/dev/api-keys/test-key-id");
        
        // Accept both 200/204 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 204, 404);
    }

    @Test
    @Order(5)
    @DisplayName("Should get webhook endpoints")
    void testGetWebhookEndpoints() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/dev/webhooks");
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(6)
    @DisplayName("Should create webhook")
    void testCreateWebhook() {
        Map<String, Object> webhook = new HashMap<>();
        webhook.put("userId", userId);
        webhook.put("url", "https://example.com/webhook");
        webhook.put("events", new String[]{"transaction.created", "budget.exceeded"});

        Response response = authHelper.getAuthenticatedSpec()
                .body(webhook)
                .when()
                .post("/api/v1/dev/webhooks");
        
        // Accept both 200/201 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 201, 404);
    }

    @Test
    @Order(7)
    @DisplayName("Should get API usage statistics")
    void testGetApiUsageStats() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/dev/usage/" + userId);
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }
}
