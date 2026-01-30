package com.api.health;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;

/**
 * Integration tests for Health Check Controller
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HealthCheckControllerIntegrationTest extends BaseApiTest {

    @Test
    @Order(1)
    @DisplayName("Should return health status")
    void testHealthCheck() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/api/health");

        ApiAssertions.assertStatusCode(response, 200);
        ApiAssertions.assertFieldExists(response, "status");
    }
}
