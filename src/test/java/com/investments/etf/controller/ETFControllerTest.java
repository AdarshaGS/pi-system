package com.investments.etf.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for ETF Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ETFControllerTest extends BaseApiTest {

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

    private Map<String, Object> createETFData(String symbol, String name) {
        Map<String, Object> etf = new HashMap<>();
        etf.put("symbol", symbol);
        etf.put("name", name);
        etf.put("expenseRatio", 0.5);
        etf.put("aum", 10000000.0);
        etf.put("underlyingIndex", "NIFTY 50");
        return etf;
    }

    @Test
    @Order(1)
    @DisplayName("Should get all ETFs")
    void testGetAllEtfs() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/etf");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(2)
    @DisplayName("Should create ETF successfully")
    void testAddEtf() {
        Map<String, Object> etfData = createETFData("NIFTYBEES", "Nippon India ETF Nifty BeES");
        Response response = authHelper.getAuthenticatedSpec()
                .body(etfData)
                .when()
                .post("/api/v1/etf");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "symbol");
    }

    @Test
    @Order(3)
    @DisplayName("Should get ETF by symbol")
    void testGetEtfBySymbol() {
        Map<String, Object> etfData = createETFData("BANKBEES", "Nippon India ETF Bank BeES");
        authHelper.getAuthenticatedSpec()
                .body(etfData)
                .post("/api/v1/etf");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/etf/BANKBEES");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "symbol", "BANKBEES");
    }

    @Test
    @Order(4)
    @DisplayName("Should return 404 for non-existent ETF")
    void testGetNonExistentEtf() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/etf/NONEXISTENT");
        ApiAssertions.assertStatusCode(response, 404);
    }
}
