package com.api.health;

import static io.restassured.RestAssured.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.api.config.BaseApiTest;
import com.api.helpers.ApiAssertions;

import io.restassured.response.Response;

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
