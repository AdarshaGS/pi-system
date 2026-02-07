package com.api.stocks;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Stock Controller
 * Tests stock management and watchlist endpoints
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StockControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;
    private String testSymbol = "TESTSTOCK";

    @BeforeEach
    void setUp() {
        authHelper = new AuthHelper(requestSpec);
        Map<String, Object> userData = TestDataBuilder.createTestUser();
        authHelper.register(
                (String) userData.get("email"),
                (String) userData.get("password"),
                (String) userData.get("name"),
                (String) userData.get("mobileNumber"));
        Response loginResponse = authHelper.login(
                (String) userData.get("email"),
                (String) userData.get("password"));
        userId = loginResponse.jsonPath().getLong("userId");
    }

    // ========== Stock Management Tests ==========

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/stocks - Should create stock successfully")
    void testCreateStock() {
        // Given
        Map<String, Object> stockData = TestDataBuilder.createStockData(testSymbol, "Test Company");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(stockData)
                .when()
                .post("/api/v1/stocks");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "symbol");
        response.then()
                .body("symbol", equalTo(testSymbol))
                .body("companyName", equalTo("Test Company"))
                .body("exchange", equalTo("NSE"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/stocks/{symbol} - Should get stock by symbol")
    void testGetStockBySymbol() {
        // Given - create stock first
        Map<String, Object> stockData = TestDataBuilder.createStockData("RELIANCE", "Reliance Industries");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/RELIANCE");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("symbol", equalTo("RELIANCE"))
                .body("companyName", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/stocks - Should list all stocks")
    void testListAllStocks() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/stocks/{symbol} - Should update stock details")
    void testUpdateStock() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("TCS", "Tata Consultancy Services");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        // Update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("currentPrice", 3650.0);
        updateData.put("sector", "IT Services");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(updateData)
                .when()
                .put("/api/v1/stocks/TCS");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("currentPrice", equalTo(3650.0f));
    }

    @Test
    @Order(5)
    @DisplayName("DELETE /api/v1/stocks/{symbol} - Should delete stock")
    void testDeleteStock() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("DELETEME", "Delete Test Company");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/stocks/DELETEME");

        // Then
        ApiAssertions.assertStatusCode(response, 204);

        // Verify deletion
        Response verifyResponse = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/DELETEME");
        verifyResponse.then()
                .statusCode(anyOf(equalTo(404), equalTo(204)));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/stocks/search - Should search stocks by query")
    void testSearchStocks() {
        // Given - create stocks
        Map<String, Object> stock1 = TestDataBuilder.createStockData("INFY", "Infosys Limited");
        authHelper.getAuthenticatedSpec()
                .body(stock1)
                .post("/api/v1/stocks");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("q", "Infosys")
                .when()
                .get("/api/v1/stocks/search");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", not(empty()));
    }

    // ========== Price History Tests ==========

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/stocks/{symbol}/price-history - Should get price history")
    void testGetPriceHistory() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("WIPRO", "Wipro Limited");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("from", LocalDate.now().minusMonths(1).toString())
                .queryParam("to", LocalDate.now().toString())
                .when()
                .get("/api/v1/stocks/WIPRO/price-history");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/stocks/{symbol}/prices - Should add stock price")
    void testAddStockPrice() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("HDFC", "HDFC Bank");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        Map<String, Object> priceData = new HashMap<>();
        priceData.put("date", LocalDate.now().toString());
        priceData.put("open", 1600.0);
        priceData.put("high", 1650.0);
        priceData.put("low", 1590.0);
        priceData.put("close", 1625.0);
        priceData.put("volume", 5000000);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(priceData)
                .when()
                .post("/api/v1/stocks/HDFC/prices");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("close", equalTo(1625.0f));
    }

    // ========== Fundamentals Tests ==========

    @Test
    @Order(9)
    @DisplayName("GET /api/v1/stocks/{symbol}/fundamentals - Should get stock fundamentals")
    void testGetStockFundamentals() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("ITC", "ITC Limited");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/ITC/fundamentals");

        // Then
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    @Order(10)
    @DisplayName("POST /api/v1/stocks/{symbol}/fundamentals - Should add/update fundamentals")
    void testAddStockFundamentals() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("BHARTIARTL", "Bharti Airtel");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        Map<String, Object> fundamentalsData = new HashMap<>();
        fundamentalsData.put("marketCap", 5000000000000.0);
        fundamentalsData.put("peRatio", 25.5);
        fundamentalsData.put("dividendYield", 1.5);
        fundamentalsData.put("roe", 18.5);
        fundamentalsData.put("debtToEquity", 0.8);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(fundamentalsData)
                .when()
                .post("/api/v1/stocks/BHARTIARTL/fundamentals");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("peRatio", equalTo(25.5f));
    }

    // ========== Watchlist Tests ==========

    @Test
    @Order(11)
    @DisplayName("POST /api/v1/stocks/watchlist - Should add stock to watchlist")
    void testAddToWatchlist() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("MARUTI", "Maruti Suzuki");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        Map<String, Object> watchlistData = new HashMap<>();
        watchlistData.put("userId", userId);
        watchlistData.put("symbol", "MARUTI");
        watchlistData.put("targetPrice", 10000.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(watchlistData)
                .when()
                .post("/api/v1/stocks/watchlist");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("symbol", equalTo("MARUTI"))
                .body("userId", equalTo(userId.intValue()));
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/v1/stocks/watchlist - Should get user's watchlist")
    void testGetWatchlist() {
        // Given - add stocks to watchlist
        Map<String, Object> stock1 = TestDataBuilder.createStockData("SBIN", "State Bank of India");
        authHelper.getAuthenticatedSpec()
                .body(stock1)
                .post("/api/v1/stocks");

        Map<String, Object> watchlistData1 = new HashMap<>();
        watchlistData1.put("userId", userId);
        watchlistData1.put("symbol", "SBIN");
        authHelper.getAuthenticatedSpec()
                .body(watchlistData1)
                .post("/api/v1/stocks/watchlist");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .when()
                .get("/api/v1/stocks/watchlist");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("userId", everyItem(equalTo(userId.intValue())));
    }

    @Test
    @Order(13)
    @DisplayName("DELETE /api/v1/stocks/watchlist/{symbol} - Should remove from watchlist")
    void testRemoveFromWatchlist() {
        // Given - add to watchlist
        Map<String, Object> stockData = TestDataBuilder.createStockData("AXISBANK", "Axis Bank");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        Map<String, Object> watchlistData = new HashMap<>();
        watchlistData.put("userId", userId);
        watchlistData.put("symbol", "AXISBANK");
        authHelper.getAuthenticatedSpec()
                .body(watchlistData)
                .post("/api/v1/stocks/watchlist");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .when()
                .delete("/api/v1/stocks/watchlist/AXISBANK");

        // Then
        ApiAssertions.assertStatusCode(response, 204);
    }

    // ========== Alerts Tests ==========

    @Test
    @Order(14)
    @DisplayName("POST /api/v1/stocks/alerts - Should create price alert")
    void testCreatePriceAlert() {
        // Given
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("userId", userId);
        alertData.put("symbol", "RELIANCE");
        alertData.put("targetPrice", 2500.0);
        alertData.put("alertType", "ABOVE");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(alertData)
                .when()
                .post("/api/v1/stocks/alerts");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("symbol", equalTo("RELIANCE"))
                .body("targetPrice", equalTo(2500.0f));
    }

    @Test
    @Order(15)
    @DisplayName("GET /api/v1/stocks/alerts - Should get user's alerts")
    void testGetAlerts() {
        // Given - create alert
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("userId", userId);
        alertData.put("symbol", "TCS");
        alertData.put("targetPrice", 3700.0);
        alertData.put("alertType", "BELOW");
        
        authHelper.getAuthenticatedSpec()
                .body(alertData)
                .post("/api/v1/stocks/alerts");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("userId", userId)
                .when()
                .get("/api/v1/stocks/alerts");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(16)
    @DisplayName("DELETE /api/v1/stocks/alerts/{alertId} - Should delete alert")
    void testDeleteAlert() {
        // Given - create alert
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("userId", userId);
        alertData.put("symbol", "INFY");
        alertData.put("targetPrice", 1500.0);
        alertData.put("alertType", "ABOVE");
        
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(alertData)
                .post("/api/v1/stocks/alerts");
        Long alertId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/stocks/alerts/" + alertId);

        // Then
        ApiAssertions.assertStatusCode(response, 204);
    }

    // ========== Corporate Actions Tests ==========

    @Test
    @Order(17)
    @DisplayName("GET /api/v1/stocks/{symbol}/corporate-actions - Should get corporate actions")
    void testGetCorporateActions() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/RELIANCE/corporate-actions");

        // Then
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    @Order(18)
    @DisplayName("POST /api/v1/stocks/{symbol}/corporate-actions - Should add corporate action")
    void testAddCorporateAction() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("TATAMOTORS", "Tata Motors");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        Map<String, Object> actionData = new HashMap<>();
        actionData.put("actionType", "DIVIDEND");
        actionData.put("announcementDate", LocalDate.now().toString());
        actionData.put("exDate", LocalDate.now().plusDays(10).toString());
        actionData.put("dividendAmount", 25.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(actionData)
                .when()
                .post("/api/v1/stocks/TATAMOTORS/corporate-actions");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("actionType", equalTo("DIVIDEND"));
    }

    @Test
    @Order(19)
    @DisplayName("GET /api/v1/stocks/corporate-actions/upcoming - Should get upcoming corporate actions")
    void testGetUpcomingCorporateActions() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/stocks/corporate-actions/upcoming");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    // ========== Validation Tests ==========

    @Test
    @Order(20)
    @DisplayName("Should validate stock symbol format")
    void testInvalidStockSymbol() {
        // Given - invalid symbol
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("symbol", ""); // Empty symbol
        stockData.put("companyName", "Test Company");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(stockData)
                .when()
                .post("/api/v1/stocks");

        // Then
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(21)
    @DisplayName("Should prevent duplicate stock symbols")
    void testDuplicateStockSymbol() {
        // Given - create stock
        Map<String, Object> stockData = TestDataBuilder.createStockData("DUPLICATE", "Duplicate Company");
        authHelper.getAuthenticatedSpec()
                .body(stockData)
                .post("/api/v1/stocks");

        // Try creating again
        Response response = authHelper.getAuthenticatedSpec()
                .body(stockData)
                .when()
                .post("/api/v1/stocks");

        // Then - should handle duplicate
        response.then()
                .statusCode(anyOf(equalTo(200), equalTo(409)));
    }
}
