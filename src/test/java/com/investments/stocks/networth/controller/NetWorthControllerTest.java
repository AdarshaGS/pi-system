package com.investments.stocks.networth.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Net Worth Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NetWorthControllerTest extends BaseApiTest {

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
    @DisplayName("Should get net worth for user")
    void testGetNetWorth() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/net-worth/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "totalAssets");
        ApiAssertions.assertFieldExists(response, "totalLiabilities");
        ApiAssertions.assertFieldExists(response, "netWorth");
    }

    @Test
    @Order(2)
    @DisplayName("Should get asset and liability templates")
    void testGetTemplates() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/net-worth/template");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "assetTypes");
        ApiAssertions.assertFieldExists(response, "liabilityTypes");
    }

    @Test
    @Order(3)
    @DisplayName("Should deny access to other user's net worth")
    void testUnauthorizedAccess() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/net-worth/" + (userId + 1000));
        ApiAssertions.assertStatusCode(response, 403);
    }
}
