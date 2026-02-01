# Mutual Fund External API Integration - MFAPI.in

## Overview

This document describes the integration of the **mfapi.in** third-party API for fetching mutual fund data in the Pi System application. The external API functionality is **integrated into the main Mutual Fund Service and Controller** at `/api/v1/mutual-funds`.

---

## API Provider

**Provider**: mfapi.in  
**Documentation**: https://www.mfapi.in/docs/  
**Base URL**: `https://api.mfapi.in`  
**Authentication**: None required (Free & Open API)  
**Rate Limiting**: Yes (applied by provider)  
**Data Coverage**: Indian Mutual Funds (All schemes, NAV history)

---

## Features

✅ **Search Mutual Fund Schemes** - Search by fund house name, scheme name  
✅ **List All Schemes** - Paginated listing of all available schemes  
✅ **NAV History** - Complete historical NAV data for any scheme  
✅ **Latest NAV** - Get the most recent NAV with metadata  
✅ **Date Range Filtering** - Fetch NAV within specific date ranges  
✅ **Integrated Architecture** - Single controller for portfolio and external API

---

## Architecture

### Package Structure

```
com.investments.mutualfunds/
├── controller/
│   └── MutualFundController.java           # Unified REST API (Portfolio + External)
├── service/
│   ├── MutualFundService.java              # Unified service interface
│   ├── MutualFundServiceImpl.java          # Implementation with external API
│   └── MutualFundFetchService.java         # Portfolio data fetching
└── data/
    ├── MutualFundHolding.java
    ├── MutualFundSummary.java
    └── MutualFundInsights.java

com.externalServices.mutualfund/
├── service/
│   ├── MutualFundDataProvider.java         # External API interface
│   └── MFAPIService.java                   # mfapi.in implementation
└── dto/
    ├── MFSchemeSearchResult.java           # Search result DTO
    ├── MFSchemeListItem.java               # Scheme list item DTO
    ├── MFSchemeMeta.java                   # Scheme metadata DTO
    ├── MFNAVData.java                      # NAV data point DTO
    ├── MFLatestNAVResponse.java            # Latest NAV response DTO
    └── MFNAVHistoryResponse.java           # NAV history response DTO
```

### Design Pattern

This integration follows the **Unified Service Pattern**:

1. **Unified Controller** (`MutualFundController`) - Single controller for all mutual fund operations
2. **Unified Service** (`MutualFundService`) - Interface for both portfolio management and external data
3. **Provider Pattern** (`MutualFundDataProvider`) - Abstraction for external data sources
4. **Implementation** (`MFAPIService`) - Concrete implementation using mfapi.in
5. **DTOs** - Type-safe data transfer objects matching API responses

---

## API Endpoints

### Base Path: `/api/v1/mutual-funds`

---

### Portfolio Management APIs

#### 1. Get Portfolio Summary

```http
GET /api/v1/mutual-funds/summary?userId={userId}
```

**Description**: Get mutual fund portfolio summary with totals and XIRR

---

#### 2. Get Holdings

```http
GET /api/v1/mutual-funds/holdings?userId={userId}
```

**Description**: Get all mutual fund holdings for a user

---

#### 3. Get Insights

```http
GET /api/v1/mutual-funds/insights?userId={userId}
```

**Description**: Get portfolio insights including asset allocation and risk analysis

---

### External API - Scheme Discovery

#### 1. Search Schemes

```http
GET /api/v1/mutual-funds/external/search?query={searchTerm}
```

**Parameters**:
- `query` (required): Search term (e.g., "HDFC", "SBI", "Axis")

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/search?query=HDFC"
```

**Response**:
```json
[
  {
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth"
  },
  {
    "schemeCode": 125498,
    "schemeName": "HDFC Top 100 Fund - Regular Plan - Growth"
  }
]
```

---

#### 2. List All Schemes

```http
GET /api/v1/mutual-funds/external/schemes?limit={limit}&offset={offset}
```

**Parameters**:
- `limit` (optional): Results per page (default: 100, max: 500)
- `offset` (optional): Pagination offset (default: 0)

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes?limit=50&offset=0"
```

