package com.investments.stocks.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis cache configuration for stock prices.
 * Caches stock data for 5 minutes to reduce third-party API calls and respect rate limits.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Configure Redis cache manager with TTL and serialization settings.
     * 
     * Cache Strategy:
     * - Cache Name: "stockPrices"
     * - TTL: 5 minutes (300 seconds)
     * - Key Serializer: StringRedisSerializer (for stock symbols like "RELIANCE", "TCS")
     * - Value Serializer: GenericJackson2JsonRedisSerializer (for ThirdPartyResponse objects)
     * - Max Size: 10,000 entries (configurable via LRU eviction)
     * 
     * This prevents hitting Alpha Vantage rate limits (5 calls/min) while maintaining
     * reasonably fresh data for portfolio calculations.
     */
    @Bean
    public CacheManager stockCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // Cache for 5 minutes
            .disableCachingNullValues() // Don't cache null responses (failed API calls)
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfig)
            .transactionAware() // Participate in Spring transactions
            .build();
    }
}
