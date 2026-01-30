package com.auth.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnMissingBean(RedisTemplate.class)
public class InMemoryRefreshTokenService implements IRefreshTokenService {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();
    private final Map<String, Boolean> blacklist = new ConcurrentHashMap<>();

    @Override
    public String createRefreshToken(String userEmail) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userEmail);
        return token;
    }

    @Override
    public void blacklistAccessToken(String token) {
        blacklist.put(token, true);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String token) {
        return blacklist.getOrDefault(token, false);
    }

    @Override
    public String validateAndGetUserEmail(String token) {
        String email = tokens.get(token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        return email;
    }

    @Override
    public void deleteRefreshToken(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
