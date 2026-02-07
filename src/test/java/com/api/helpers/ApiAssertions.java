package com.api.helpers;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Helper class for common API assertions
 */
public class ApiAssertions {

    /**
     * Assert successful response (200-299)
     */
    public static void assertSuccess(Response response) {
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                "Expected success status code but got: " + response.statusCode());
    }

    /**
     * Assert specific status code
     */
    public static void assertStatusCode(Response response, int expectedCode) {
        assertEquals(expectedCode, response.statusCode(),
                "Status code mismatch");
    }

    /**
     * Assert one of multiple acceptable status codes
     */
    public static void assertStatusCode(Response response, int... expectedCodes) {
        int actualCode = response.statusCode();
        for (int expectedCode : expectedCodes) {
            if (actualCode == expectedCode) {
                return;
            }
        }
        fail("Expected one of " + java.util.Arrays.toString(expectedCodes) + 
             " but got: " + actualCode);
    }

    /**
     * Assert response time
     */
    public static void assertResponseTime(Response response, long maxMillis) {
        assertTrue(response.time() < maxMillis,
                String.format("Response took %d ms, expected less than %d ms",
                        response.time(), maxMillis));
    }

    /**
     * Assert field exists in response
     */
    public static void assertFieldExists(Response response, String fieldPath) {
        assertNotNull(response.jsonPath().get(fieldPath),
                "Field '" + fieldPath + "' should exist in response");
    }

    /**
     * Assert field has specific value
     */
    public static void assertFieldValue(Response response, String fieldPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(fieldPath);
        
        // Handle numeric comparisons (JSON often returns numbers as Double)
        if (expectedValue instanceof Number && actualValue instanceof Number) {
            double expected = ((Number) expectedValue).doubleValue();
            double actual = ((Number) actualValue).doubleValue();
            assertEquals(expected, actual, 0.001,
                    "Field '" + fieldPath + "' has unexpected value");
        } else {
            assertEquals(expectedValue, actualValue,
                    "Field '" + fieldPath + "' has unexpected value");
        }
    }

    /**
     * Assert error response structure
     */
    public static void assertErrorResponse(Response response, int expectedCode, String expectedMessage) {
        assertStatusCode(response, expectedCode);
        response.then()
                .body("status", equalTo(expectedCode))
                .body("message", containsString(expectedMessage));
    }

    /**
     * Assert JSON schema
     */
    public static void assertJsonSchema(Response response, String schemaPath) {
        response.then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }

    /**
     * Assert array size
     */
    public static void assertArraySize(Response response, String arrayPath, int expectedSize) {
        response.then()
                .body(arrayPath, hasSize(expectedSize));
    }

    /**
     * Assert array not empty
     */
    public static void assertArrayNotEmpty(Response response, String arrayPath) {
        response.then()
                .body(arrayPath, not(empty()));
    }
}
