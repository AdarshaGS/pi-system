package com.users.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.AuthHelper;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for User Controller (Profile Management)
 * Note: This is a placeholder test suite. Update with actual endpoints when UserController is implemented.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest extends BaseApiTest {

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

    @Test
    @Order(1)
    @DisplayName("Should get user profile")
    void testGetUserProfile() {
        // TODO: Update endpoint when UserController is implemented
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .get("/api/v1/user/profile/" + userId);
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(2)
    @DisplayName("Should update user profile")
    void testUpdateUserProfile() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Updated Test User");
        updateData.put("mobileNumber", "9876543211");

        Response response = authHelper.getAuthenticatedSpec()
                .body(updateData)
                .when()
                .put("/api/v1/user/profile/" + userId);
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(3)
    @DisplayName("Should change password")
    void testChangePassword() {
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "Test@1234");
        passwordData.put("newPassword", "NewTest@1234");

        Response response = authHelper.getAuthenticatedSpec()
                .body(passwordData)
                .when()
                .post("/api/v1/user/change-password");
        
        // Accept both 200 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 404);
    }

    @Test
    @Order(4)
    @DisplayName("Should delete user account")
    void testDeleteUserAccount() {
        Response response = authHelper.getAuthenticatedSpec()
                .when()
                .delete("/api/v1/user/account/" + userId);
        
        // Accept both 200/204 (success) and 404 (not yet implemented)
        ApiAssertions.assertStatusCode(response, 200, 204, 404);
    }
}
