package com.investments.mutualfunds.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Mutual Fund Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MutualFundControllerTest extends BaseApiTest {

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
    @DisplayName("Should get mutual fund summary")
    void testGetSummary() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("userId", userId)
                .when()
                .get("/api/v1/mutual-funds/summary");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(2)
    @DisplayName("Should get mutual fund holdings")
    void testGetHoldings() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("userId", userId)
                .when()
                .get("/api/v1/mutual-funds/holdings");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get mutual fund insights")
    void testGetInsights() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("userId", userId)
                .when()
                .get("/api/v1/mutual-funds/insights");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(4)
    @DisplayName("Should search mutual fund schemes")
    void testSearchSchemes() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("arg0", "HDFC")
                .when()
                .get("/api/v1/mutual-funds/external/search");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(5)
    @DisplayName("Should reject empty search query")
    void testSearchSchemesWithEmptyQuery() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("arg0", "")
                .when()
                .get("/api/v1/mutual-funds/external/search");
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(6)
    @DisplayName("Should get all schemes")
    void testGetAllSchemes() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/mutual-funds/external/schemes");
        ApiAssertions.assertStatusCode(response, 200);
    }
}
