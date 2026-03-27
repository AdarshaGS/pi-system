package com.api.networth;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;

import io.restassured.response.Response;

/**
 * Integration tests for NetWorth Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NetWorthControllerIntegrationTest extends BaseApiTest {

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

    @Test
    @Order(1)
    @DisplayName("Should get net worth summary")
    void testGetNetWorthSummary() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/net-worth/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "totalAssets");
        ApiAssertions.assertFieldExists(response, "totalLiabilities");
    }
}
