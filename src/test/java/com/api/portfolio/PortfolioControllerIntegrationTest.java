package com.api.portfolio;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Portfolio Controller
 * Tests stock portfolio CRUD operations
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PortfolioControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;

    @BeforeEach
    void setUp() {
        authHelper = new AuthHelper(requestSpec);
        
        // Register and login test user
        Map<String, Object> userData = TestDataBuilder.createTestUser();
        authHelper.register(
                (String) userData.get("email"),
                (String) userData.get("password"),
                (String) userData.get("firstName"),
                (String) userData.get("lastName")
        );
        
        Response loginResponse = authHelper.login(
                (String) userData.get("email"),
                (String) userData.get("password")
        );
        
        userId = loginResponse.jsonPath().getLong("user.id");
    }

    @Test
    @Order(1)
    @DisplayName("Should add stock to portfolio successfully")
    void testAddStockToPortfolio() {
        // Given
        Map<String, Object> portfolioData = TestDataBuilder.createPortfolioData(
                userId, "RELIANCE", 10, 2450.50
        );

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(portfolioData)
                .when()
                .post("/portfolio");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "symbol", "RELIANCE");
        ApiAssertions.assertFieldValue(response, "quantity", 10);
    }

    @Test
    @Order(2)
    @DisplayName("Should get user portfolio")
    void testGetUserPortfolio() {
        // Given - add stocks
        authHelper.getAuthenticatedSpec()
                .body(TestDataBuilder.createPortfolioData(userId, "TCS", 5, 3500.00))
                .post("/portfolio");
        authHelper.getAuthenticatedSpec()
                .body(TestDataBuilder.createPortfolioData(userId, "INFY", 8, 1450.75))
                .post("/portfolio");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/portfolio");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertArrayNotEmpty(response, "$");
        
        response.then()
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(3)
    @DisplayName("Should update portfolio entry")
    void testUpdatePortfolioEntry() {
        // Given - add stock first
        Map<String, Object> portfolioData = TestDataBuilder.createPortfolioData(
                userId, "HDFC", 15, 1600.00
        );
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(portfolioData)
                .post("/portfolio");
        Long portfolioId = createResponse.jsonPath().getLong("id");

        // Update quantity
        portfolioData.put("quantity", 20);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(portfolioData)
                .when()
                .put("/portfolio/" + portfolioId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "quantity", 20);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete portfolio entry")
    void testDeletePortfolioEntry() {
        // Given - add stock first
        Map<String, Object> portfolioData = TestDataBuilder.createPortfolioData(
                userId, "WIPRO", 12, 450.00
        );
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(portfolioData)
                .post("/portfolio");
        Long portfolioId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/portfolio/" + portfolioId);

        // Then
        ApiAssertions.assertStatusCode(response, 204);
    }

    @Test
    @Order(5)
    @DisplayName("Should validate stock symbol format")
    void testInvalidStockSymbol() {
        // Given
        Map<String, Object> portfolioData = TestDataBuilder.createPortfolioData(
                userId, "", 10, 100.00
        );

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(portfolioData)
                .when()
                .post("/portfolio");

        // Then
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(6)
    @DisplayName("Should validate positive quantity")
    void testNegativeQuantity() {
        // Given
        Map<String, Object> portfolioData = TestDataBuilder.createPortfolioData(
                userId, "ITC", -5, 450.00
        );

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(portfolioData)
                .when()
                .post("/portfolio");

        // Then
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(7)
    @DisplayName("Should calculate portfolio total value")
    void testPortfolioTotalValue() {
        // Given - add multiple stocks
        authHelper.getAuthenticatedSpec()
                .body(TestDataBuilder.createPortfolioData(userId, "SBIN", 20, 500.00))
                .post("/portfolio");
        authHelper.getAuthenticatedSpec()
                .body(TestDataBuilder.createPortfolioData(userId, "ICICIBANK", 15, 900.00))
                .post("/portfolio");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/portfolio/summary");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "totalValue");
        
        response.then()
                .body("totalValue", greaterThan(0.0f));
    }
}
