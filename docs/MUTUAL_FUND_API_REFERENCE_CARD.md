# 🚀 Mutual Fund API - Quick Reference Card

## Base URL
```
http://localhost:8080/api/v1/mutual-funds
```

---

## 📊 Portfolio APIs (Authenticated)

| Endpoint | Description |
|----------|-------------|
| `GET /summary?userId={id}` | Portfolio summary with totals & XIRR |
| `GET /holdings?userId={id}` | All mutual fund holdings |
| `GET /insights?userId={id}` | Asset allocation & risk analysis |

**Example**:
```bash
curl "http://localhost:8080/api/v1/mutual-funds/summary?userId=1"
```

---

## 🌐 External API (Public Data from mfapi.in)

### Search & Discovery

| Endpoint | Description |
|----------|-------------|
| `GET /external/search?query={term}` | Search schemes by name |
| `GET /external/schemes?limit={n}&offset={n}` | List all schemes (paginated) |

**Examples**:
```bash
# Search for HDFC schemes
curl "http://localhost:8080/api/v1/mutual-funds/external/search?query=HDFC"

# List first 50 schemes
curl "http://localhost:8080/api/v1/mutual-funds/external/schemes?limit=50&offset=0"
```

### NAV Data

| Endpoint | Description |
|----------|-------------|
| `GET /external/schemes/{code}/latest` | Latest NAV with metadata |
| `GET /external/schemes/{code}/nav` | Complete NAV history |
| `GET /external/schemes/{code}/nav?startDate={start}&endDate={end}` | NAV history (filtered) |

**Examples**:
```bash
# Latest NAV for HDFC Top 100 Fund (scheme code: 125497)
curl "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/latest"

# NAV history for 2023
curl "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/nav?startDate=2023-01-01&endDate=2023-12-31"
```

---

## 📱 Mobile App Integration

```javascript
const API_BASE = 'http://localhost:8080/api/v1/mutual-funds';

// Search schemes
const searchSchemes = (query) => 
  fetch(`${API_BASE}/external/search?query=${query}`).then(r => r.json());

// Get latest NAV
const getLatestNAV = (schemeCode) => 
  fetch(`${API_BASE}/external/schemes/${schemeCode}/latest`).then(r => r.json());

// Get portfolio summary
const getPortfolioSummary = (userId) => 
  fetch(`${API_BASE}/summary?userId=${userId}`).then(r => r.json());

// Usage
const schemes = await searchSchemes('HDFC');
const nav = await getLatestNAV(schemes[0].schemeCode);
console.log(`Latest NAV: ₹${nav.data[0].nav}`);
```

---

## 📦 Response Examples

### Search Result
```json
[
  {
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth"
  }
]
```

### Latest NAV
```json
{
  "meta": {
    "fundHouse": "HDFC Mutual Fund",
    "schemeCode": 125497,
    "schemeName": "HDFC Top 100 Fund - Direct Plan - Growth"
  },
  "data": [
    {
      "date": "31-01-2026",
      "nav": "895.234"
    }
  ],
  "status": "SUCCESS"
}
```

---

## 🎯 Common Use Cases

### 1. Add New Mutual Fund to Portfolio
```javascript
// Step 1: Search for scheme
const schemes = await searchSchemes('SBI Bluechip');

// Step 2: Get latest NAV
const nav = await getLatestNAV(schemes[0].schemeCode);

// Step 3: Create transaction with current NAV as purchase price
const transaction = {
  schemeCode: schemes[0].schemeCode,
  units: 100,
  nav: nav.data[0].nav,
  date: new Date()
};
```

### 2. Display NAV Chart
```javascript
// Fetch NAV history for last year
const history = await fetch(
  `${API_BASE}/external/schemes/125497/nav?startDate=2023-01-01&endDate=2023-12-31`
).then(r => r.json());

// Plot chart with history.data
const chartData = history.data.map(item => ({
  date: item.date,
  value: parseFloat(item.nav)
}));
```

### 3. Track Portfolio Performance
```javascript
// Get portfolio holdings
const holdings = await fetch(
  `${API_BASE}/holdings?userId=1`
).then(r => r.json());

// For each holding, fetch latest NAV to update current value
for (const holding of holdings) {
  const latest = await getLatestNAV(holding.schemeCode);
  holding.currentNAV = latest.data[0].nav;
}
```

---

## 🧪 Testing

### Start Server
```bash
./gradlew bootRun
```

### Test Endpoints
```bash
# Portfolio APIs (replace userId)
curl "http://localhost:8080/api/v1/mutual-funds/summary?userId=1"

# External APIs
curl "http://localhost:8080/api/v1/mutual-funds/external/search?query=HDFC"
curl "http://localhost:8080/api/v1/mutual-funds/external/schemes/125497/latest"
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```
Search for: **"Mutual Fund Management"**

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| `MUTUAL_FUND_INTEGRATION_SUMMARY.md` | Implementation overview |
| `EXTERNAL_MUTUAL_FUND_API_INTEGRATION.md` | Complete API reference |
| `MUTUAL_FUND_API_QUICK_START.md` | Quick start guide |

---

## ⚠️ Important Notes

1. **Rate Limiting**: mfapi.in applies rate limiting. Implement caching for production.
2. **NAV Updates**: NAV typically updates once per day around 10 PM IST.
3. **Scheme Codes**: Use search API to get valid scheme codes before fetching NAV.
4. **Authentication**: External APIs are public. Portfolio APIs require authentication.

---

## 🎓 Best Practices

✅ Cache search results (schemes list changes infrequently)  
✅ Cache NAV data (updates once daily)  
✅ Use pagination for large scheme lists  
✅ Validate scheme codes before fetching NAV  
✅ Handle API errors gracefully  
✅ Show loading states in UI

---

**Data Provider**: mfapi.in (Free & Open API)  
**Last Updated**: February 1, 2026  
**Status**: ✅ Production Ready
