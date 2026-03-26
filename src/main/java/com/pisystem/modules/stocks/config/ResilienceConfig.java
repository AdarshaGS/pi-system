// package com.investments.stocks.config;

// // import io.github.resilience4j.ratelimiter.RateLimiter;
// // import io.github.resilience4j.ratelimiter.RateLimiterConfig;
// // import io.github.resilience4j.retry.Retry;
// // import io.github.resilience4j.retry.RetryConfig;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import java.time.Duration;
// import java.util.Optional;

// @Configuration
// public class ResilienceConfig {

//     private final ResilienceConfigRepository resilienceConfigRepository;

//     public ResilienceConfig(ResilienceConfigRepository resilienceConfigRepository) {
//         this.resilienceConfigRepository = resilienceConfigRepository;
//     }

//     @Bean
//     public RateLimiterConfig rateLimiterConfig() {
//         ResilienceConfigEntity configEntity = resilienceConfigRepository.findById("indianApiRateLimiter")
//                 .orElseGet(() -> {
//                     ResilienceConfigEntity defaultConfig = new ResilienceConfigEntity();
//                     defaultConfig.setId("indianApiRateLimiter");
//                     defaultConfig.setLimitForPeriod(5);
//                     defaultConfig.setTimeoutDuration(1);
//                     return resilienceConfigRepository.save(defaultConfig);
//                 });

//         return RateLimiterConfig.custom()
//                 .limitRefreshPeriod(Duration.ofSeconds(1))
//                 .limitForPeriod(configEntity.getLimitForPeriod())
//                 .timeoutDuration(Duration.ofSeconds(configEntity.getTimeoutDuration()))
//                 .build();
//     }

//     @Bean
//     public RateLimiter rateLimiter(RateLimiterConfig rateLimiterConfig) {
//         return RateLimiter.of("indianApiRateLimiter", rateLimiterConfig);
//     }

//     @Bean
//     public RetryConfig retryConfig() {
//         ResilienceConfigEntity configEntity = resilienceConfigRepository.findById("indianApiRetry")
//                 .orElseGet(() -> {
//                     ResilienceConfigEntity defaultConfig = new ResilienceConfigEntity();
//                     defaultConfig.setId("indianApiRetry");
//                     defaultConfig.setMaxAttempts(3);
//                     defaultConfig.setWaitDuration(500);
//                     return resilienceConfigRepository.save(defaultConfig);
//                 });

//         return RetryConfig.custom()
//                 .maxAttempts(configEntity.getMaxAttempts())
//                 .waitDuration(Duration.ofMillis(configEntity.getWaitDuration()))
//                 .build();
//     }

//     @Bean
//     public Retry retry(RetryConfig retryConfig) {
//         return Retry.of("indianApiRetry", retryConfig);
//     }
// }
