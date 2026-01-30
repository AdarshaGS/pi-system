package com.api.loan;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

/**
 * Integration tests for Loan Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoanControllerIntegrationTest extends BaseApiTest {

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
        @DisplayName("Should create loan successfully")
        void testCreateLoan() {
                Map<String, Object> loanData = TestDataBuilder.createLoanData(
                                userId, "HOME_LOAN", 5000000.0);
                Response response = authHelper.getAuthenticatedSpec()
                                .body(loanData)
                                .when()
                                .post("/api/v1/loans/create");
                ApiAssertions.assertStatusCode(response, 200);
        }

        @Test
        @Order(2)
        @DisplayName("Should get all user loans")
        void testGetUserLoans() {
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/loans/user/" + userId);
                ApiAssertions.assertStatusCode(response, 200);
        }
}
