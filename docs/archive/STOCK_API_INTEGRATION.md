# Stock API Integration - Implementation Guide

**Date**: January 31, 2026  
**Status**: Implementing Real Market Data  
**Priority**: P0 - Critical

---

## Overview

Replacing mock/simulated stock prices with real market data from:
1. **Alpha Vantage** (Primary) - Global stock data
2. **Indian API** (Secondary) - NSE/BSE specific data

---

## Implementation Tasks

### Phase 1: Alpha Vantage Enhancement ✅
- [ ] Add GLOBAL_QUOTE endpoint for real-time prices
- [ ] Add TIME_SERIES_DAILY for historical data
- [ ] Implement rate limiting (5 calls/min for free tier)
- [ ] Add caching with Redis (5-minute cache)
- [ ] Add proper error handling and retries

### Phase 2: Indian API Enhancement ✅
- [ ] Validate NSE/BSE price data format
- [ ] Add exchange-specific validation
- [ ] Implement fallback mechanisms
- [ ] Add data freshness checks

### Phase 3: Validation & Quality ✅
- [ ] Price validation (positive, reasonable range)
- [ ] Timestamp validation (not stale data)
- [ ] Symbol format validation
- [ ] Exchange code validation (NSE/BSE)

### Phase 4: Testing ✅
- [ ] Unit tests for AlphaVantageProvider
- [ ] Unit tests for IndianAPIService
- [ ] Integration tests for StockDataProviderFactory
- [ ] End-to-end tests for StockReadService
- [ ] Performance tests for caching

### Phase 5: Monitoring ✅
- [ ] Add metrics for API success/failure rates
- [ ] Add alerts for stale data
- [ ] Add dashboard for API health
- [ ] Log all third-party API calls

---

## API Endpoints

### Alpha Vantage

#### 1. GLOBAL_QUOTE (Real-time Price)
```
GET https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=RELIANCE.BSE&apikey=YOUR_API_KEY
```

Response:
```json
{
    "Global Quote": {
        "01. symbol": "RELIANCE.BSE",
        "05. price": "2450.50",
        "07. latest trading day": "2026-01-31",
        "08. previous close": "2445.00",
        "09. change": "5.50",
        "10. change percent": "0.22%"
    }
}
```

#### 2. OVERVIEW (Company Info)
```
GET https://www.alphavantage.co/query?function=OVERVIEW&symbol=RELIANCE.BSE&apikey=YOUR_API_KEY
```

### Indian API

```
GET {endpoint}?name=RELIANCE
Headers: x-api-key: YOUR_API_KEY
```

Response:
```json
{
    "companyName": "Reliance Industries Ltd.",
    "currentPrice": {
        "NSE": 2450.50,
        "BSE": 2450.25
    },
    "companyProfile": {
        "mgIndustry": "Oil & Gas",
        "companyDescription": "..."
    }
}
```

---

## Rate Limiting Strategy

| Provider | Free Tier Limit | Implementation |
|----------|----------------|----------------|
| Alpha Vantage | 5 calls/min, 500/day | Bucket4j rate limiter + Redis cache (5 min TTL) |
| Indian API | TBD | Redis cache (2 min TTL) |

---

## Caching Strategy

```java
@Cacheable(value = "stockPrices", key = "#symbol", unless = "#result == null")
public ThirdPartyResponse fetchStockData(String symbol) {
    // API call
}
```

**Cache Configuration**:
- **TTL**: 5 minutes (real-time feel without excessive API calls)
- **Storage**: Redis
- **Eviction**: LRU (Least Recently Used)
- **Max Size**: 10,000 symbols

---

## Error Handling

### Fallback Chain
1. **Primary**: Alpha Vantage GLOBAL_QUOTE
2. **Secondary**: Indian API (if Primary fails)
3. **Tertiary**: Database cached price (if both fail)
4. **Final**: Throw SymbolNotFoundException

