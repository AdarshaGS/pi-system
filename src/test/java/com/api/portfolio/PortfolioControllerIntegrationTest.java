package com.api.portfolio;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
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
        @DisplayName("Should add stock to portfolio successfully")
        @Disabled("Requires stock data in database - will implement after stock seeding")
        void testAddStockToPortfolio() {
                // Given
                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("userId", userId);
                portfolioData.put("stockSymbol", "RELIANCE");
                portfolioData.put("quantity", 10);
                portfolioData.put("purchasePrice", 2450.50);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(portfolioData)
                                .when()
                                .post("/api/v1/portfolio");

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "id");
                ApiAssertions.assertFieldValue(response, "stockSymbol", "RELIANCE");
                ApiAssertions.assertFieldValue(response, "quantity", 10);
        }

        @Test
        @Order(2)
        @DisplayName("Should get user portfolio")
        @Disabled("Requires stock data in database - will implement after stock seeding")
        void testGetUserPortfolio() {
                // Given - add stocks
                Map<String, Object> stock1 = new HashMap<>();
                stock1.put("userId", userId);
                stock1.put("stockSymbol", "TCS");
                stock1.put("quantity", 5);
                stock1.put("purchasePrice", 3500.00);
                authHelper.getAuthenticatedSpec()
                                .body(stock1)
                                .post("/api/v1/portfolio");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/portfolio/summary/" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "totalInvestment");
        }

        @Test
        @Order(3)
        @DisplayName("Should validate stock symbol format")
        @Disabled("Requires stock data in database - will implement after stock seeding")
        void testInvalidStockSymbol() {
                // Skip this test until we have stock seeding
        }

        @Test
        @Order(4)
        @DisplayName("Should validate positive quantity")
        @Disabled("Requires stock data in database - will implement after stock seeding")
        void testNegativeQuantity() {
                // Skip this test until we have stock seeding
        }

        @Test
        @Order(5)
        @DisplayName("Should calculate portfolio total value")
        @Disabled("Requires stock data in database - will implement after stock seeding")
        void testPortfolioTotalValue() {
                // Skip this test until we have stock seeding
        }
}
