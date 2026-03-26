package com.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@ConfigurationProperties(prefix = "jwt")
@Validated
@Data
public class JwtProperties {

    @NotBlank(message = "JWT secret must be configured")
    private String secret;

    @NotNull(message = "JWT expiration must be configured")
    private Long expiration;

    private RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class RefreshToken {
        @NotNull(message = "JWT refresh token expiration must be configured")
        private Long expiration;
    }
}
