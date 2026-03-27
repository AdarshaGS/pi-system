# 🧾 Tax Management API Quick Reference

## Base URL
```
/api/v1/tax
```

---

## 📊 Tax Regime Comparison

### Compare Old vs New Regime
```http
GET /api/v1/tax/{userId}/regime-comparison?financialYear={FY}&grossIncome={amount}
```

**Example:**
```bash
GET /api/v1/tax/123/regime-comparison?financialYear=2025-26&grossIncome=1200000
```

**Response:**
- Tax calculation for both regimes
- Recommendation on which to choose
- Potential savings amount

---

## 💰 Capital Gains Management

### Record Capital Gains Transaction
```http
POST /api/v1/tax/{userId}/capital-gains
```

**Body:**
```json
{
  "financialYear": "2025-26",
  "assetType": "LISTED_EQUITY",
  "assetName": "Stock/Fund Name",
  "purchaseDate": "2023-01-15",
  "saleDate": "2026-01-20",
  "quantity": 10,
  "purchasePrice": 2500,
  "salePrice": 3000
}
```

**Asset Types:**
- `LISTED_EQUITY` - Listed stocks
- `EQUITY_MUTUAL_FUND` - Equity MFs
- `DEBT_MUTUAL_FUND` - Debt MFs
- `ETF` - Exchange Traded Funds
- `BONDS` - Bonds/Debentures
- `GOLD` - Gold/Gold Bonds
- `REAL_ESTATE` - Property
- `OTHER` - Other assets

### Get Capital Gains Summary
```http
GET /api/v1/tax/{userId}/capital-gains/summary?financialYear={FY}
```

**Returns:**
- Total STCG and tax
- Total LTCG and tax
- Transaction-wise breakdown
- ₹1L exemption usage

### Calculate Capital Gains (Preview)
```http
POST /api/v1/tax/capital-gains/calculate
```
*Preview calculation without saving*

---

## 💡 Tax Saving Recommendations

### Get Recommendations
```http
GET /api/v1/tax/{userId}/recommendations?financialYear={FY}
```

**Returns:**
- Available deduction limits
- Recommended investments
- Potential tax savings
- Suggested instruments

### Record Tax Saving Investment
```http
POST /api/v1/tax/{userId}/tax-savings
```

**Body:**
```json
{
  "financialYear": "2025-26",
  "section": "SECTION_80C",
  "investmentName": "PPF",
  "amount": 100000,
  "investmentDate": "2025-04-15",
  "referenceNumber": "PPF123456"
}
```

**Sections:**
- `SECTION_80C` - PPF, ELSS, LIC (₹1.5L)
- `SECTION_80D` - Health Insurance (₹25K/50K)
- `SECTION_80CCD_1B` - NPS (₹50K)
- `SECTION_80E` - Education Loan
- `SECTION_80G` - Donations
- `SECTION_24B` - Home Loan Interest (₹2L)
- Others...

### Get Tax Savings List
```http
GET /api/v1/tax/{userId}/tax-savings?financialYear={FY}
```

---

## 📑 TDS Tracking

### Record TDS Entry
```http
POST /api/v1/tax/{userId}/tds
```

**Body:**
```json
{
  "financialYear": "2025-26",
  "deductorName": "XYZ Company",
  "deductorTan": "ABCD12345E",
  "tdsAmount": 25000,
  "incomeAmount": 100000,
  "tdsSection": "194J",
  "deductionDate": "2025-06-30",
  "certificateNumber": "CERT123"
}
```

### Get TDS Reconciliation
```http
GET /api/v1/tax/{userId}/tds/reconciliation?financialYear={FY}
```

**Returns:**
- Total TDS deducted
- Verified vs claimed amounts
- Unclaimed balance
- Recommendations

### Update TDS Status
```http
PUT /api/v1/tax/tds/{tdsId}/status?status={STATUS}
```

**Statuses:**
- `PENDING` - Awaiting verification
- `VERIFIED` - Verified against 26AS
- `CLAIMED` - Claimed in ITR
- `MISMATCH` - Mismatch found

---

## 📈 Tax Projections

### Get Current FY Projection
```http
GET /api/v1/tax/{userId}/projection?financialYear={FY}
```

**Returns:**
- Projected income and tax
- Balance payable/refundable
- Month-wise payment plan
- Planning advice

---

## 📤 ITR Data Export

### Get ITR Pre-fill Data
```http
GET /api/v1/tax/{userId}/itr-prefill?financialYear={FY}
```

**Returns:**
- Complete income details
- All deductions
- Capital gains schedule
- TDS details
- Tax computation
- Ready for ITR filing

---

## 🔑 Common Parameters

### Financial Year Format
```
"2025-26" (April 2025 to March 2026)
"2024-25" (April 2024 to March 2025)
```

### Date Format
```
"2025-04-15" (YYYY-MM-DD)
```

### Currency
All amounts in INR (Indian Rupees)

---

## 📊 Tax Rates Reference

### STCG Rates
- Listed Equity/Equity MF: **15%**
- Debt MF/Bonds: **Slab rate**
- Real Estate: **Slab rate**

### LTCG Rates
- Listed Equity/Equity MF: **10%** (after ₹1L exemption)
- Debt MF (>36 months): **20%** with indexation
- Real Estate: **20%** with indexation

### Holding Period for LTCG
- Equity/Equity MF/ETF: **> 12 months**
- Debt MF/Bonds: **> 36 months**
- Real Estate: **> 24 months**

---

## 🎯 Best Practices

### Recording Transactions
1. Record capital gains as soon as sale happens
2. Update TDS entries quarterly
3. Track tax savings monthly
4. Review regime comparison before year-end

### Tax Planning
1. Check recommendations by December
2. Make 80C investments before March 31
3. Verify TDS against Form 26AS
4. Export ITR data in April/May

### Reconciliation
1. Update TDS status after 26AS verification
2. Mark investments as claimed after ITR filing
3. Track balance tax payable monthly
4. Plan advance tax payments

---

## 🚨 Important Dates

### Advance Tax Due Dates
- 15 June: 15% of tax
- 15 September: 45% of tax (cumulative)
- 15 December: 75% of tax (cumulative)
- 15 March: 100% of tax (cumulative)

### Year-end Deadlines
- 31 March: Complete tax-saving investments
- 31 July: ITR filing deadline

---

## 💬 Support

For tax calculation queries, refer to:
- [Income Tax Act, 1961](https://www.incometax.gov.in/)
- [Tax calculators](https://www.incometaxindia.gov.in/pages/tools/tax-calculator.aspx)

---

**Last Updated:** February 1, 2026  
**API Version:** v1