**Response**:
```json
[
  {
    "schemeCode": 100001,
    "schemeName": "Aditya Birla Sun Life Arbitrage Fund - Direct Plan - Growth"
  },
  {
    "schemeCode": 100002,
    "schemeName": "Aditya Birla Sun Life Banking & PSU Debt Fund - Direct Plan - Growth"
  }
]
```

---

### External API - NAV Data

#### 3. Get NAV History

```http
GET /api/v1/mutual-funds/external/schemes/{schemeCode}/nav
GET /api/v1/mutual-funds/external/schemes/{schemeCode}/nav?startDate={start}&endDate={end}
```

**Parameters**:
- `schemeCode` (required): Unique scheme identifier
- `startDate` (optional): Start date in YYYY-MM-DD format
- `endDate` (optional): End date in YYYY-MM-DD format

**Example Request (Full History)**:
```bash
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/nav"
```

**Example Request (Date Range)**:
```bash
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/nav?startDate=2023-01-01&endDate=2023-12-31"
```

**Response**:
```json
{
  "meta": {
    "fundHouse": "HDFC Mutual Fund",
    "schemeType": "Open Ended Schemes",
    "schemeCategory": "Equity Scheme - Large Cap Fund",
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth",
    "isinGrowth": "INF179K01BB2",
    "isinDivReinvestment": null
  },
  "data": [
    {
      "date": "31-01-2026",
      "nav": "895.23400"
    },
    {
      "date": "30-01-2026",
      "nav": "892.45600"
    }
  ],
  "status": "SUCCESS"
}
```

---

#### 4. Get Latest NAV

```http
GET /api/v1/mutual-funds/external/schemes/{schemeCode}/latest
```

**Parameters**:
- `schemeCode` (required): Unique scheme identifier

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/latest"
```

**Response**:
```json
{
  "meta": {
    "fundHouse": "HDFC Mutual Fund",
    "schemeType": "Open Ended Schemes",
    "schemeCategory": "Equity Scheme - Large Cap Fund",
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth",
    "isinGrowth": "INF179K01BB2",
    "isinDivReinvestment": null
  },
  "data": [
    {
      "date": "31-01-2026",
      "nav": "895.23400"
    }
  ],
  "status": "SUCCESS"
}
```

---

## Configuration

### Application Properties

Add to `src/main/resources/application.yml`:

```yaml
external:
  service:
    mfapi:
      base-url: https://api.mfapi.in
```

The base URL is configurable and defaults to `https://api.mfapi.in` if not specified.

---

## Usage Examples

### From Mobile App (React Native)

```javascript
// Search for HDFC mutual funds
const searchMutualFunds = async (query) => {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/mutual-funds/external/search?query=${query}`
  );
  const schemes = await response.json();
  return schemes;
};

