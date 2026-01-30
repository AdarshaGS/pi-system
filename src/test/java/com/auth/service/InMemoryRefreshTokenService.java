package com.auth.service;

import com.auth.security.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of RefreshTokenService for tests
 * Uses ConcurrentHashMap instead of Redis
 */
@Service
@Primary
@Profile("test")
@Slf4j
public class InMemoryRefreshTokenService implements IRefreshTokenService {

    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> blacklistStore = new ConcurrentHashMap<>();
    private final JwtProperties jwtProperties;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public InMemoryRefreshTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createRefreshToken(String userEmail) {
        String token = UUID.randomUUID().toString();
        String hashedToken = hashToken(token);
        String key = REFRESH_PREFIX + hashedToken;
        refreshTokenStore.put(key, userEmail);
        return token;
    }

    public void blacklistAccessToken(String token) {
        String hashedToken = hashToken(token);
        String key = BLACKLIST_PREFIX + hashedToken;
        blacklistStore.put(key, "blacklisted");
    }

    public boolean isAccessTokenBlacklisted(String token) {
        String hashedToken = hashToken(token);
        String key = BLACKLIST_PREFIX + hashedToken;
        return blacklistStore.containsKey(key);
    }

    public String validateAndGetUserEmail(String token) {
        String hashedToken = hashToken(token);
        String key = REFRESH_PREFIX + hashedToken;
        String userEmail = refreshTokenStore.get(key);
        
        if (userEmail == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        
        return userEmail;
    }

    public void deleteRefreshToken(String token) {
        String hashedToken = hashToken(token);
        String key = REFRESH_PREFIX + hashedToken;
        refreshTokenStore.remove(key);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to hash token", e);
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
