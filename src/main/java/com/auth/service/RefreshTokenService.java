package com.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token.expiration:2592000000}") // Default 30 days in ms
    private Long refreshTokenDurationMs;

    @Value("${jwt.expiration}")
    private Long accessTokenDurationMs;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Create a new refresh token and store its hash in Redis
     */
    public String createRefreshToken(String userEmail) {
        String token = UUID.randomUUID().toString();
        String hashedToken = hashToken(token);

        String key = REFRESH_PREFIX + hashedToken;
        redisTemplate.opsForValue().set(key, userEmail, refreshTokenDurationMs, TimeUnit.MILLISECONDS);

        return token;
    }

    /**
     * Blacklist an access token in Redis
     */
    public void blacklistAccessToken(String token) {
        String hashedToken = hashToken(token);
        String key = BLACKLIST_PREFIX + hashedToken;
        // Store with TTL equal to the max life of a JWT to ensure it's removed
        // eventually
        redisTemplate.opsForValue().set(key, "blacklisted", accessTokenDurationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Check if an access token is blacklisted
     */
    public boolean isAccessTokenBlacklisted(String token) {
        String hashedToken = hashToken(token);
        String key = BLACKLIST_PREFIX + hashedToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Validate refresh token and return the associated user email
     */
    public String validateAndGetUserEmail(String token) {
        String hashedToken = hashToken(token);
        String key = REFRESH_PREFIX + hashedToken;

        String userEmail = redisTemplate.opsForValue().get(key);
        if (userEmail == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        return userEmail;
    }

    /**
     * Delete refresh token from Redis
     */
    public void deleteRefreshToken(String token) {
        if (token == null)
            return;
        String hashedToken = hashToken(token);
        String key = REFRESH_PREFIX + hashedToken;
        redisTemplate.delete(key);
    }

    /**
     * Hash the token using SHA-256 for secure storage
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing token", e);
            throw new RuntimeException("Error secure hashing token");
        }
    }
}
