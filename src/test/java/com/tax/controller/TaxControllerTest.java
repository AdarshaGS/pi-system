package com.tax.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for Tax Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaxControllerTest extends BaseApiTest {

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

    private Map<String, Object> createTaxData(String financialYear) {
        Map<String, Object> tax = new HashMap<>();
        tax.put("userId", userId);
        tax.put("financialYear", financialYear);
        tax.put("grossIncome", 1500000.0);
        tax.put("deductions", 150000.0);
        tax.put("taxRegime", "OLD");
        return tax;
    }

    @Test
    @Order(1)
    @DisplayName("Should create tax details successfully")
    void testCreateTaxDetails() {
        Map<String, Object> taxData = createTaxData("2023-24");
        Response response = authHelper.getAuthenticatedSpec()
                .body(taxData)
                .when()
                .post("/api/v1/tax");
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "userId");
    }

    @Test
    @Order(2)
    @DisplayName("Should get tax details by user and financial year")
    void testGetTaxDetails() {
        authHelper.getAuthenticatedSpec()
                .body(createTaxData("2024-25"))
                .post("/api/v1/tax");

        Response response = authHelper.getAuthenticatedSpec()
                .param("financialYear", "2024-25")
                .when()
                .get("/api/v1/tax/" + userId);
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(3)
    @DisplayName("Should get outstanding tax liability")
    void testGetOutstandingTaxLiability() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/tax/" + userId + "/liability");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(4)
    @DisplayName("Should compare tax regimes")
    void testCompareTaxRegimes() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("financialYear", "2024-25")
                .param("grossIncome", 1500000)
                .when()
                .get("/api/v1/tax/" + userId + "/regime-comparison");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(5)
    @DisplayName("Should record capital gains transaction")
    void testRecordCapitalGain() {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("userId", userId);
        transaction.put("assetType", "EQUITY");
        transaction.put("assetName", "RELIANCE");
        transaction.put("purchasePrice", 2000.0);
        transaction.put("salePrice", 2500.0);
        transaction.put("quantity", 10);
        transaction.put("purchaseDate", "2023-01-15");
        transaction.put("saleDate", "2024-02-20");
        transaction.put("financialYear", "2023-24");

        Response response = authHelper.getAuthenticatedSpec()
                .body(transaction)
                .when()
                .post("/api/v1/tax/" + userId + "/capital-gains");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(6)
    @DisplayName("Should get capital gains summary")
    void testGetCapitalGainsSummary() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("financialYear", "2023-24")
                .when()
                .get("/api/v1/tax/" + userId + "/capital-gains/summary");
        ApiAssertions.assertStatusCode(response, 200);
    }

    @Test
    @Order(7)
    @DisplayName("Should get capital gains transactions")
    void testGetCapitalGainsTransactions() {
        Response response = authHelper.getAuthenticatedSpec()
                .param("financialYear", "2023-24")
                .when()
                .get("/api/v1/tax/" + userId + "/capital-gains/transactions");
        ApiAssertions.assertStatusCode(response, 200);
    }
}
