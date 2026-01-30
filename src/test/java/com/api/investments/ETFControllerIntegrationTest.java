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
 * Integration tests for ETF Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ETFControllerIntegrationTest extends BaseApiTest {

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
    @DisplayName("Should add ETF to portfolio")
    void testAddETF() {
        Map<String, Object> etfData = new HashMap<>();
        etfData.put("userId", userId);
        etfData.put("etfName", "Nifty BeES");
        etfData.put("name", "Nifty BeES");
        etfData.put("units", 50);
        etfData.put("buyPrice", 250.0);

        Response response = authHelper.getAuthenticatedSpec()
                .body(etfData)
                .when()
                .post("/api/v1/etf");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(2)
    @DisplayName("Should get user ETFs")
    void testGetUserETFs() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/etf");
        ApiAssertions.assertStatusCode(response, 200);
    }
}
