package com.investments.stocks.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for third-party stock API calls using Token Bucket algorithm.
 * 
 * Alpha Vantage Free Tier Limits:
 * - 5 API requests per minute
 * - 500 API requests per day
 * 
 * This implementation enforces the per-minute limit to prevent API key suspension.
 * Daily limits are tracked separately in application monitoring.
 * 
 * Usage:
 * if (!rateLimiter.tryConsume("AlphaVantage")) {
 *     throw new RateLimitExceededException("Rate limit exceeded for AlphaVantage");
 * }
 * // Proceed with API call
 */
@Component
@Slf4j
public class RateLimiter {

    // Store separate buckets for each provider
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Alpha Vantage: 5 requests per minute
    private static final int ALPHA_VANTAGE_CAPACITY = 5;
    private static final Duration ALPHA_VANTAGE_REFILL_DURATION = Duration.ofMinutes(1);

    // Indian API: Assume 60 requests per minute (adjust based on actual limits)
    private static final int INDIAN_API_CAPACITY = 60;
    private static final Duration INDIAN_API_REFILL_DURATION = Duration.ofMinutes(1);

    public RateLimiter() {
        // Initialize buckets for each provider
        buckets.put("AlphaVantage", createBucket(ALPHA_VANTAGE_CAPACITY, ALPHA_VANTAGE_REFILL_DURATION));
        buckets.put("IndianAPI", createBucket(INDIAN_API_CAPACITY, INDIAN_API_REFILL_DURATION));
    }

    /**
     * Create a token bucket with specified capacity and refill rate.
     * 
     * @param capacity Maximum number of tokens in the bucket
     * @param refillDuration Duration for refilling all tokens
     * @return Configured Bucket instance
     */
    private Bucket createBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Try to consume one token from the provider's bucket.
     * 
     * @param providerName Name of the API provider ("AlphaVantage" or "IndianAPI")
     * @return true if token was consumed (rate limit not exceeded), false otherwise
     */
    public boolean tryConsume(String providerName) {
        if (providerName == null || providerName.isEmpty()) {
            log.warn("Null or empty provider name, allowing request by default");
            return true; // Allow request if provider name is null/empty
        }
        
        Bucket bucket = buckets.get(providerName);
        if (bucket == null) {
            log.warn("No rate limiter configured for provider: {}", providerName);
            return true; // Allow request if no limiter configured
        }

        boolean consumed = bucket.tryConsume(1);
        if (!consumed) {
            log.warn("Rate limit exceeded for provider: {}", providerName);
        } else {
            log.debug("Rate limit check passed for provider: {}. Available tokens: {}", 
                      providerName, bucket.getAvailableTokens());
        }
        return consumed;
    }

    /**
     * Get the number of available tokens for a provider.
     * Useful for monitoring and debugging.
     * 
     * @param providerName Name of the API provider
     * @return Number of available tokens, or -1 if provider not found
     */
    public long getAvailableTokens(String providerName) {
        if (providerName == null || providerName.isEmpty()) {
            return -1; // Return -1 for null/empty provider name
        }
        
        Bucket bucket = buckets.get(providerName);
        return bucket != null ? bucket.getAvailableTokens() : -1;
    }
}
