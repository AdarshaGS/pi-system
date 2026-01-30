package com.api.config;

import com.main.Application;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for API integration tests
 * Provides common setup for REST Assured and Spring Boot Test
 */
@SpringBootTest(
    classes = Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {OAuth2ClientAutoConfiguration.class})
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    protected RequestSpecification requestSpec;
    protected String baseUrl;

    @BeforeEach
    public void setUpBase() {
        baseUrl = "http://localhost:" + port;
        RestAssured.port = port;
        RestAssured.baseURI = baseUrl;

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType("application/json")
                .setAccept("application/json")
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    /**
     * Get base API path
     */
    protected String getApiPath(String endpoint) {
        return "/api" + endpoint;
    }

    /**
     * Get versioned API path
     */
    protected String getV1ApiPath(String endpoint) {
        return "/api/v1" + endpoint;
    }
}
