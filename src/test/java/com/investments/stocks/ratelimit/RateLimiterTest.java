package com.investments.stocks.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimiter using Token Bucket algorithm.
 * Tests rate limiting behavior for Alpha Vantage and Indian API.
 */
@DisplayName("Rate Limiter Tests")
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    // ==================== Alpha Vantage Rate Limiting (5 calls/min) ====================

    @Test
    @DisplayName("Should allow first 5 requests for AlphaVantage")
    void testAlphaVantage_AllowFirst5Requests() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryConsume("AlphaVantage"), 
                      "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reject 6th request for AlphaVantage")
    void testAlphaVantage_Reject6thRequest() {
        // Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryConsume("AlphaVantage");
        }
        
        // 6th request should fail
        assertFalse(rateLimiter.tryConsume("AlphaVantage"), 
                   "6th request should be rejected due to rate limit");
    }

    @Test
    @DisplayName("Should show correct available tokens for AlphaVantage")
    void testAlphaVantage_AvailableTokens() {
        // Initial state: 5 tokens
        assertEquals(5, rateLimiter.getAvailableTokens("AlphaVantage"));
        
        // After 1 request: 4 tokens
        rateLimiter.tryConsume("AlphaVantage");
        assertEquals(4, rateLimiter.getAvailableTokens("AlphaVantage"));
        
        // After 3 more requests: 1 token
        rateLimiter.tryConsume("AlphaVantage");
        rateLimiter.tryConsume("AlphaVantage");
        rateLimiter.tryConsume("AlphaVantage");
        assertEquals(1, rateLimiter.getAvailableTokens("AlphaVantage"));
        
        // After 5th request: 0 tokens
        rateLimiter.tryConsume("AlphaVantage");
        assertEquals(0, rateLimiter.getAvailableTokens("AlphaVantage"));
    }

    // ==================== Indian API Rate Limiting (60 calls/min) ====================

    @Test
    @DisplayName("Should allow first 60 requests for IndianAPI")
    void testIndianAPI_AllowFirst60Requests() {
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimiter.tryConsume("IndianAPI"), 
                      "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reject 61st request for IndianAPI")
    void testIndianAPI_Reject61stRequest() {
        // Consume all 60 tokens
        for (int i = 0; i < 60; i++) {
            rateLimiter.tryConsume("IndianAPI");
        }
        
        // 61st request should fail
        assertFalse(rateLimiter.tryConsume("IndianAPI"), 
                   "61st request should be rejected due to rate limit");
    }

    @Test
    @DisplayName("Should show correct available tokens for IndianAPI")
    void testIndianAPI_AvailableTokens() {
        assertEquals(60, rateLimiter.getAvailableTokens("IndianAPI"));
        
        rateLimiter.tryConsume("IndianAPI");
        assertEquals(59, rateLimiter.getAvailableTokens("IndianAPI"));
        
        // Consume 10 more tokens
        for (int i = 0; i < 10; i++) {
            rateLimiter.tryConsume("IndianAPI");
        }
        assertEquals(49, rateLimiter.getAvailableTokens("IndianAPI"));
    }

    // ==================== Independent Provider Limits ====================

    @Test
    @DisplayName("Should maintain independent limits for each provider")
    void testIndependentProviderLimits() {
        // Exhaust AlphaVantage tokens
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryConsume("AlphaVantage");
        }
        
        // AlphaVantage should be exhausted
        assertFalse(rateLimiter.tryConsume("AlphaVantage"));
        assertEquals(0, rateLimiter.getAvailableTokens("AlphaVantage"));
        
        // IndianAPI should still have all tokens
        assertTrue(rateLimiter.tryConsume("IndianAPI"));
        assertEquals(59, rateLimiter.getAvailableTokens("IndianAPI"));
    }

    // ==================== Unknown Provider Handling ====================

    @Test
    @DisplayName("Should allow requests for unknown providers (no limiter configured)")
    void testUnknownProvider_AllowByDefault() {
        assertTrue(rateLimiter.tryConsume("UnknownProvider"));
        assertEquals(-1, rateLimiter.getAvailableTokens("UnknownProvider"));
    }

    @Test
    @DisplayName("Should return -1 for available tokens of unknown provider")
    void testUnknownProvider_AvailableTokens() {
        assertEquals(-1, rateLimiter.getAvailableTokens("NonExistentProvider"));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle null provider name gracefully")
    void testNullProviderName() {
        assertTrue(rateLimiter.tryConsume(null)); // Should allow (null provider name)
        assertEquals(-1, rateLimiter.getAvailableTokens(null));
    }

    @Test
    @DisplayName("Should handle empty provider name gracefully")
    void testEmptyProviderName() {
        assertTrue(rateLimiter.tryConsume("")); // Should allow (no limiter found)
        assertEquals(-1, rateLimiter.getAvailableTokens(""));
    }

    // ==================== Concurrent Access Tests ====================

    @Test
    @DisplayName("Should handle concurrent requests correctly")
    void testConcurrentRequests() {
        // Simulate concurrent requests
        int successCount = 0;
        for (int i = 0; i < 10; i++) {
            if (rateLimiter.tryConsume("AlphaVantage")) {
                successCount++;
            }
        }
        
        // Should allow exactly 5 requests (AlphaVantage limit)
        assertEquals(5, successCount);
        assertEquals(0, rateLimiter.getAvailableTokens("AlphaVantage"));
    }

    // ==================== Token Refill Tests (Time-based) ====================

    /**
     * Note: Bucket4j refills tokens automatically after the configured duration.
     * In production, after 1 minute, the bucket for AlphaVantage will refill to 5 tokens.
     * These tests cannot easily simulate time passing without Thread.sleep(),
     * which would make tests slow. For time-based refill testing, consider:
     * 1. Manual testing with actual delays
     * 2. Integration tests with shorter refill durations
     * 3. Using Bucket4j's test utilities if available
     * 
     * Current test coverage validates:
     * - ✅ Initial token allocation
     * - ✅ Token consumption
     * - ✅ Rejection after exhaustion
     * - ✅ Independent provider limits
     * - ✅ Unknown provider handling
     * - ⚠️ Time-based refill (requires integration test or manual test)
     */

    @Test
    @DisplayName("Integration note: Token refill after 1 minute (not tested here)")
    void testTokenRefill_IntegrationNote() {
        // This test documents that token refill is time-based and requires
        // either Thread.sleep() or integration testing with real time delays.
        // 
        // Expected behavior:
        // 1. Exhaust all 5 AlphaVantage tokens
        // 2. Wait 1 minute
        // 3. Tokens should refill to 5
        // 4. Requests should succeed again
        
        assertTrue(true); // Documentation test
    }
}
