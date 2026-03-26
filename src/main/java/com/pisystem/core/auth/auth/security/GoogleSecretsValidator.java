package com.auth.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GoogleSecretsValidator {

    private static final Logger log = LoggerFactory.getLogger(GoogleSecretsValidator.class);

    @Autowired
    private Environment env;

    @PostConstruct
    public void validate() {
        // OAuth2 Google login is OPTIONAL
        // Only validate if properties are explicitly configured
        // If not configured, the system will work with JWT authentication only

        String clientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id");

        // If the property exists, validate it
        if (clientId != null && !clientId.isBlank()) {
            boolean isValid = validateProperty("spring.security.oauth2.client.registration.google.client-id")
                    && validateProperty("spring.security.oauth2.client.registration.google.client-secret");
            
            if (isValid) {
                log.info("Google OAuth2 configuration detected and validated successfully");
            } else {
                log.warn("Google OAuth2 credentials are not properly configured. OAuth2 login will be disabled. " +
                        "The system will continue with JWT authentication only.");
            }
        } else {
            log.info("Google OAuth2 not configured. System will use JWT authentication only.");
        }
    }

    private boolean validateProperty(String key) {
        String value = env.getProperty(key);
        if (value == null || value.isBlank() || value.contains("YOUR_CLIENT_ID")
                || value.contains("YOUR_CLIENT_SECRET")) {
            log.warn("OAuth2 configuration '{}' is missing or contains placeholder value. OAuth2 login will be unavailable.", key);
            return false;
        }
        return true;
    }
}
