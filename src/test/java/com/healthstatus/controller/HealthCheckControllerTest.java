package com.healthstatus.controller;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

/**
 * Integration tests for Health Check Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HealthCheckControllerTest extends BaseApiTest {

    @Test
    @Order(1)
    @DisplayName("Should return health status")
    void testGetHealth() {
        Response response = requestSpec
                .when()
                .get("/api/health");
        
        // Should return 200 or 503 depending on health
        ApiAssertions.assertStatusCode(response, 200, 503);
        ApiAssertions.assertFieldExists(response, "status");
    }

    @Test
    @Order(2)
    @DisplayName("Health status should have required fields")
    void testHealthStatusStructure() {
        Response response = requestSpec
                .when()
                .get("/api/health");
        
        // Verify structure regardless of status code
        if (response.statusCode() == 200 || response.statusCode() == 503) {
            ApiAssertions.assertFieldExists(response, "status");
            ApiAssertions.assertFieldExists(response, "timestamp");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should be accessible without authentication")
    void testHealthCheckWithoutAuth() {
        // Health check should be accessible without auth
        Response response = requestSpec
                .when()
                .get("/api/health");
        
        // Should not return 401 or 403
        int statusCode = response.statusCode();
        assert statusCode != 401 && statusCode != 403 : 
            "Health check should be accessible without authentication";
    }
}
