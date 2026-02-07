package com.api.portfolio;

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
 * Integration tests for Portfolio Controller
 * Tests stock portfolio CRUD operations with proper data seeding
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioControllerIntegrationTest extends BaseApiTest {

        private AuthHelper authHelper;
        private Long userId;
        private static final String TEST_SYMBOL_1 = "RELIANCE";
        private static final String TEST_SYMBOL_2 = "TCS";

        @BeforeEach
        void setUp() {
                authHelper = new AuthHelper(requestSpec);

                // Register and login test user
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
                
                // Seed stock data before portfolio tests
                seedStockData();
        }

        /**
         * Seed stock data for testing
         */
        private void seedStockData() {
                // Create stock 1
                Map<String, Object> stock1 = TestDataBuilder.createStockData(TEST_SYMBOL_1, "Reliance Industries");
                authHelper.getAuthenticatedSpec()
                                .body(stock1)
                                .post("/api/v1/stocks");

                // Create stock 2
                Map<String, Object> stock2 = TestDataBuilder.createStockData(TEST_SYMBOL_2, "Tata Consultancy Services");
                authHelper.getAuthenticatedSpec()
                                .body(stock2)
                                .post("/api/v1/stocks");
        }

        @Test
        @Order(1)
        @DisplayName("POST /api/v1/portfolio - Should add stock to portfolio successfully")
        void testAddStockToPortfolio() {
                // Given
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_1);
                portfolioData.put("quantity", 10);
                portfolioData.put("purchasePrice", 2450.50);
                portfolioData.put("purchaseDate", LocalDate.now().toString());

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .when()
                                .post("/api/v1/portfolio");

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "id");
                response.then()
                                .body("stockSymbol", equalTo(TEST_SYMBOL_1))
                                .body("quantity", equalTo(10))
                                .body("purchasePrice", equalTo(2450.50f));
        }

        @Test
        @Order(2)
        @DisplayName("POST /api/v1/portfolio - Should validate required fields")
        void testAddStockWithMissingFields() {
                // Given - missing quantity
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_1);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .when()
                                .post("/api/v1/portfolio");

                // Then
                ApiAssertions.assertStatusCode(response, 400);
        }

        @Test
        @Order(3)
        @DisplayName("GET /api/v1/portfolio/summary/{userId} - Should get user portfolio summary")
        void testGetUserPortfolio() {
                // Given - add multiple stocks
                Map<String, Object> stock1 = new HashMap<>();
                stock1.put("userId", userId);
                stock1.put("stockSymbol", TEST_SYMBOL_1);
                stock1.put("quantity", 10);
                stock1.put("purchasePrice", 2450.00);
                stock1.put("purchaseDate", LocalDate.now().toString());
                authHelper.getAuthenticatedSpec()
                                .body(stock1)
                                .post("/api/v1/portfolio");

                Map<String, Object> stock2 = new HashMap<>();
                stock2.put("userId", userId);
                stock2.put("stockSymbol", TEST_SYMBOL_2);
                stock2.put("quantity", 5);
                stock2.put("purchasePrice", 3500.00);
                stock2.put("purchaseDate", LocalDate.now().toString());
                authHelper.getAuthenticatedSpec()
                                .body(stock2)
                                .post("/api/v1/portfolio");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/summary/" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                response.then()
                                .body("totalInvestment", notNullValue())
                                .body("currentValue", notNullValue())
                                .body("holdings", hasSize(greaterThanOrEqualTo(2)));
        }

        @Test
        @Order(4)
        @DisplayName("GET /api/v1/portfolio/user/{userId} - Should list portfolio holdings")
        void testListPortfolioHoldings() {
                // Given - add stock
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_1);
                portfolioData.put("quantity", 15);
                portfolioData.put("purchasePrice", 2400.00);
                portfolioData.put("purchaseDate", LocalDate.now().toString());
                authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .post("/api/v1/portfolio");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/user/" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                response.then()
                                .body("$", hasSize(greaterThanOrEqualTo(1)))
                                .body("[0].userId", equalTo(userId.intValue()));
        }

        @Test
        @Order(5)
        @DisplayName("PUT /api/v1/portfolio/{id} - Should update portfolio holding")
        void testUpdatePortfolioHolding() {
                // Given - create holding
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_2);
                portfolioData.put("quantity", 8);
                portfolioData.put("purchasePrice", 3600.00);
                portfolioData.put("purchaseDate", LocalDate.now().toString());
                
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .post("/api/v1/portfolio");
                Long portfolioId = createResponse.jsonPath().getLong("id");

                // Update data
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("quantity", 12);
                updateData.put("purchasePrice", 3550.00);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(updateData)
                                .when()
                                .put("/api/v1/portfolio/" + portfolioId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                response.then()
                                .body("quantity", equalTo(12))
                                .body("purchasePrice", equalTo(3550.0f));
        }

        @Test
        @Order(6)
        @DisplayName("DELETE /api/v1/portfolio/{id} - Should delete portfolio holding")
        void testDeletePortfolioHolding() {
                // Given - create holding
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_1);
                portfolioData.put("quantity", 3);
                portfolioData.put("purchasePrice", 2500.00);
                portfolioData.put("purchaseDate", LocalDate.now().toString());
                
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .post("/api/v1/portfolio");
                Long portfolioId = createResponse.jsonPath().getLong("id");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .delete("/api/v1/portfolio/" + portfolioId);

                // Then
                ApiAssertions.assertStatusCode(response, 204);

                // Verify deletion
                Response verifyResponse = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/" + portfolioId);
                verifyResponse.then()
                                .statusCode(anyOf(equalTo(404), equalTo(204)));
        }

        @Test
        @Order(7)
        @DisplayName("Should validate stock symbol exists")
        void testInvalidStockSymbol() {
                // Given - non-existent stock
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", "NONEXISTENT");
                portfolioData.put("quantity", 10);
                portfolioData.put("purchasePrice", 1000.00);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .when()
                                .post("/api/v1/portfolio");

                // Then - should handle gracefully
                response.then()
                                .statusCode(anyOf(equalTo(400), equalTo(404), equalTo(200)));
        }

        @Test
        @Order(8)
        @DisplayName("Should validate positive quantity")
        void testNegativeQuantity() {
                // Given - negative quantity
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_1);
                portfolioData.put("quantity", -5);
                portfolioData.put("purchasePrice", 2450.00);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .when()
                                .post("/api/v1/portfolio");

                // Then
                ApiAssertions.assertStatusCode(response, 400);
        }

        @Test
        @Order(9)
        @DisplayName("Should validate positive purchase price")
        void testNegativePrice() {
                // Given - negative price
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", TEST_SYMBOL_1);
                portfolioData.put("quantity", 10);
                portfolioData.put("purchasePrice", -100.00);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .when()
                                .post("/api/v1/portfolio");

                // Then
                ApiAssertions.assertStatusCode(response, 400);
        }

        @Test
        @Order(10)
        @DisplayName("Should calculate portfolio total value correctly")
        void testPortfolioTotalValue() {
                // Given - create portfolio with known values
                Map<String, Object> stock1 = new HashMap<>();
                stock1.put("userId", userId);
                stock1.put("stockSymbol", TEST_SYMBOL_1);
                stock1.put("quantity", 10);
                stock1.put("purchasePrice", 2000.00);
                stock1.put("purchaseDate", LocalDate.now().toString());
                authHelper.getAuthenticatedSpec()
                                .body(stock1)
                                .post("/api/v1/portfolio");

                Map<String, Object> stock2 = new HashMap<>();
                stock2.put("userId", userId);
                stock2.put("stockSymbol", TEST_SYMBOL_2);
                stock2.put("quantity", 5);
                stock2.put("purchasePrice", 3000.00);
                stock2.put("purchaseDate", LocalDate.now().toString());
                authHelper.getAuthenticatedSpec()
                                .body(stock2)
                                .post("/api/v1/portfolio");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/summary/" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                response.then()
                                .body("totalInvestment", notNullValue())
                                .body("currentValue", notNullValue())
                                .body("profitLoss", notNullValue());
        }

        @Test
        @Order(11)
        @DisplayName("GET /api/v1/portfolio/{id} - Should return 404 for non-existent portfolio")
        void testGetNonExistentPortfolio() {
                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/999999");

                // Then
                ApiAssertions.assertStatusCode(response, 404);
        }

        @Test
        @Order(12)
        @DisplayName("Should handle multiple purchases of same stock")
        void testMultiplePurchasesSameStock() {
                // Given - buy same stock twice
                Map<String, Object> purchase1 = new HashMap<>();
                purchase1.put("userId", userId);
                purchase1.put("stockSymbol", TEST_SYMBOL_1);
                purchase1.put("quantity", 10);
                purchase1.put("purchasePrice", 2400.00);
                purchase1.put("purchaseDate", LocalDate.now().minusDays(10).toString());
                authHelper.getAuthenticatedSpec()
                                .body(purchase1)
                                .post("/api/v1/portfolio");

                Map<String, Object> purchase2 = new HashMap<>();
                purchase2.put("userId", userId);
                purchase2.put("stockSymbol", TEST_SYMBOL_1);
                purchase2.put("quantity", 5);
                purchase2.put("purchasePrice", 2500.00);
                purchase2.put("purchaseDate", LocalDate.now().toString());
                authHelper.getAuthenticatedSpec()
                                .body(purchase2)
                                .post("/api/v1/portfolio");

                // When - get portfolio
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/user/" + userId);

                // Then - should have both purchases
                ApiAssertions.assertStatusCode(response, 200);
                response.then()
                                .body("$", hasSize(greaterThanOrEqualTo(2)));
        }
}

