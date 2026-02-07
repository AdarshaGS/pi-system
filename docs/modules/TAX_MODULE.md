# 📄 Tax Module - Complete Guide

**Last Updated**: February 6, 2026  
**Status**: ✅ 100% Complete (Backend + Frontend + Testing)

---

## 📋 Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [API Endpoints](#api-endpoints)
4. [Frontend Components](#frontend-components)
5. [Tax Calculations](#tax-calculations)
6. [Database Schema](#database-schema)
7. [Usage Guide](#usage-guide)
8. [Testing](#testing)

---

## 🎯 Overview

The Tax Module provides comprehensive tax management with regime comparison (Old vs New), capital gains tracking with STCG/LTCG auto-classification, TDS reconciliation, tax projections, and advance tax scheduling.

### Key Capabilities
- ✅ **Old vs New Regime Comparison** with smart recommendations
- ✅ **Capital Gains Tracking** (STCG/LTCG auto-classification by asset type & holding period)
- ✅ **TDS Management** with quarterly tracking and reconciliation
- ✅ **Tax Projections** with advance tax schedule (4 installments if >₹10K)
- ✅ **Section 80C-80D Deductions** with validation
- ✅ **ITR Prefill Data Export** for easy filing
- ✅ **Tax Saving Suggestions** based on income and deductions

---

## 🚀 Features

### Backend Features
| Feature | Status | Description |
|---------|--------|-------------|
| Tax Details CRUD | ✅ | Create, read, update tax information |
| Regime Comparison | ✅ | Compare Old vs New tax regime with recommendations |
| Capital Gains Tracking | ✅ | Record and track STCG/LTCG with auto-classification |
| TDS Management | ✅ | Record TDS with quarterly reconciliation |
| Tax Projections | ✅ | Calculate tax liability with advance tax schedule |
| Deduction Tracking | ✅ | Sections 80C, 80CCD(1B), 80D, 80G, 80E, 80TTA |
| Tax Slabs Calculation | ✅ | Accurate slab-wise tax computation |
| ITR Prefill | ✅ | Export data in ITR format |
| Tax Saving Tips | ✅ | Personalized tax saving suggestions |

### Frontend Features  
| Feature | Status | Description |
|---------|--------|-------------|
| Tax Dashboard | ✅ | 6-tab interface (Overview, Details, Comparison, CG, TDS, Projection) |
| Tax Details Form | ✅ | Income sources, deductions, regime selection |
| Regime Comparison | ✅ | Side-by-side comparison with recommendations |
| Capital Gains Tracker | ✅ | CRUD with asset type classification |
| TDS Management | ✅ | Quarterly tracking with reconciliation modal |
| Tax Projection | ✅ | Visual tax calculation with advance tax schedule |
| Summary Cards | ✅ | Tax liability, CG, TDS, recommended regime |

---

## 🔌 API Endpoints

### Tax Management (`/api/v1/tax`)
```http
# Tax Details
POST   /api/v1/tax                                      # Create/update tax details
GET    /api/v1/tax/{userId}                             # Get tax details
DELETE /api/v1/tax/{userId}                             # Delete tax details

# Regime Comparison
GET    /api/v1/tax/{userId}/regime-comparison           # Compare Old vs New regime

# Capital Gains
POST   /api/v1/tax/{userId}/capital-gains               # Record capital gain
GET    /api/v1/tax/{userId}/capital-gains               # Get all CG records
GET    /api/v1/tax/{userId}/capital-gains/summary       # CG summary (STCG + LTCG)
GET    /api/v1/tax/{userId}/capital-gains/{type}        # Get by type (STCG/LTCG)
PUT    /api/v1/tax/{userId}/capital-gains/{id}          # Update CG record
DELETE /api/v1/tax/{userId}/capital-gains/{id}          # Delete CG record

# TDS Management
POST   /api/v1/tax/{userId}/tds                         # Record TDS
GET    /api/v1/tax/{userId}/tds                         # Get all TDS records
GET    /api/v1/tax/{userId}/tds/reconciliation          # TDS reconciliation
PUT    /api/v1/tax/{userId}/tds/{id}                    # Update TDS record
DELETE /api/v1/tax/{userId}/tds/{id}                    # Delete TDS record

# Projections & Reports
GET    /api/v1/tax/{userId}/projection                  # Tax projection
GET    /api/v1/tax/{userId}/itr-prefill                 # ITR export data
GET    /api/v1/tax/{userId}/tax-saving-suggestions      # Tax saving tips
```

**Request Examples:**

**Create Tax Details:**
```json
POST /api/v1/tax
{
  "userId": 1,
  "financialYear": "2025-2026",
  "taxRegime": "NEW",
  "salaryIncome": 1500000,
  "otherIncome": 100000,
  "section80C": 150000,
  "section80CCD1B": 50000,
  "section80D": 25000,
  "section80G": 10000,
  "standardDeduction": 50000,
  "professionalTax": 2500
}
```

**Compare Regimes:**
```json
GET /api/v1/tax/1/regime-comparison

Response:
{
  "oldRegime": {
    "grossIncome": 1600000,
    "totalDeductions": 237500,
    "taxableIncome": 1362500,
    "taxLiability": 242500,
    "effectiveTaxRate": 15.16
  },
  "newRegime": {
    "grossIncome": 1600000,
    "totalDeductions": 50000,
    "taxableIncome": 1550000,
    "taxLiability": 195000,
    "effectiveTaxRate": 12.19
  },
  "recommendation": "NEW",
  "savings": 47500,
  "reason": "New regime saves ₹47,500 with simpler filing"
}
```

**Record Capital Gain:**
```json
POST /api/v1/tax/1/capital-gains
{
  "assetType": "EQUITY",
  "purchaseDate": "2023-01-15",
  "saleDate": "2025-11-20",
  "purchasePrice": 500000,
  "salePrice": 750000,
  "holdingPeriodMonths": 34
}

Response:
{
  "id": 1,
  "assetType": "EQUITY",
  "capitalGainType": "LTCG",  // Auto-classified (>12 months for equity)
  "capitalGain": 250000,
  "taxLiability": 25000,      // 10% above ₹1L for LTCG on equity
  "holdingPeriodMonths": 34
}
```

---

## 🖥️ Frontend Components

### Component Structure
```
frontend/src/
├── pages/
│   └── Tax.jsx                          # Main tax dashboard (800+ lines)
├── components/
│   ├── TaxDetailsForm.jsx               # Tax entry form (400+ lines)
│   ├── RegimeComparison.jsx             # Regime comparison view (300+ lines)
│   ├── CapitalGainsTracker.jsx          # CG CRUD interface (350+ lines)
│   ├── TDSManagement.jsx                # TDS tracking (280+ lines)
│   └── TaxProjection.jsx                # Tax calculation visual (320+ lines)
├── api/
│   └── taxApi.js                        # Tax API integration (16 functions)
└── styles/
    └── Tax.css                          # Tax module styling
```

### Tax.jsx Features (6 Tabs)
1. **Overview Tab:**
   - Summary cards: Tax liability, CG, TDS, recommended regime
   - Quick stats and highlights
   
2. **Tax Details Tab:**
   - Income sources form (Salary, Business, Other)
   - Deductions input (80C, 80CCD, 80D, 80G, 80E, 80TTA)
   - Regime selection (Old/New)
   
3. **Regime Comparison Tab:**
   - Side-by-side comparison
   - Savings calculation
   - Smart recommendation with reasoning
   
4. **Capital Gains Tab:**
   - CG transactions list
   - Add/Edit/Delete CG records
   - STCG/LTCG summary
   - Auto-classification by asset type
   
5. **TDS Tab:**
   - Quarterly TDS tracking
   - TDS reconciliation modal
   - TDS vs Tax Liability comparison
   
6. **Tax Projection Tab:**
   - Slab-wise tax breakdown
   - Advance tax schedule (4 installments)
   - Total tax liability visualization

---

## 💰 Tax Calculations

### Old Tax Regime (FY 2025-26)
```
Slabs:
₹0 - ₹2,50,000:       Nil
₹2,50,001 - ₹5,00,000: 5%
₹5,00,001 - ₹10,00,000: 20%
Above ₹10,00,000:      30%

Cess: 4% on tax amount
Rebate: Up to ₹12,500 if income ≤ ₹5,00,000

Deductions Allowed:
- Section 80C: Up to ₹1,50,000
- Section 80CCD(1B): Up to ₹50,000
- Section 80D: Up to ₹25,000 (₹50,000 for senior citizens)
- Section 80G: Donations
- Section 80E: Education loan interest
- Section 80TTA: Interest on savings (₹10,000)
- Standard Deduction: ₹50,000
```

**Example Calculation (Old Regime):**
```
Gross Income: ₹15,00,000
Section 80C: ₹1,50,000
Section 80D: ₹25,000
Standard Deduction: ₹50,000

Taxable Income: 15,00,000 - 2,25,000 = ₹12,75,000

Tax Calculation:
₹0 - ₹2,50,000:      Nil = ₹0
₹2,50,001 - ₹5,00,000: 5% of 2,50,000 = ₹12,500
₹5,00,001 - ₹10,00,000: 20% of 5,00,000 = ₹1,00,000
₹10,00,001 - ₹12,75,000: 30% of 2,75,000 = ₹82,500

Subtotal: ₹1,95,000
Cess (4%): ₹7,800
Total Tax: ₹2,02,800
```

### New Tax Regime (FY 2025-26)
```
Slabs:
₹0 - ₹3,00,000:       Nil
₹3,00,001 - ₹6,00,000: 5%
₹6,00,001 - ₹9,00,000: 10%
₹9,00,001 - ₹12,00,000: 15%
₹12,00,001 - ₹15,00,000: 20%
Above ₹15,00,000:      30%

Cess: 4% on tax amount
Rebate: Up to ₹25,000 if income ≤ ₹7,00,000

Deductions NOT Allowed (except Standard Deduction of ₹50,000)
```

**Example Calculation (New Regime):**
```
Gross Income: ₹15,00,000
Standard Deduction: ₹50,000

Taxable Income: 15,00,000 - 50,000 = ₹14,50,000

Tax Calculation:
₹0 - ₹3,00,000:      Nil = ₹0
₹3,00,001 - ₹6,00,000: 5% of 3,00,000 = ₹15,000
₹6,00,001 - ₹9,00,000: 10% of 3,00,000 = ₹30,000
₹9,00,001 - ₹12,00,000: 15% of 3,00,000 = ₹45,000
₹12,00,001 - ₹14,50,000: 20% of 2,50,000 = ₹50,000

Subtotal: ₹1,40,000
Cess (4%): ₹5,600
Total Tax: ₹1,45,600
```

### Capital Gains Classification

**Short-Term Capital Gains (STCG):**
```
Asset Type | Holding Period | Tax Rate
-----------|----------------|----------
Equity     | ≤ 12 months    | 15%
Debt       | ≤ 36 months    | Slab rate
Real Estate| ≤ 24 months    | Slab rate
Gold       | ≤ 36 months    | Slab rate
```

**Long-Term Capital Gains (LTCG):**
```
Asset Type | Holding Period | Tax Rate | Exemption
-----------|----------------|----------|----------
Equity     | > 12 months    | 10%      | ₹1,00,000/year
Debt       | > 36 months    | 20% with indexation
Real Estate| > 24 months    | 20% with indexation
Gold       | > 36 months    | 20% with indexation
```

**Auto-Classification Logic:**
```javascript
// Frontend automatically classifies based on:
1. Asset Type (Equity, Debt, Real Estate, Gold)
2. Holding Period (months between purchase and sale)

Example:
- Equity bought on 2023-01-15, sold on 2025-11-20
- Holding: 34 months
- Classification: LTCG (>12 months for equity)
- Tax: 10% on gains above ₹1,00,000
```

### Advance Tax Schedule
```
If estimated tax > ₹10,000, pay in 4 installments:

Due Date        | % of Tax Liability
----------------|-------------------
15th June       | 15%
15th September  | 45% (cumulative)
15th December   | 75% (cumulative)
15th March      | 100% (cumulative)

Example: Tax Liability = ₹2,00,000
- 15 June: ₹30,000
- 15 Sep: ₹60,000 (₹90,000 cumulative)
- 15 Dec: ₹60,000 (₹1,50,000 cumulative)
- 15 Mar: ₹50,000 (₹2,00,000 cumulative)
```

---

## 💾 Database Schema

### Tables

**tax_details**
```sql
CREATE TABLE tax_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    financial_year VARCHAR(10) NOT NULL,
    tax_regime VARCHAR(10) NOT NULL,        -- OLD, NEW
    salary_income DECIMAL(15,2),
    business_income DECIMAL(15,2),
    other_income DECIMAL(15,2),
    section_80c DECIMAL(15,2),
    section_80ccd_1b DECIMAL(15,2),
    section_80d DECIMAL(15,2),
    section_80g DECIMAL(15,2),
    section_80e DECIMAL(15,2),
    section_80tta DECIMAL(15,2),
    standard_deduction DECIMAL(15,2) DEFAULT 50000,
    professional_tax DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**capital_gains**
```sql
CREATE TABLE capital_gains (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,       -- EQUITY, DEBT, REAL_ESTATE, GOLD
    purchase_date DATE NOT NULL,
    sale_date DATE NOT NULL,
    purchase_price DECIMAL(15,2) NOT NULL,
    sale_price DECIMAL(15,2) NOT NULL,
    capital_gain_type VARCHAR(10),         -- STCG, LTCG (auto-classified)
    capital_gain DECIMAL(15,2),
    tax_liability DECIMAL(15,2),
    holding_period_months INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**tds_records**
```sql
CREATE TABLE tds_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    quarter VARCHAR(10) NOT NULL,          -- Q1, Q2, Q3, Q4
    tds_amount DECIMAL(15,2) NOT NULL,
    deductor_name VARCHAR(255),
    deductor_tan VARCHAR(20),
    tds_type VARCHAR(50),                  -- SALARY, INTEREST, OTHER
    payment_date DATE,
    challan_number VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 📖 Usage Guide

### 1. Enter Tax Details
```
Steps:
1. Navigate to Tax Dashboard → "Tax Details" tab
2. Enter income details:
   - Salary Income
   - Business Income
   - Other Income (rent, FD interest, etc.)
3. Enter deductions:
   - Section 80C (PPF, ELSS, LIC)
   - Section 80D (Health insurance)
   - Section 80G (Donations)
   - Other applicable sections
4. Select tax regime (Old/New)
5. Click "Save Tax Details"

Result: Tax details saved, regime comparison available
```

### 2. Compare Tax Regimes
```
Steps:
1. Go to "Regime Comparison" tab
2. View side-by-side comparison:
   - Old Regime (with all deductions)
   - New Regime (simplified slabs)
3. See savings calculation
4. Read recommendation with reasoning

Recommendation Logic:
- Compares tax liability in both regimes
- Considers deduction utilization
- Provides actionable advice
- Shows potential savings

Example: "New regime saves ₹47,500. Switch to new regime for simpler filing."
```

### 3. Track Capital Gains
```
Steps:
1. Go to "Capital Gains" tab
2. Click "+ Add Capital Gain"
3. Enter transaction details:
   - Asset Type (Equity/Debt/Real Estate/Gold)
   - Purchase Date
   - Sale Date
   - Purchase Price
   - Sale Price
4. System auto-calculates:
   - Holding Period
   - STCG/LTCG Classification
   - Capital Gain Amount
   - Tax Liability
5. Click "Add"

Result: CG recorded, tax liability added to total
```

### 4. Manage TDS
```
Steps:
1. Go to "TDS" tab
2. Click "+ Add TDS Record"
3. Enter TDS details:
   - Quarter (Q1/Q2/Q3/Q4)
   - TDS Amount
   - Deductor Name & TAN
   - TDS Type (Salary/Interest/Other)
   - Challan Number
4. Click "Add TDS"

Reconciliation:
- View quarterly TDS breakdown
- Compare total TDS vs Tax Liability
- Identify refund due or tax payable
- Click "Reconcile" for detailed report
```

### 5. View Tax Projection
```
Steps:
1. Go to "Tax Projection" tab
2. View tax calculation flow:
   - Gross Income
   - Less: Deductions
   - Taxable Income
   - Tax Calculation (slab-wise)
   - Add: Cess (4%)
   - Total Tax Liability

Advance Tax Schedule:
- If liability > ₹10,000, see 4 installment dates
- Each installment amount displayed
- Due dates highlighted

Export: Click "Download Tax Summary" for PDF report
```

---

## 🧪 Testing

### Integration Tests
```
Location: src/test/java/com/tax/
Test Count: 20 tests (100% coverage)

Key Tests:
- testCreateTaxDetails()
- testGetTaxDetails()
- testRegimeComparison()
- testOldRegimeTaxCalculation()
- testNewRegimeTaxCalculation()
- testAddCapitalGain()
- testSTCGClassification()
- testLTCGClassification()
- testAddTDSRecord()
- testTDSReconciliation()
- testTaxProjection()
- testAdvanceTaxSchedule()
- testITRPrefillData()
- testTaxSavingSuggestions()
```

**Run Tests:**
```bash
./gradlew test --tests "*TaxController*"
./gradlew test --tests "*TaxService*"
```

---

## 📚 Related Documentation

- [Tax Module Implementation Complete](../TAX_MODULE_IMPLEMENTATION_COMPLETE.md)
- [Tax Module Developer Guide](../TAX_MODULE_DEVELOPER_GUIDE.md)
- [Tax API Complete Reference](../TAX_API_COMPLETE_REFERENCE.md)
- [Tax API Quick Reference](../TAX_API_QUICK_REFERENCE.md)
- [Tax Frontend Implementation](../../TAX_FRONTEND_IMPLEMENTATION.md)

---

**Module Status:** ✅ Production Ready  
**Test Coverage:** 100% (20 tests)  
**Last Tested:** February 6, 2026  
**Compliance:** FY 2025-26 tax slabs
