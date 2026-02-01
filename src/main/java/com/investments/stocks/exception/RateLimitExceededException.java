package com.investments.stocks.exception;

/**
 * Exception thrown when API rate limit is exceeded.
 * Used to prevent hitting third-party API rate limits (e.g., Alpha Vantage 5 calls/min).
 */
public class RateLimitExceededException extends RuntimeException {
    
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
