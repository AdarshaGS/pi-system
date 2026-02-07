package com.loan.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Loan Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoanControllerTest extends BaseApiTest {

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

    private Map<String, Object> createLoanData(String loanType, double amount) {
        Map<String, Object> loan = new HashMap<>();
        loan.put("userId", userId);
        loan.put("loanType", loanType);
        loan.put("principalAmount", amount);
        loan.put("interestRate", 8.5);
        loan.put("tenureMonths", 240);
        loan.put("emi", 5000.0);
        loan.put("startDate", "2024-01-01");
        loan.put("lenderName", "HDFC Bank");
        return loan;
    }

    @Test
    @Order(1)
    @DisplayName("Should create loan successfully")
    void testCreateLoan() {
        Map<String, Object> loanData = createLoanData("HOME_LOAN", 5000000.0);
        Response response = authHelper.getAuthenticatedSpec()
                .body(loanData)
                .when()
                .post("/api/v1/loans/create");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
    }

    @Test
    @Order(2)
    @DisplayName("Should get all user loans")
    void testGetUserLoans() {
        createLoanData("PERSONAL_LOAN", 200000.0);
        authHelper.getAuthenticatedSpec()
                .body(createLoanData("PERSONAL_LOAN", 200000.0))
                .post("/api/v1/loans/create");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/loans/user/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get loan by ID")
    void testGetLoanById() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLoanData("CAR_LOAN", 800000.0))
                .post("/api/v1/loans/create");
        Long loanId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/loans/" + loanId);
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "id", loanId);
    }

    @Test
    @Order(4)
    @DisplayName("Should get amortization schedule")
    void testGetAmortizationSchedule() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLoanData("EDUCATION_LOAN", 1000000.0))
                .post("/api/v1/loans/create");
        Long loanId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/loans/" + loanId + "/amortization-schedule");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(5)
    @DisplayName("Should analyze loan")
    void testAnalyzeLoan() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLoanData("BUSINESS_LOAN", 3000000.0))
                .post("/api/v1/loans/create");
        Long loanId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/loans/" + loanId + "/analysis");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(6)
    @DisplayName("Should delete loan")
    void testDeleteLoan() {
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(createLoanData("PERSONAL_LOAN", 100000.0))
                .post("/api/v1/loans/create");
        Long loanId = createResponse.jsonPath().getLong("id");

        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/loans/" + loanId);
        ApiAssertions.assertStatusCode(response, 204);
    }
}
