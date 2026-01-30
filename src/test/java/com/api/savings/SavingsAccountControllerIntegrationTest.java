package com.api.savings;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Savings Account Controller
 * Tests CRUD operations and exception handling
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SavingsAccountControllerIntegrationTest extends BaseApiTest {

    private AuthHelper authHelper;
    private Long userId;
    private Long savingsAccountId;

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
    @DisplayName("Should create savings account successfully")
    void testCreateSavingsAccount() {
        // Given
        Map<String, Object> savingsData = TestDataBuilder.createSavingsAccountData(
                userId, "HDFC Bank", 50000.0
        );

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .when()
                .post("/savings-accounts");

        // Then
        ApiAssertions.assertStatusCode(response, 201);
        ApiAssertions.assertFieldExists(response, "id");
        ApiAssertions.assertFieldValue(response, "bankName", "HDFC Bank");
        ApiAssertions.assertFieldValue(response, "amount", 50000.0);
        
        savingsAccountId = response.jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to create duplicate savings account")
    void testCreateDuplicateSavingsAccount() {
        // Given - create first account
        Map<String, Object> savingsData = TestDataBuilder.createSavingsAccountData(
                userId, "ICICI Bank", 25000.0
        );
        authHelper.getAuthenticatedSpec().body(savingsData).post("/savings-accounts");

        // When - try to create duplicate
        Response response = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .when()
                .post("/savings-accounts");

        // Then - should return 409 Conflict
        ApiAssertions.assertStatusCode(response, 409);
        response.then()
                .body("message", containsString("already exists"));
    }

    @Test
    @Order(3)
    @DisplayName("Should get all savings accounts for user")
    void testGetAllSavingsAccounts() {
        // Given - create multiple accounts
        Map<String, Object> savings1 = TestDataBuilder.createSavingsAccountData(
                userId, "SBI Bank", 30000.0
        );
        Map<String, Object> savings2 = TestDataBuilder.createSavingsAccountData(
                userId, "Axis Bank", 40000.0
        );
        
        authHelper.getAuthenticatedSpec().body(savings1).post("/savings-accounts");
        authHelper.getAuthenticatedSpec().body(savings2).post("/savings-accounts");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/savings-accounts");

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertArrayNotEmpty(response, "$");
        
        response.then()
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(4)
    @DisplayName("Should get savings account by ID")
    void testGetSavingsAccountById() {
        // Given - create account
        Map<String, Object> savingsData = TestDataBuilder.createSavingsAccountData(
                userId, "Kotak Bank", 35000.0
        );
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .post("/savings-accounts");
        Long accountId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/savings-accounts/" + accountId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "id", accountId.intValue());
        ApiAssertions.assertFieldValue(response, "bankName", "Kotak Bank");
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 for non-existent savings account")
    void testGetNonExistentSavingsAccount() {
        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/savings-accounts/999999");

        // Then
        ApiAssertions.assertStatusCode(response, 404);
        response.then()
                .body("message", containsString("not found"));
    }

    @Test
    @Order(6)
    @DisplayName("Should update savings account successfully")
    void testUpdateSavingsAccount() {
        // Given - create account first
        Map<String, Object> savingsData = TestDataBuilder.createSavingsAccountData(
                userId, "YES Bank", 20000.0
        );
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .post("/savings-accounts");
        Long accountId = createResponse.jsonPath().getLong("id");

        // Update amount
        savingsData.put("amount", 25000.0);

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .when()
                .put("/savings-accounts/" + accountId);

        // Then
        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldValue(response, "amount", 25000.0);
    }

    @Test
    @Order(7)
    @DisplayName("Should delete savings account successfully")
    void testDeleteSavingsAccount() {
        // Given - create account first
        Map<String, Object> savingsData = TestDataBuilder.createSavingsAccountData(
                userId, "IndusInd Bank", 15000.0
        );
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .post("/savings-accounts");
        Long accountId = createResponse.jsonPath().getLong("id");

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/savings-accounts/" + accountId);

        // Then
        ApiAssertions.assertStatusCode(response, 204);

        // Verify deletion
        Response getResponse = authHelper.getAuthenticatedSpec()
                .when()
                .get("/savings-accounts/" + accountId);
        ApiAssertions.assertStatusCode(getResponse, 404);
    }

    @Test
    @Order(8)
    @DisplayName("Should validate required fields")
    void testValidationErrors() {
        // Given - missing required fields
        String invalidRequest = """
                {
                    "userId": null,
                    "bankName": "",
                    "amount": -1000
                }
                """;

        // When
        Response response = authHelper.getAuthenticatedSpec()
                .body(invalidRequest)
                .when()
                .post("/savings-accounts");

        // Then
        ApiAssertions.assertStatusCode(response, 400);
    }

    @Test
    @Order(9)
    @DisplayName("Should prevent unauthorized access to other user's accounts")
    void testUnauthorizedAccess() {
        // Given - create another user
        Map<String, Object> user2Data = TestDataBuilder.createTestUser();
        AuthHelper authHelper2 = new AuthHelper(requestSpec);
        authHelper2.register(
                (String) user2Data.get("email"),
                (String) user2Data.get("password"),
                (String) user2Data.get("firstName"),
                (String) user2Data.get("lastName")
        );
        authHelper2.login(
                (String) user2Data.get("email"),
                (String) user2Data.get("password")
        );

        // Create account with first user
        Map<String, Object> savingsData = TestDataBuilder.createSavingsAccountData(
                userId, "Standard Chartered", 45000.0
        );
        Response createResponse = authHelper.getAuthenticatedSpec()
                .body(savingsData)
                .post("/savings-accounts");
        Long accountId = createResponse.jsonPath().getLong("id");

        // When - second user tries to access first user's account
        Response response = authHelper2.getAuthenticatedSpec()
                .when()
                .get("/savings-accounts/" + accountId);

        // Then - should be forbidden
        response.then()
                .statusCode(anyOf(equalTo(403), equalTo(404)));
    }
}
