package com.api.helpers;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Helper class for authentication-related API calls
 */
public class AuthHelper {

    private final RequestSpecification requestSpec;
    private String jwtToken;
    private String refreshToken;

    public AuthHelper(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    /**
     * Register a new user
     */
    public Response register(String email, String password, String firstName, String lastName) {
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "password": "%s",
                    "firstName": "%s",
                    "lastName": "%s"
                }
                """, email, password, firstName, lastName);

        return given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/api/auth/register");
    }

    /**
     * Login and store JWT token
     */
    public Response login(String email, String password) {
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);

        Response response = given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/api/auth/login");

        if (response.statusCode() == 200) {
            jwtToken = response.jsonPath().getString("token");
            refreshToken = response.jsonPath().getString("refreshToken");
        }

        return response;
    }

    /**
     * Get JWT token (must call login first)
     */
    public String getJwtToken() {
        return jwtToken;
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Create authenticated request specification
     */
    public RequestSpecification getAuthenticatedSpec() {
        if (jwtToken == null) {
            throw new IllegalStateException("No JWT token available. Call login() first.");
        }
        return given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + jwtToken);
    }

    /**
     * Logout
     */
    public Response logout() {
        return getAuthenticatedSpec()
                .when()
                .post("/api/auth/logout");
    }

    /**
     * Refresh token
     */
    public Response refreshToken() {
        String requestBody = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken);

        Response response = given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/api/auth/refresh");

        if (response.statusCode() == 200) {
            jwtToken = response.jsonPath().getString("token");
            refreshToken = response.jsonPath().getString("refreshToken");
        }

        return response;
    }
}
