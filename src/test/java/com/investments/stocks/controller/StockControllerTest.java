package com.investments.stocks.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Stock Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StockControllerTest extends BaseApiTest {

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
    @DisplayName("Should get stock by symbol")
    void testGetStockBySymbol() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/AAPL");
        // May return 404 if stock doesn't exist
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(2)
    @DisplayName("Should get all stocks")
    void testGetAllStocks() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should search stocks")
    void testSearchStocks() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("query", "Apple")
                .when()
                .get("/api/v1/stocks/search");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(4)
    @DisplayName("Should get stock price")
    void testGetStockPrice() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/AAPL/price");
        // May return 404 if stock doesn't exist
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(5)
    @DisplayName("Should get stock history")
    void testGetStockHistory() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("symbol", "AAPL")
                .param("period", "1M")
                .when()
                .get("/api/v1/stocks/history");
        ApiAssertions.assertStatusCode(response, 200, 404);
    }
}
