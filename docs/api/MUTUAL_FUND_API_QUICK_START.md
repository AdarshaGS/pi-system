# Mutual Fund API Integration - Quick Reference

## 🎯 What Was Built

A complete integration with **mfapi.in** third-party API to fetch mutual fund data for the Pi Financial Management System. The external API is now **integrated into the main Mutual Fund Controller** at `/api/v1/mutual-funds`.

---

## 📦 Architecture

### Service Layer
- `MutualFundService` interface - Contains both portfolio and external API methods
- `MutualFundServiceImpl` - Implements both portfolio management and external data fetching
- `MutualFundDataProvider` - Interface for external API provider
- `MFAPIService` - Implementation using mfapi.in

### DTOs (Data Transfer Objects)
Located in `com.externalServices.mutualfund.dto`:
1. `MFSchemeSearchResult.java` - Search results
2. `MFSchemeListItem.java` - Scheme list items
3. `MFSchemeMeta.java` - Scheme metadata
4. `MFNAVData.java` - NAV data points
5. `MFLatestNAVResponse.java` - Latest NAV response
6. `MFNAVHistoryResponse.java` - Historical NAV response

---

## 🚀 API Endpoints Available

**Base**: `/api/v1/mutual-funds`

### Portfolio Management APIs

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/summary?userId={id}` | GET | Get portfolio summary |
| `/holdings?userId={id}` | GET | Get all holdings |
| `/insights?userId={id}` | GET | Get portfolio insights |

### External API - Scheme Discovery

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/external/search?query={term}` | GET | Search mutual fund schemes |
| `/external/schemes?limit={n}&offset={n}` | GET | List all schemes (paginated) |

### External API - NAV Data

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/external/schemes/{code}/nav` | GET | Get NAV history |
| `/external/schemes/{code}/nav?startDate={date}&endDate={date}` | GET | Get NAV with date filter |
| `/external/schemes/{code}/latest` | GET | Get latest NAV |

---

## 💡 Quick Test

```bash
# Start your application
./gradlew bootRun

# Test search endpoint
curl "http://localhost:8080/api/v1/mutual-funds/external/search?query=HDFC"

# Test latest NAV (HDFC Top 100 Fund)
curl "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/latest"

# List schemes (first 50)
curl "http://localhost:8080/api/v1/mutual-funds/external/schemes?limit=50&offset=0"
```

---

## 📱 Mobile App Integration Example

```javascript
// Search for mutual funds
const searchMF = async (query) => {
  const response = await fetch(
    `${API_URL}/api/v1/mutual-funds/external/search?query=${query}`
  );
  return await response.json();
};

// Get latest NAV
const getLatestNAV = async (schemeCode) => {
  const response = await fetch(
    `${API_URL}/api/v1/mutual-funds/external/schemes/${schemeCode}/latest`
  );
  return await response.json();
};

// Get NAV history for charts
const getNAVHistory = async (schemeCode, startDate, endDate) => {
  const response = await fetch(
    `${API_URL}/api/v1/mutual-funds/external/schemes/${schemeCode}/nav?startDate=${startDate}&endDate=${endDate}`
  );
  return await response.json();
};

// Usage
const schemes = await searchMF('HDFC');
const nav = await getLatestNAV(schemes[0].schemeCode);
console.log(`Latest NAV: ₹${nav.data[0].nav}`);
```

---

## 🔧 Configuration

In `application.yml`:
```yaml
external:
  service:
    mfapi:
      base-url: https://api.mfapi.in
```

---

## ✅ Features

✅ No authentication required (Free API)  
✅ Search schemes by name  
✅ List all Indian mutual fund schemes  
✅ Fetch complete NAV history  
✅ Get latest NAV with metadata  
✅ Date range filtering for NAV  
✅ Comprehensive error handling  
✅ Logging for monitoring  
✅ Swagger documentation included  

---

## 📊 Example Response

**Search Result**:
```json
[
  {
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth"
  }
]
```

**Latest NAV**:
```json
{
  "meta": {
    "fundHouse": "HDFC Mutual Fund",
    "schemeType": "Open Ended Schemes",
    "schemeCategory": "Equity Scheme - Large Cap Fund",
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth"
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

## 🎓 Next Steps for Mobile App

1. **Add MF Search Screen**
   - Use `/search` endpoint
   - Display results in list

2. **Add to Portfolio**
   - Get scheme code from search
   - Fetch latest NAV
   - Save transaction

3. **Portfolio Tracking**
   - Fetch latest NAV for all holdings
   - Calculate returns

4. **Historical Charts**
   - Use `/nav` endpoint with date range
   - Plot NAV trends

---

## 📖 Full Documentation

See [EXTERNAL_MUTUAL_FUND_API_INTEGRATION.md](./EXTERNAL_MUTUAL_FUND_API_INTEGRATION.md) for complete details.

---

**API Provider**: mfapi.in  
**Documentation**: https://www.mfapi.in/docs/  
**Status**: ✅ Ready for use  
**Date**: February 1, 2026
