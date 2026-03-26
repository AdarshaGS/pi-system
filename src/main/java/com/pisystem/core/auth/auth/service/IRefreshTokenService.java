package com.auth.service;

/**
 * Interface for refresh token operations
 * Allows different implementations (Redis-based or in-memory)
 */
public interface IRefreshTokenService {
    String createRefreshToken(String userEmail);
    void blacklistAccessToken(String token);
    boolean isAccessTokenBlacklisted(String token);
    String validateAndGetUserEmail(String token);
    void deleteRefreshToken(String token);
}
