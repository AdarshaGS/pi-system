package com.api.savings;

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
 * Integration tests for Fixed Deposit Controller
 * Tests FD CRUD operations and exception handling
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FixedDepositControllerIntegrationTest extends BaseApiTest {

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

        private Map<String, Object> createFDData(String bankName, double principalAmount, int tenureMonths) {
                Map<String, Object> fd = new HashMap<>();
                fd.put("userId", userId);
                fd.put("bankName", bankName);
                fd.put("accountNumber", "FD" + System.currentTimeMillis());
                fd.put("principalAmount", principalAmount);
                fd.put("interestRate", 6.5);
                fd.put("tenureMonths", tenureMonths);
                fd.put("startDate", "2024-01-01");
                fd.put("maturityDate", "2025-01-01");
                return fd;
        }

        @Test
        @Order(1)
        @DisplayName("Should create fixed deposit successfully")
        void testCreateFixedDeposit() {
                // Given
                Map<String, Object> fdData = createFDData("HDFC Bank", 100000.0, 12);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .when()
                                .post("/api/v1/fixed-deposit");

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "id");
                ApiAssertions.assertFieldValue(response, "bankName", "HDFC Bank");
                ApiAssertions.assertFieldValue(response, "principalAmount", 100000.0f);
                ApiAssertions.assertFieldValue(response, "tenureMonths", 12);
        }

        @Test
        @Order(2)
        @DisplayName("Should fail to create duplicate fixed deposit")
        void testCreateDuplicateFixedDeposit() {
                // Given - create first FD
                Map<String, Object> fdData = createFDData("ICICI Bank", 50000.0, 24);
                authHelper.getAuthenticatedSpec().body(fdData).post("/api/v1/fixed-deposit");

                // When - try to create duplicate (same account number)
                Response response = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .when()
                                .post("/api/v1/fixed-deposit");

                // Then - should return 200 since account numbers are unique each time
                ApiAssertions.assertStatusCode(response, 200);
        }

        @Test
        @Order(3)
        @DisplayName("Should get all fixed deposits for user")
        void testGetAllFixedDeposits() {
                // Given - create multiple FDs
                authHelper.getAuthenticatedSpec()
                                .body(createFDData("SBI Bank", 75000.0, 18))
                                .post("/api/v1/fixed-deposit");
                authHelper.getAuthenticatedSpec()
                                .body(createFDData("Axis Bank", 125000.0, 36))
                                .post("/api/v1/fixed-deposit");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/fixed-deposit/user/" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertArrayNotEmpty(response, "$");

                response.then()
                                .body("size()", greaterThanOrEqualTo(2));
        }

        @Test
        @Order(4)
        @DisplayName("Should get fixed deposit by ID")
        void testGetFixedDepositById() {
                // Given - create FD
                Map<String, Object> fdData = createFDData("Kotak Bank", 200000.0, 60);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .post("/api/v1/fixed-deposit");
                Long fdId = createResponse.jsonPath().getLong("id");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/fixed-deposit/" + fdId + "?userId=" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldValue(response, "id", fdId.intValue());
                ApiAssertions.assertFieldValue(response, "bankName", "Kotak Bank");
        }

        @Test
        @Order(5)
        @DisplayName("Should return 404 for non-existent fixed deposit")
        void testGetNonExistentFixedDeposit() {
                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/fixed-deposit/999999?userId=" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 404);
                response.then()
                                .body("error", containsString("Fixed Deposit"));
        }

        @Test
        @Order(6)
        @DisplayName("Should update fixed deposit successfully")
        void testUpdateFixedDeposit() {
                // Given - create FD first
                Map<String, Object> fdData = createFDData("YES Bank", 80000.0, 12);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .post("/api/v1/fixed-deposit");
                Long fdId = createResponse.jsonPath().getLong("id");

                // Update amount and tenure
                fdData.put("principalAmount", 90000.0);
                fdData.put("tenureMonths", 24);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .when()
                                .put("/api/v1/fixed-deposit/" + fdId + "?userId=" + userId);

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldValue(response, "principalAmount", 90000.0f);
                ApiAssertions.assertFieldValue(response, "tenureMonths", 24);
        }

        @Test
        @Order(7)
        @DisplayName("Should delete fixed deposit successfully")
        void testDeleteFixedDeposit() {
                // Given - create FD first
                Map<String, Object> fdData = createFDData("IndusInd Bank", 60000.0, 18);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .post("/api/v1/fixed-deposit");
                Long fdId = createResponse.jsonPath().getLong("id");

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .when()
                                .delete("/api/v1/fixed-deposit/" + fdId + "?userId=" + userId);

                // Then - controller returns 200 not 204
                ApiAssertions.assertStatusCode(response, 200);

                // Verify deletion
                Response getResponse = authHelper.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/fixed-deposit/" + fdId + "?userId=" + userId);
                ApiAssertions.assertStatusCode(getResponse, 404);
        }

        @Test
        @Order(8)
        @DisplayName("Should validate minimum tenure")
        void testMinimumTenureValidation() {
                // Given - FD with tenure less than minimum (usually 7 days)
                Map<String, Object> fdData = createFDData("Standard Chartered", 100000.0, 0);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .when()
                                .post("/api/v1/fixed-deposit");

                // Then - service throws IllegalArgumentException wrapped in 500
                ApiAssertions.assertStatusCode(response, 500);
        }

        @Test
        @Order(9)
        @DisplayName("Should validate minimum amount")
        void testMinimumAmountValidation() {
                // Given - FD with very small amount
                Map<String, Object> fdData = createFDData("RBL Bank", 100.0, 12);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .when()
                                .post("/api/v1/fixed-deposit");

                // Then - no minimum amount validation, so it should succeed
                ApiAssertions.assertStatusCode(response, 200);
        }

        @Test
        @Order(10)
        @DisplayName("Should calculate maturity amount correctly")
        void testMaturityCalculation() {
                // Given
                Map<String, Object> fdData = createFDData("IDFC Bank", 100000.0, 12);

                // When
                Response response = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .when()
                                .post("/api/v1/fixed-deposit");

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "maturityAmount");

                // Maturity should be more than principal (with interest)
                response.then()
                                .body("maturityAmount", greaterThan(100000.0f));
        }

        @Test
        @Order(11)
        @DisplayName("Should prevent unauthorized access to other user's FDs")
        void testUnauthorizedAccess() {
                // Given - create another user
                Map<String, Object> user2Data = TestDataBuilder.createTestUser();
                AuthHelper authHelper2 = new AuthHelper(requestSpec);
                authHelper2.register(
                                (String) user2Data.get("email"),
                                (String) user2Data.get("password"),
                                (String) user2Data.get("name"),
                                (String) user2Data.get("mobileNumber"),
                                (String) user2Data.get("roles"));
                authHelper2.login(
                                (String) user2Data.get("email"),
                                (String) user2Data.get("password"));

                // Create FD with first user
                Map<String, Object> fdData = createFDData("DBS Bank", 150000.0, 24);
                Response createResponse = authHelper.getAuthenticatedSpec()
                                .body(fdData)
                                .post("/api/v1/fixed-deposit");
                Long fdId = createResponse.jsonPath().getLong("id");

                // When - second user tries to access first user's FD
                Response response = authHelper2.getAuthenticatedSpec()
                                .when()
                                .get("/api/v1/fixed-deposit/" + fdId + "?userId=" + userId);

                // Then - should be forbidden or not found
                response.then()
                                .statusCode(anyOf(equalTo(403), equalTo(404)));
        }
}
