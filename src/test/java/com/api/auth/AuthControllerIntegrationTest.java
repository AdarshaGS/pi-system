package com.api.auth;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import com.api.helpers.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Authentication Controller
 * Tests user registration, login, logout, token refresh
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest extends BaseApiTest {

        private static String testEmail;
        private static String testPassword;
        private static String jwtToken;
        private static String refreshToken;

        @BeforeEach
        void setUp() {
                testEmail = TestDataBuilder.generateUniqueEmail();
                testPassword = "Test@1234";
        }

        @Test
        @Order(1)
        @DisplayName("Should register new user successfully")
        void testUserRegistration() {
                // Given
                Map<String, Object> userData = TestDataBuilder.createTestUser();

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(userData)
                                .when()
                                .post("/api/auth/register");

                // Then
                ApiAssertions.assertStatusCode(response, 201);
                ApiAssertions.assertFieldExists(response, "userId");
                ApiAssertions.assertFieldExists(response, "email");
                ApiAssertions.assertFieldValue(response, "email", userData.get("email"));
                ApiAssertions.assertResponseTime(response, 3000);
        }

        @Test
        @Order(2)
        @DisplayName("Should fail registration with duplicate email")
        void testDuplicateEmailRegistration() {
                // Given - register user first
                Map<String, Object> userData = TestDataBuilder.createTestUser();
                given().spec(requestSpec).body(userData).post("/api/auth/register");

                // When - try to register again with same email
                Response response = given()
                                .spec(requestSpec)
                                .body(userData)
                                .when()
                                .post("/api/auth/register");

                // Then
                ApiAssertions.assertStatusCode(response, 400);
                response.then()
                                .body("message", containsString("already exists"));
        }

        @Test
        @Order(3)
        @DisplayName("Should fail registration with invalid email format")
        void testInvalidEmailRegistration() {
                // Given
                String requestBody = """
                                {
                                    "email": "invalid-email",
                                    "password": "Test@1234",
                                    "firstName": "Test",
                                    "lastName": "User"
                                }
                                """;

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(requestBody)
                                .when()
                                .post("/api/auth/register");

                // Then
                ApiAssertions.assertStatusCode(response, 400);
        }

        @Test
        @Order(4)
        @DisplayName("Should fail registration with weak password")
        void testWeakPasswordRegistration() {
                // Given
                String requestBody = String.format("""
                                {
                                    "email": "%s",
                                    "password": "123",
                                    "firstName": "Test",
                                    "lastName": "User"
                                }
                                """, testEmail);

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(requestBody)
                                .when()
                                .post("/api/auth/register");

                // Then
                ApiAssertions.assertStatusCode(response, 400);
        }

        @Test
        @Order(5)
        @DisplayName("Should login successfully with valid credentials")
        void testSuccessfulLogin() {
                // Given - register user first
                Map<String, Object> userData = TestDataBuilder.createTestUser();
                given().spec(requestSpec).body(userData).post("/api/auth/register");

                String loginRequest = String.format("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """, userData.get("email"), userData.get("password"));

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(loginRequest)
                                .when()
                                .post("/api/auth/login");

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "token");
                ApiAssertions.assertFieldExists(response, "refreshToken");
                ApiAssertions.assertFieldExists(response, "name");

                response.then()
                                .body("email", equalTo(userData.get("email")))
                                .body("token", notNullValue())
                                .body("refreshToken", notNullValue());

                // Store for later tests
                jwtToken = response.jsonPath().getString("token");
                refreshToken = response.jsonPath().getString("refreshToken");
        }

        @Test
        @Order(6)
        @DisplayName("Should fail login with invalid credentials")
        void testFailedLogin() {
                // Given
                String loginRequest = """
                                {
                                    "email": "nonexistent@example.com",
                                    "password": "wrongpassword"
                                }
                                """;

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(loginRequest)
                                .when()
                                .post("/api/auth/login");

                // Then
                ApiAssertions.assertStatusCode(response, 401);
        }

        @Test
        @Order(7)
        @DisplayName("Should refresh token successfully")
        void testTokenRefresh() {
                // Given - register and login first
                Map<String, Object> userData = TestDataBuilder.createTestUser();
                given().spec(requestSpec).body(userData).post("/api/auth/register");

                String loginRequest = String.format("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """, userData.get("email"), userData.get("password"));

                Response loginResponse = given().spec(requestSpec).body(loginRequest).post("/api/auth/login");
                String oldRefreshToken = loginResponse.jsonPath().getString("refreshToken");

                String refreshRequest = String.format("""
                                {
                                    "refreshToken": "%s"
                                }
                                """, oldRefreshToken);

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(refreshRequest)
                                .when()
                                .post("/api/auth/refresh");

                // Then
                ApiAssertions.assertStatusCode(response, 200);
                ApiAssertions.assertFieldExists(response, "accessToken");
                ApiAssertions.assertFieldExists(response, "refreshToken");

                response.then()
                                .body("accessToken", not(equalTo(loginResponse.jsonPath().getString("accessToken"))));
        }

        @Test
        @Order(8)
        @DisplayName("Should fail with invalid refresh token")
        void testInvalidRefreshToken() {
                // Given
                String refreshRequest = """
                                {
                                    "refreshToken": "invalid-token-12345"
                                }
                                """;

                // When
                Response response = given()
                                .spec(requestSpec)
                                .body(refreshRequest)
                                .when()
                                .post("/api/auth/refresh");

                // Then
                ApiAssertions.assertStatusCode(response, 401);
        }

        @Test
        @Order(9)
        @DisplayName("Should access protected endpoint with valid token")
        void testProtectedEndpointAccess() {
                // Given - register and login first
                Map<String, Object> userData = TestDataBuilder.createTestUser();
                Response registerResponse = given().spec(requestSpec).body(userData).post("/api/auth/register");
                Long userId = registerResponse.jsonPath().getLong("userId");

                String loginRequest = String.format("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """, userData.get("email"), userData.get("password"));

                Response loginResponse = given().spec(requestSpec).body(loginRequest).post("/api/auth/login");
                String token = loginResponse.jsonPath().getString("token");

                // When - access protected endpoint
                Response response = given()
                                .spec(requestSpec)
                                .header("Authorization", "Bearer " + token)
                                .when()
                                .get("/api/v1/portfolio/summary/" + userId);

                // Then
                ApiAssertions.assertSuccess(response);
        }

        @Test
        @Order(10)
        @DisplayName("Should deny access without token")
        void testUnauthorizedAccess() {
                // When
                Response response = given()
                                .spec(requestSpec)
                                .when()
                                .get("/api/v1/portfolio/summary/1");

                // Then
                ApiAssertions.assertStatusCode(response, 401);
        }
}