### Retry Logic
```java
@Retryable(
    value = { RestClientException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
```

---

## Validation Rules

### Price Validation
```java
if (price == null || price <= 0 || price > 1000000) {
    throw new InvalidPriceException("Invalid price: " + price);
}
```

### Timestamp Validation
```java
if (timestamp.isBefore(LocalDateTime.now().minusHours(24))) {
    log.warn("Stale data detected for {}: {}", symbol, timestamp);
    // Use cached data or fetch again
}
```

### Symbol Format Validation
```java
// NSE format: SYMBOL
// BSE format: SYMBOL.BSE or SYMBOL
if (!symbol.matches("^[A-Z0-9]+(\\.BSE)?$")) {
    throw new InvalidSymbolException("Invalid symbol format: " + symbol);
}
```

---

## Configuration

### application.yml
```yaml
stock:
  api:
    alpha-vantage:
      enabled: true
      api-key: ${ALPHA_VANTAGE_API_KEY}
      base-url: https://www.alphavantage.co/query
      rate-limit: 5 # per minute
    indian-api:
      enabled: true
      api-key: ${INDIAN_API_KEY}
      endpoint: ${INDIAN_API_ENDPOINT}
  cache:
    enabled: true
    ttl: 300 # seconds (5 minutes)
    max-size: 10000
```

### .env
```properties
ALPHA_VANTAGE_API_KEY=your_api_key_here
INDIAN_API_KEY=your_indian_api_key_here
INDIAN_API_ENDPOINT=https://api.example.com/stocks
```

---

## Testing Strategy

### Unit Tests
- Test each provider in isolation with mocked HTTP responses
- Test validation logic
- Test error handling and retries
- Test caching behavior

### Integration Tests
- Test full flow from controller to third-party API
- Test fallback chain
- Test rate limiting
- Test cache invalidation

### Performance Tests
- Measure API response times
- Test cache hit/miss ratios
- Test system behavior under rate limits

---

## Monitoring & Alerts

### Metrics to Track
- API success rate (target: >95%)
- Average response time (target: <2s)
- Cache hit rate (target: >60%)
- Stale data incidents (target: 0)
- Rate limit violations (target: 0)

### Alerts
- Alert if API success rate <90% for 5 minutes
- Alert if average response time >5s for 5 minutes
- Alert if cache hit rate <40% for 15 minutes
- Alert if stale data detected

---

## Migration Plan

### Step 1: Deploy with Feature Flag
```java
@Value("${stock.api.real-data-enabled:false}")
private boolean realDataEnabled;

if (realDataEnabled) {
    return fetchFromRealAPI(symbol);
} else {
    return fetchFromMock(symbol);
}
```

### Step 2: Gradual Rollout
- Week 1: Enable for 10% of requests
- Week 2: Enable for 50% of requests
- Week 3: Enable for 100% of requests

### Step 3: Monitor & Validate
- Compare real data vs mock data
- Validate portfolio calculations
- Check XIRR accuracy
- User feedback

### Step 4: Remove Mock Code
- Once validated, remove all mock/simulation code
- Update documentation
- Mark task as complete

---

## Success Criteria

✅ Real-time prices from Alpha Vantage/Indian API  
✅ 95%+ API success rate  
✅ <2s average response time  
✅ Zero stale data incidents  
✅ Proper error handling and fallbacks  
✅ Comprehensive test coverage (>80%)  
✅ Monitoring and alerting in place  
✅ User-facing timestamp on all prices  

---

## Next Steps

1. ✅ Enhance AlphaVantageProvider with GLOBAL_QUOTE
2. ✅ Add rate limiting with Bucket4j
3. ✅ Implement Redis caching
4. ✅ Add comprehensive validation
5. ✅ Write unit and integration tests
6. ✅ Deploy with feature flag
7. ✅ Monitor and validate
8. ✅ Update PRODUCT_PROBLEMS.md to mark as RESOLVED