// Get latest NAV for a scheme
const getLatestNAV = async (schemeCode) => {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/mutual-funds/external/schemes/${schemeCode}/latest`
  );
  const navData = await response.json();
  return navData;
};

// Example usage
const schemes = await searchMutualFunds('HDFC');
console.log(schemes); // [{ schemeCode: 125497, schemeName: "..." }, ...]

const latestNav = await getLatestNAV(125497);
console.log(latestNav.data[0].nav); // "895.23400"
```

### From Java Service

```java
@Service
public class MyMutualFundService {
    
    private final MutualFundService mutualFundService;
    
    public MyMutualFundService(MutualFundService mutualFundService) {
        this.mutualFundService = mutualFundService;
    }
    
    public void processMutualFundData() {
        // Search for schemes
        List<MFSchemeSearchResult> schemes = mutualFundService.searchSchemes("HDFC");
        
        // Get latest NAV for first scheme
        if (!schemes.isEmpty()) {
            Long schemeCode = schemes.get(0).getSchemeCode();
            MFLatestNAVResponse latestNav = mutualFundService.getLatestNAV(schemeCode);
            
            String navValue = latestNav.getData().get(0).getNav();
            System.out.println("Latest NAV: " + navValue);
        }
    }
}
```

---

## Error Handling

The service handles errors gracefully and wraps them in `RuntimeException` with descriptive messages:

**Common Errors**:

1. **Invalid Scheme Code** (404)
   - Message: "Failed to fetch NAV for scheme: {schemeCode}"
   
2. **API Timeout/Connection Error** (500)
   - Message: "Failed to search mutual fund schemes: Connection timeout"
   
3. **Rate Limiting** (429)
   - The external API applies rate limiting. Consider caching responses.

**Logging**:
- All API calls are logged at INFO level
- Errors are logged at ERROR level with full stack trace

---

## Performance Considerations

### Caching Strategy

Since mfapi.in applies rate limiting, implement caching:

1. **Scheme List** - Cache for 24 hours (changes infrequently)
2. **Latest NAV** - Cache for 1 hour (updates once per day around 10 PM IST)
3. **NAV History** - Cache indefinitely (historical data doesn't change)

### Example Caching Implementation

```java
@Service
public class CachedMFAPIService implements MutualFundDataProvider {
    
    @Cacheable(value = "mf-schemes", key = "#query")
    public List<MFSchemeSearchResult> searchSchemes(String query) {
        return mfApiService.searchSchemes(query);
    }
    
    @Cacheable(value = "mf-latest-nav", key = "#schemeCode", ttl = 3600)
    public MFLatestNAVResponse getLatestNAV(Long schemeCode) {
        return mfApiService.getLatestNAV(schemeCode);
    }
}
```

---

## Testing

### Unit Tests

Create tests in `src/test/java/com/externalServices/mutualfund/`:

```java
@SpringBootTest
class MFAPIServiceTest {
    
    @Autowired
    private MutualFundDataProvider mfDataProvider;
    
    @Test
    void testSearchSchemes() {
        List<MFSchemeSearchResult> results = mfDataProvider.searchSchemes("HDFC");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
    
    @Test
    void testGetLatestNAV() {
        MFLatestNAVResponse response = mfDataProvider.getLatestNAV(125497L);
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
    }
}
```

### Manual Testing with cURL

```bash
# Search schemes
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/search?query=SBI"

# List all schemes (first 10)
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes?limit=10&offset=0"

# Get latest NAV
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/latest"

# Get NAV history with date range
curl -X GET "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/nav?startDate=2023-01-01&endDate=2023-12-31"
```

---

## Integration with Mobile App

### Recommended Usage Flow

1. **Scheme Discovery**:
   - User searches for mutual funds by name
   - App calls `/search?query={fundName}`
   - Display results with scheme names and codes

2. **Add to Portfolio**:
   - User selects a scheme from search results
   - Get scheme code and latest NAV
   - Save transaction with current NAV as purchase price

3. **Portfolio Tracking**:
   - Periodically fetch latest NAV for all holdings
   - Calculate current value and returns
   - Display in portfolio screen

4. **Historical Charts**:
   - Fetch NAV history for selected scheme
   - Plot line chart showing NAV trends over time
   - Allow user to select date range

---

## Next Steps

### Future Enhancements

1. **Bulk NAV Fetch** ✨
   - Create endpoint to fetch latest NAV for multiple schemes in one call
   - Reduces API calls for portfolio tracking

2. **Scheme Recommendations** 🎯
   - Filter schemes by category (Equity, Debt, Hybrid)
   - Sort by returns, ratings, AUM

3. **SIP Calculator** 📊
   - Use NAV history to calculate SIP returns
   - Provide investment insights

4. **Webhook Integration** 🔔
   - Set up webhooks for NAV updates
   - Real-time notifications to users

5. **Advanced Analytics** 📈
   - Calculate XIRR using NAV history
   - Compare scheme performance
   - Risk analysis

---

## Support & Documentation

**External API Documentation**: https://www.mfapi.in/docs/  
**Swagger UI**: http://localhost:8080/swagger-ui.html  
**API Tags**: "External Mutual Fund API"

For questions or issues, refer to the mfapi.in documentation or contact the development team.

---

**Last Updated**: February 1, 2026  
**Version**: 1.0.0
