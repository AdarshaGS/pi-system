# 🧾 Tax Module - Complete API Reference

> **Status**: 100% Complete (Feb 2, 2026)  
> **Base URL**: `/api/v1/tax`  
> **Authentication**: Required (JWT Token)

---

## 📋 Table of Contents

1. [Basic Tax Management](#basic-tax-management)
2. [Tax Regime Comparison](#tax-regime-comparison)
3. [Capital Gains Management](#capital-gains-management)
4. [Tax Saving Recommendations](#tax-saving-recommendations)
5. [TDS Tracking & Reconciliation](#tds-tracking--reconciliation)
6. [Tax Projections](#tax-projections)
7. [ITR Pre-fill Data Export](#itr-pre-fill-data-export)
8. [Auto-Population APIs](#auto-population-apis)
9. [Advanced Tax Calculations](#advanced-tax-calculations)
10. [ITR Generation & Export](#itr-generation--export)

---

## Basic Tax Management

### Create or Update Tax Details
```http
POST /api/v1/tax
```

**Request Body:**
```json
{
  "userId": 1,
  "financialYear": "2025-26",
  "grossSalary": 1200000,
  "businessIncome": 500000,
  "capitalGainsShortTerm": 50000,
  "capitalGainsLongTerm": 100000,
  "otherIncome": 25000,
  "section80CDeductions": 150000,
  "section80DDeductions": 25000,
  "otherDeductions": 50000,
  "advanceTaxPaid": 50000,
  "taxPaid": 100000,
  "selectedRegime": "OLD"
}
```

### Get Tax Details
```http
GET /api/v1/tax/{userId}?financialYear=2025-26
```

### Get Outstanding Tax Liability
```http
GET /api/v1/tax/{userId}/liability
```

---

## Tax Regime Comparison

### Compare Old vs New Tax Regime
```http
GET /api/v1/tax/{userId}/regime-comparison?financialYear=2025-26&grossIncome=1200000
```

**Response:**
```json
{
  "oldRegimeTaxableIncome": 1000000,
  "oldRegimeTax": 112500,
  "oldRegimeTotalTax": 117000,
  "newRegimeTaxableIncome": 1150000,
  "newRegimeTax": 125000,
  "newRegimeTotalTax": 130000,
  "taxSavings": -13000,
  "recommendedRegime": "NEW",
  "recommendation": "Consider switching to New Regime to save ₹13,000"
}
```

---

## Capital Gains Management

### Record Capital Gains Transaction
```http
POST /api/v1/tax/{userId}/capital-gains
```

**Request Body:**
```json
{
  "assetType": "STOCK",
  "assetName": "Reliance Industries",
  "assetSymbol": "RELIANCE",
  "quantity": 10,
  "purchaseDate": "2023-01-15",
  "purchasePrice": 2400,
  "saleDate": "2025-06-20",
  "salePrice": 2800,
  "expenses": 500,
  "financialYear": "2025-26"
}
```

**Response:** Auto-calculated with STCG/LTCG classification and tax amount.

### Get Capital Gains Summary
```http
GET /api/v1/tax/{userId}/capital-gains/summary?financialYear=2025-26
```

**Response:**
```json
{
  "totalSTCG": 50000,
  "totalLTCG": 150000,
  "totalSTCGTax": 7500,
  "totalLTCGTax": 5000,
  "ltcgExemption": 100000,
  "transactionsCount": 15,
  "stcgDetails": [...],
  "ltcgDetails": [...]
}
```

### List Capital Gains Transactions
```http
GET /api/v1/tax/{userId}/capital-gains/transactions?financialYear=2025-26
```

### Calculate Capital Gains (Preview)
```http
POST /api/v1/tax/capital-gains/calculate
```
*Preview calculation without saving to database.*

---

## Tax Saving Recommendations

### Get Personalized Recommendations
```http
GET /api/v1/tax/{userId}/recommendations?financialYear=2025-26
```

**Response:**
```json
{
  "currentIncome": 1200000,
  "current80CInvestments": 100000,
  "available80CLimit": 50000,
  "current80DInvestments": 15000,
  "available80DLimit": 10000,
  "potentialTaxSavings": 31200,
  "recommendations": [
    {
      "section": "80C",
      "instrument": "ELSS Mutual Funds",
      "suggestedAmount": 50000,
      "taxSaving": 15600
    }
  ]
}
```

### Record Tax Saving Investment
```http
POST /api/v1/tax/{userId}/tax-savings
```

**Request Body:**
```json
{
  "investmentType": "80C",
  "category": "ELSS",
  "investmentName": "HDFC Tax Saver Fund",
  "amount": 50000,
  "investmentDate": "2025-12-15",
  "financialYear": "2025-26",
  "linkedEntityType": "MUTUAL_FUND",
  "linkedEntityId": 123
}
```

### List Tax Saving Investments
```http
GET /api/v1/tax/{userId}/tax-savings?financialYear=2025-26
```

---

## TDS Tracking & Reconciliation

### Record TDS Entry
```http
POST /api/v1/tax/{userId}/tds
```

**Request Body:**
```json
{
  "financialYear": "2025-26",
  "quarter": 4,
  "deductorName": "ABC Corp Ltd",
  "deductorTan": "MUMA01234D",
  "section": "192",
  "incomeType": "SALARY",
  "amountPaid": 300000,
  "tdsDeducted": 30000,
  "certificateNumber": "TDS/2026/12345",
  "certificateDate": "2026-01-15"
}
```

### List TDS Entries
```http
GET /api/v1/tax/{userId}/tds?financialYear=2025-26
```

### Get TDS Reconciliation Report
```http
GET /api/v1/tax/{userId}/tds/reconciliation?financialYear=2025-26
```

**Response:**
```json
{
  "totalTDSClaimed": 120000,
  "totalTDSMatched": 115000,
  "totalMismatch": 5000,
  "pendingReconciliation": 2,
  "reconciliationStatus": "PARTIAL",
  "recommendations": [
    "Verify TDS with deductor ABC Corp Ltd (₹5,000 mismatch)"
  ]
}
```

### Update TDS Status
```http
PUT /api/v1/tax/tds/{tdsId}/status?status=MATCHED
```

---

## Tax Projections

### Get Month-wise Tax Projection
```http
GET /api/v1/tax/{userId}/projection?financialYear=2025-26
```

**Response:**
```json
{
  "projectedGrossIncome": 1200000,
  "projectedTaxableIncome": 1000000,
  "projectedTaxLiability": 117000,
  "advanceTaxPaid": 50000,
  "remainingTaxLiability": 67000,
  "nextAdvanceTaxDue": "2026-03-15",
  "recommendedMonthlyInvestment": 12500,
  "monthlyRecommendations": [...]
}
```

---

## ITR Pre-fill Data Export

### Get Complete ITR Pre-fill Data
```http
GET /api/v1/tax/{userId}/itr-prefill?financialYear=2025-26
```

**Response:** Comprehensive DTO with all income sources, deductions, TDS, and tax computation.

---

## Auto-Population APIs

### Auto-Populate All Tax Data
```http
POST /api/v1/tax/auto-populate/{userId}/all?financialYear=2025-26
```

**Response:**
```json
{
  "status": "success",
  "message": "All tax data auto-populated successfully",
  "capitalGainsCount": 12,
  "80CInvestmentsCount": 5,
  "80DInvestmentsCount": 2
}
```

### Auto-Populate Capital Gains
```http
POST /api/v1/tax/auto-populate/{userId}/capital-gains?financialYear=2025-26
```
*Fetches portfolio transactions and auto-calculates STCG/LTCG.*

### Auto-Populate Salary Income
```http
POST /api/v1/tax/auto-populate/{userId}/salary-income?financialYear=2025-26
```
*Fetches salary slips from income module.*

### Auto-Populate Interest Income
```http
POST /api/v1/tax/auto-populate/{userId}/interest-income?financialYear=2025-26
```
*Aggregates interest from FD and savings accounts.*

### Auto-Populate Dividend Income
```http
POST /api/v1/tax/auto-populate/{userId}/dividend-income?financialYear=2025-26
```
*Calculates dividend from stock holdings.*

### Auto-Populate 80C Investments
```http
POST /api/v1/tax/auto-populate/{userId}/80c-investments?financialYear=2025-26
```
*Links FD, insurance, PPF, ELSS, home loan principal.*

### Auto-Populate 80D Investments
```http
POST /api/v1/tax/auto-populate/{userId}/80d-investments?financialYear=2025-26
```
*Fetches health insurance premiums.*

### Auto-Populate Home Loan Interest
```http
POST /api/v1/tax/auto-populate/{userId}/home-loan-interest?financialYear=2025-26
```
*Calculates interest for 24B and 80EEA deductions.*

---

## Advanced Tax Calculations

### Calculate House Property Income
```http
POST /api/v1/tax/calculations/house-property
```

**Request Body:**
```json
{
  "propertyType": "LET_OUT",
  "annualRent": 240000,
  "municipalTaxes": 12000,
  "interestOnHomeLoan": 180000
}
```

**Response:**
```json
{
  "grossAnnualValue": 240000,
  "netAnnualValue": 228000,
  "standardDeduction": 68400,
  "incomeFromHouseProperty": -20400
}
```

### Calculate Business Income
```http
POST /api/v1/tax/calculations/business-income
```

**Request Body:**
```json
{
  "taxationScheme": "NORMAL",
  "grossReceipts": 5000000,
  "salariesAndWages": 1500000,
  "rent": 300000,
  "depreciation": 200000,
  "interestOnBorrowedCapital": 100000,
  "otherExpenses": 800000
}
```

**Response:**
```json
{
  "totalExpenses": 2900000,
  "incomeFromBusiness": 2100000
}
```

### Process Loss Set-Off
```http
POST /api/v1/tax/calculations/loss-setoff
```

**Request Body:**
```json
{
  "salaryIncome": 800000,
  "housePropertyLoss": 250000,
  "businessLoss": 100000,
  "capitalLossSTCG": 50000,
  "capitalGainLTCG": 200000
}
```

**Response:**
```json
{
  "hpSetOffUsed": 200000,
  "hpLossCarriedForward": 50000,
  "businessSetOffUsed": 100000,
  "stcgSetOffUsed": 50000,
  "netTaxableIncome": 550000
}
```

### Calculate Complete Tax
```http
POST /api/v1/tax/calculations/complete-tax
```

**Request Body:**
```json
{
  "totalIncome": 5500000,
  "regime": "OLD",
  "deductions": 200000
}
```

**Response:**
```json
{
  "taxableIncome": 5300000,
  "baseTax": 1362500,
  "surcharge": 136250,
  "cess": 59950,
  "rebate87A": 0,
  "totalTaxLiability": 1558700
}
```

### Calculate Rebate 87A
```http
GET /api/v1/tax/calculations/rebate-87a?totalIncome=450000&regime=OLD
```

### Calculate Surcharge
```http
GET /api/v1/tax/calculations/surcharge?taxAmount=500000&totalIncome=7500000
```

### Calculate Health & Education Cess
```http
GET /api/v1/tax/calculations/cess?taxAfterSurcharge=550000
```

---

## ITR Generation & Export

### Build ITR-1 Data
```http
GET /api/v1/tax/itr/{userId}/itr1?financialYear=2025-26
```

**Response:** Complete ITR-1 DTO for Sahaj form.

### Generate ITR-1 JSON
```http
GET /api/v1/tax/itr/{userId}/itr1/json?financialYear=2025-26
```

**Response:** Downloadable ITR-1 JSON file ready for Income Tax Portal upload.

### Build ITR-2 Data
```http
GET /api/v1/tax/itr/{userId}/itr2?financialYear=2025-26
```

**Response:** Complete ITR-2 DTO with capital gains and multiple properties.

### Generate ITR-2 JSON
```http
GET /api/v1/tax/itr/{userId}/itr2/json?financialYear=2025-26
```

**Response:** Downloadable ITR-2 JSON file.

### Import Form 16
```http
POST /api/v1/tax/itr/{userId}/form16/import?financialYear=2025-26
Content-Type: multipart/form-data

file: <form16.pdf>
fileType: PDF
```

**Response:**
```json
{
  "status": "success",
  "message": "Form 16 imported successfully"
}
```

### Import Form 26AS
```http
POST /api/v1/tax/itr/{userId}/form26as/import?financialYear=2025-26
Content-Type: multipart/form-data

file: <form26as.pdf>
fileType: PDF
```

### Sync with AIS
```http
POST /api/v1/tax/itr/{userId}/ais/sync?financialYear=2025-26
```

**Response:**
```json
{
  "status": "success",
  "message": "AIS data synced successfully"
}
```

### Check ITR Filing Readiness
```http
GET /api/v1/tax/itr/{userId}/filing-readiness?financialYear=2025-26
```

**Response:**
```json
{
  "status": "ready",
  "message": "All required data is available for ITR filing",
  "missingFields": [],
  "warnings": []
}
```

---

## 📊 Complete Tax API Summary

| Feature Category | Endpoints | Status |
|-----------------|-----------|--------|
| Basic Tax Management | 3 | ✅ Complete |
| Tax Regime Comparison | 1 | ✅ Complete |
| Capital Gains | 4 | ✅ Complete |
| Tax Savings | 3 | ✅ Complete |
| TDS Tracking | 4 | ✅ Complete |
| Tax Projections | 1 | ✅ Complete |
| ITR Pre-fill | 1 | ✅ Complete |
| Auto-Population | 8 | ✅ Complete |
| Advanced Calculations | 7 | ✅ Complete |
| ITR Generation | 8 | ✅ Complete |
| **Total** | **40** | **100%** |

---

## 🎯 Key Features

✅ **Capital Gains Auto-Calculation** - Automatic STCG/LTCG computation  
✅ **Tax Regime Comparison** - Old vs New with recommendations  
✅ **TDS Reconciliation** - Form 26AS matching and verification  
✅ **Auto-Population** - Link portfolio, FD, insurance data  
✅ **ITR Export** - ITR-1 and ITR-2 JSON generation  
✅ **Advanced Calculations** - House property, business income, loss set-off  
✅ **Form Parsing** - Import Form 16, 26AS  
✅ **AIS Integration** - Sync with Income Tax Portal  

---

**Tax Module Status**: 100% Complete  
**Last Updated**: February 2, 2026  
**Total API Endpoints**: 40+  
**Documentation Coverage**: 100%
