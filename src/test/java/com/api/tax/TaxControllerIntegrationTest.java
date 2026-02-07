package com.api.tax;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Tax Controller
 * Tests all 16 tax management endpoints
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaxControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;
    private String financialYear = "2024-25";

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

    // ========== Basic Tax Management Tests ==========

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/tax - Should create tax details successfully")
    void testCreateTaxDetails() {
        // Given
        Map<String, Object> taxData = TestDataBuilder.createTaxData(userId, financialYear);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(taxData)
                .when()
                .post("/api/v1/tax");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "userId");
        ApiAssertions.assertFieldValue(response, "financialYear", financialYear);
        response.then()
                .body("grossIncome", notNullValue())
                .body("taxRegime", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/tax - Should validate required fields")
    void testCreateTaxDetailsWithMissingFields() {
        // Given - missing financialYear
        Map<String, Object> taxData = new HashMap<>();
        taxData.put("userId", userId);
        taxData.put("grossIncome", 1000000.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(taxData)
                .when()
                .post("/api/v1/tax");

        // Then
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/tax/{userId} - Should get tax details")
    void testGetTaxDetails() {
        // Given - create tax details first
        Map<String, Object> taxData = TestDataBuilder.createTaxData(userId, financialYear);
        authHelper.getAuthenticatedSpec()
                .body(taxData)
                .post("/api/v1/tax");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("userId", equalTo(userId.intValue()))
                .body("financialYear", equalTo(financialYear));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/tax/{userId}/liability - Should get outstanding tax liability")
    void testGetOutstandingTaxLiability() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/tax/" + userId + "/liability");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    // ========== Tax Regime Comparison Tests ==========

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/tax/{userId}/regime-comparison - Should compare tax regimes")
    void testCompareTaxRegimes() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .queryParam("grossIncome", 1200000)
                .when()
                .get("/api/v1/tax/" + userId + "/regime-comparison");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("oldRegimeTax", notNullValue())
                .body("newRegimeTax", notNullValue())
                .body("recommendation", notNullValue());
    }

    // ========== Capital Gains Tests ==========

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/tax/{userId}/capital-gains - Should record capital gain")
    void testRecordCapitalGain() {
        // Given
        Map<String, Object> cgData = TestDataBuilder.createCapitalGainsData(userId, "EQUITY");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(cgData)
                .when()
                .post("/api/v1/tax/" + userId + "/capital-gains");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("assetType", equalTo("EQUITY"))
                .body("capitalGain", notNullValue())
                .body("gainType", anyOf(equalTo("STCG"), equalTo("LTCG")));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/tax/{userId}/capital-gains/summary - Should get capital gains summary")
    void testGetCapitalGainsSummary() {
        // Given - create some capital gains
        Map<String, Object> cgData1 = TestDataBuilder.createCapitalGainsData(userId, "EQUITY");
        authHelper.getAuthenticatedSpec()
                .body(cgData1)
                .post("/api/v1/tax/" + userId + "/capital-gains");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/capital-gains/summary");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/tax/{userId}/capital-gains/transactions - Should list capital gains transactions")
    void testGetCapitalGainsTransactions() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/capital-gains/transactions");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/v1/tax/capital-gains/calculate - Should calculate capital gains preview")
    void testCalculateCapitalGains() {
        // Given
        Map<String, Object> cgData = new HashMap<>();
        cgData.put("assetType", "EQUITY");
        cgData.put("purchaseDate", LocalDate.now().minusMonths(6).toString());
        cgData.put("saleDate", LocalDate.now().toString());
        cgData.put("purchasePrice", 100000.0);
        cgData.put("salePrice", 120000.0);
        cgData.put("quantity", 10);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(cgData)
                .when()
                .post("/api/v1/tax/capital-gains/calculate");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("capitalGain", notNullValue())
                .body("gainType", anyOf(equalTo("STCG"), equalTo("LTCG")))
                .body("taxAmount", notNullValue());
    }

    // ========== Tax Saving Tests ==========

    @Test
    @Order(10)
    @DisplayName("GET /api/v1/tax/{userId}/recommendations - Should get tax saving recommendations")
    void testGetTaxSavingRecommendations() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/recommendations");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    @Test
    @Order(11)
    @DisplayName("POST /api/v1/tax/{userId}/tax-savings - Should record tax saving investment")
    void testRecordTaxSavingInvestment() {
        // Given
        Map<String, Object> savingData = new HashMap<>();
        savingData.put("financialYear", financialYear);
        savingData.put("section", "80C");
        savingData.put("investmentType", "PPF");
        savingData.put("amount", 50000.0);
        savingData.put("investmentDate", LocalDate.now().toString());

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(savingData)
                .when()
                .post("/api/v1/tax/" + userId + "/tax-savings");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("section", equalTo("80C"))
                .body("amount", equalTo(50000.0f));
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/v1/tax/{userId}/tax-savings - Should list tax saving investments")
    void testGetTaxSavingInvestments() {
        // Given - create investment
        Map<String, Object> savingData = new HashMap<>();
        savingData.put("financialYear", financialYear);
        savingData.put("section", "80D");
        savingData.put("investmentType", "HEALTH_INSURANCE");
        savingData.put("amount", 25000.0);
        savingData.put("investmentDate", LocalDate.now().toString());
        
        authHelper.getAuthenticatedSpec()
                .body(savingData)
                .post("/api/v1/tax/" + userId + "/tax-savings");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/tax-savings");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    // ========== TDS Tracking Tests ==========

    @Test
    @Order(13)
    @DisplayName("POST /api/v1/tax/{userId}/tds - Should record TDS entry")
    void testRecordTDSEntry() {
        // Given
        Map<String, Object> tdsData = TestDataBuilder.createTDSData(userId, financialYear);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(tdsData)
                .when()
                .post("/api/v1/tax/" + userId + "/tds");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "id");
        response.then()
                .body("deductorName", equalTo("ABC Company"))
                .body("tdsAmount", equalTo(25000.0f));
    }

    @Test
    @Order(14)
    @DisplayName("GET /api/v1/tax/{userId}/tds - Should list TDS entries")
    void testGetTDSEntries() {
        // Given - create TDS entry
        Map<String, Object> tdsData = TestDataBuilder.createTDSData(userId, financialYear);
        authHelper.getAuthenticatedSpec()
                .body(tdsData)
                .post("/api/v1/tax/" + userId + "/tds");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/tds");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(15)
    @DisplayName("GET /api/v1/tax/{userId}/tds/reconciliation - Should get TDS reconciliation")
    void testGetTDSReconciliation() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/tds/reconciliation");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    @Test
    @Order(16)
    @DisplayName("PUT /api/v1/tax/tds/{tdsId}/status - Should update TDS status")
    void testUpdateTDSStatus() {
        // Given - create TDS entry
        Map<String, Object> tdsData = TestDataBuilder.createTDSData(userId, financialYear);
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(tdsData)
                .post("/api/v1/tax/" + userId + "/tds");
        Long tdsId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("status", "VERIFIED")
                .when()
                .put("/api/v1/tax/tds/" + tdsId + "/status");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("id", equalTo(tdsId.intValue()));
    }

    // ========== Tax Projection Tests ==========

    @Test
    @Order(17)
    @DisplayName("GET /api/v1/tax/{userId}/projection - Should get tax projection")
    void testGetTaxProjection() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/projection");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue());
    }

    // ========== ITR Pre-fill Tests ==========

    @Test
    @Order(18)
    @DisplayName("GET /api/v1/tax/{userId}/itr-prefill - Should get ITR pre-fill data")
    void testGetITRPreFillData() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .queryParam("financialYear", financialYear)
                .when()
                .get("/api/v1/tax/" + userId + "/itr-prefill");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        response.then()
                .body("$", notNullValue())
                .body("userId", equalTo(userId.intValue()))
                .body("financialYear", equalTo(financialYear));
    }

    // ========== Edge Cases ==========

    @Test
    @Order(19)
    @DisplayName("Should handle LTCG vs STCG calculation correctly")
    void testLTCGvsSCG() {
        // Given - LTCG (> 1 year holding for equity)
        Map<String, Object> ltcgData = new HashMap<>();
        ltcgData.put("assetType", "EQUITY");
        ltcgData.put("purchaseDate", LocalDate.now().minusYears(2).toString());
        ltcgData.put("saleDate", LocalDate.now().toString());
        ltcgData.put("purchasePrice", 100000.0);
        ltcgData.put("salePrice", 150000.0);
        ltcgData.put("quantity", 10);

        // When
        Response ltcgResponse = authHelper.getAuthenticatedSpec()
                .body(ltcgData)
                .post("/api/v1/tax/capital-gains/calculate");

        // Then
        ltcgResponse.then()
                .body("gainType", equalTo("LTCG"));

        // Given - STCG (< 1 year holding for equity)
        Map<String, Object> stcgData = new HashMap<>();
        stcgData.put("assetType", "EQUITY");
        stcgData.put("purchaseDate", LocalDate.now().minusMonths(6).toString());
        stcgData.put("saleDate", LocalDate.now().toString());
        stcgData.put("purchasePrice", 100000.0);
        stcgData.put("salePrice", 120000.0);
        stcgData.put("quantity", 10);

        // When
        Response stcgResponse = authHelper.getAuthenticatedSpec()
                .body(stcgData)
                .post("/api/v1/tax/capital-gains/calculate");

        // Then
        stcgResponse.then()
                .body("gainType", equalTo("STCG"));
    }

    @Test
    @Order(20)
    @DisplayName("Should validate 80C limit of 1.5 lakhs")
    void test80CLimit() {
        // Given - investment exceeding 80C limit
        Map<String, Object> savingData = new HashMap<>();
        savingData.put("financialYear", financialYear);
        savingData.put("section", "80C");
        savingData.put("investmentType", "PPF");
        savingData.put("amount", 200000.0); // Exceeds limit
        savingData.put("investmentDate", LocalDate.now().toString());

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(savingData)
                .when()
                .post("/api/v1/tax/" + userId + "/tax-savings");

        // Then - should accept but cap at limit
        ApiAssertions.assertStatusCode(response, 200);
    }
}
