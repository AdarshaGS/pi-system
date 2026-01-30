package com.api.investments;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Mutual Fund Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MutualFundControllerIntegrationTest extends BaseApiTest {

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
    @DisplayName("Should get mutual fund holdings")
    @Disabled("Requires AA consent setup - will implement when AA integration tests are ready")
    void testGetMutualFundHoldings() {
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .when()
                .get("/api/v1/mutual-funds/holdings");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(2)
    @DisplayName("Should get mutual fund summary")
    @Disabled("Requires AA consent setup - will implement when AA integration tests are ready")
    void testGetMutualFundSummary() {
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .when()
                .get("/api/v1/mutual-funds/summary");
        ApiAssertions.assertStatusCode(response, 200);
    }
}
