# 🚀 Tax Module - Developer Quick Start

> **Quick reference for developers working with the tax module**

---

## 📦 Module Structure

```
com.tax/
├── controller/
│   ├── TaxController.java                    # Main tax CRUD + TDS + projections
│   ├── TaxAutoPopulationController.java      # Auto-populate from other modules
│   ├── TaxCalculationController.java         # Advanced calculations
│   └── ITRController.java                    # ITR generation & export
├── service/
│   ├── TaxService.java                       # Main tax service interface
│   ├── TaxServiceImpl.java                   # Main implementation
│   ├── TaxAutoPopulationService.java         # Auto-population interface
│   ├── TaxAutoPopulationServiceImpl.java     # Auto-population implementation
│   ├── TaxCalculationService.java            # Calculations interface
│   ├── TaxCalculationServiceImpl.java        # Calculations implementation
│   ├── ITRService.java                       # ITR interface
│   └── ITRServiceImpl.java                   # ITR implementation
├── repo/
│   ├── TaxRepository.java                    # Tax details repo
│   ├── CapitalGainsRepository.java           # Capital gains repo
│   ├── TaxSavingRepository.java              # Tax savings repo
│   └── TDSRepository.java                    # TDS entries repo
├── data/
│   ├── Tax.java                              # Main tax entity
│   ├── CapitalGainsTransaction.java          # Capital gains entity
│   ├── TaxSavingInvestment.java              # Tax savings entity
│   └── TDSEntry.java                         # TDS entry entity
└── dto/
    ├── TaxDTO.java
    ├── CapitalGainsSummaryDTO.java
    ├── TaxRegimeComparisonDTO.java
    ├── TDSReconciliationDTO.java
    ├── HousePropertyIncomeDTO.java
    ├── BusinessIncomeDTO.java
    ├── LossSetOffDTO.java
    ├── TaxComputationDTO.java
    ├── ITR1DTO.java
    └── ITR2DTO.java
```

---

## 🎯 Common Use Cases

### 1. Calculate Capital Gains

```java
// Auto-populate from portfolio
POST /api/v1/tax/auto-populate/{userId}/capital-gains?financialYear=2025-26

// Manual entry
POST /api/v1/tax/{userId}/capital-gains
{
  "assetType": "STOCK",
  "assetName": "Reliance",
  "quantity": 10,
  "purchaseDate": "2023-01-15",
  "purchasePrice": 2400,
  "saleDate": "2025-06-20",
  "salePrice": 2800,
  "financialYear": "2025-26"
}

// Get summary
GET /api/v1/tax/{userId}/capital-gains/summary?financialYear=2025-26
```

### 2. Track Tax Saving Investments

```java
// Auto-populate 80C
POST /api/v1/tax/auto-populate/{userId}/80c-investments?financialYear=2025-26

// Manual entry
POST /api/v1/tax/{userId}/tax-savings
{
  "investmentType": "80C",
  "category": "ELSS",
  "amount": 50000,
  "investmentDate": "2025-12-15",
  "financialYear": "2025-26"
}

// Get recommendations
GET /api/v1/tax/{userId}/recommendations?financialYear=2025-26
```

### 3. Manage TDS

```java
// Record TDS
POST /api/v1/tax/{userId}/tds
{
  "financialYear": "2025-26",
  "quarter": 4,
  "deductorName": "ABC Corp",
  "deductorTan": "MUMA01234D",
  "section": "192",
  "amountPaid": 300000,
  "tdsDeducted": 30000
}

// Get reconciliation
GET /api/v1/tax/{userId}/tds/reconciliation?financialYear=2025-26
```

### 4. Compare Tax Regimes

```java
GET /api/v1/tax/{userId}/regime-comparison?financialYear=2025-26&grossIncome=1200000
```

### 5. Export ITR

```java
// ITR-1 (Sahaj)
GET /api/v1/tax/itr/{userId}/itr1/json?financialYear=2025-26

// ITR-2
GET /api/v1/tax/itr/{userId}/itr2/json?financialYear=2025-26
```

### 6. Advanced Calculations

```java
// House property income
POST /api/v1/tax/calculations/house-property
{
  "propertyType": "LET_OUT",
  "annualRent": 240000,
  "municipalTaxes": 12000,
  "interestOnHomeLoan": 180000
}

// Business income
POST /api/v1/tax/calculations/business-income
{
  "taxationScheme": "NORMAL",
  "grossReceipts": 5000000,
  ...expenses
}

// Loss set-off
POST /api/v1/tax/calculations/loss-setoff
{
  "housePropertyLoss": 250000,
  "businessLoss": 100000,
  ...
}
```

---

## 🔑 Key Entities

### Tax (Main)
```java
Long id, userId, String financialYear
BigDecimal grossSalary, businessIncome, capitalGainsShortTerm, capitalGainsLongTerm
BigDecimal section80CDeductions, section80DDeductions
BigDecimal taxPayable, taxPaid
TaxRegime selectedRegime (OLD/NEW)
```

### CapitalGainsTransaction
```java
Long id, userId, String assetType, assetName
LocalDate purchaseDate, saleDate
BigDecimal quantity, purchasePrice, salePrice
Integer holdingPeriodDays
String gainType (STCG/LTCG)
BigDecimal capitalGain, taxAmount
String financialYear
```

### TaxSavingInvestment
```java
Long id, userId
String investmentType (80C, 80D, 80E, 80G)
String category (PPF, ELSS, LIC, etc.)
BigDecimal amount
LocalDate investmentDate
String financialYear
// Linking
String linkedEntityType
Long linkedEntityId
```

### TDSEntry
```java
Long id, userId, String financialYear
Integer quarter
String deductorName, deductorTan
String section (192, 194A, etc.)
BigDecimal amountPaid, tdsDeducted
String reconciliationStatus (PENDING, MATCHED, MISMATCHED)
```

---

## 🛠️ Service Methods

### TaxService
```java
createTaxDetails(Tax) → TaxDTO
getTaxDetailsByUserId(userId, FY) → TaxDTO
getOutstandingTaxLiability(userId) → BigDecimal
compareTaxRegimes(userId, FY, income) → ComparisonDTO
recordCapitalGain(transaction) → Transaction
getCapitalGainsSummary(userId, FY) → SummaryDTO
recordTDSEntry(entry) → TDSEntry
getTDSReconciliation(userId, FY) → ReconciliationDTO
getTaxProjection(userId, FY) → ProjectionDTO
getITRPreFillData(userId, FY) → PreFillDTO
```

### TaxAutoPopulationService
```java
autoPopulateCapitalGains(userId, FY) → List<Transaction>
autoPopulateSalaryIncome(userId, FY) → void
autoPopulateInterestIncome(userId, FY) → void
autoPopulate80CInvestments(userId, FY) → List<Investment>
autoPopulate80DInvestments(userId, FY) → List<Investment>
autoPopulateHomeLoanInterest(userId, FY) → void
```

### TaxCalculationService
```java
calculateHousePropertyIncome(input) → HousePropertyDTO
calculateBusinessIncome(input) → BusinessIncomeDTO
processLossSetOff(input) → LossSetOffDTO
calculateCompleteTax(input) → TaxComputationDTO
calculateRebate87A(income, regime) → BigDecimal
calculateSurcharge(tax, income) → BigDecimal
```

### ITRService
```java
generateITR1JSON(userId, FY) → String
generateITR2JSON(userId, FY) → String
buildITR1Data(userId, FY) → ITR1DTO
buildITR2Data(userId, FY) → ITR2DTO
parseAndImportForm16(userId, FY, data) → void
parseAndImportForm26AS(userId, FY, data) → void
syncWithAIS(userId, FY) → void
```

---

## 📊 Database Queries

### Capital Gains Queries
```java
// Custom repository methods
findByUserIdAndFinancialYear(userId, FY)
findByUserIdAndFinancialYearAndGainType(userId, FY, type)
getTotalCapitalGainByType(userId, FY, type)
findUnsetOffLosses(userId)
```

### Tax Savings Queries
```java
findByUserIdAndFinancialYear(userId, FY)
findByUserIdAndFinancialYearAndInvestmentType(userId, FY, type)
getTotalInvestmentByType(userId, FY, type)
findAutoPopulatedInvestments(userId, FY)
```

### TDS Queries
```java
findByUserIdAndFinancialYear(userId, FY)
findByUserIdAndFinancialYearAndQuarter(userId, FY, quarter)
getTotalTDSDeducted(userId, FY)
countMismatchedEntries(userId, FY)
```

---

## 🎯 Testing Checklist

### Unit Tests
- [ ] TaxServiceImpl - all methods
- [ ] TaxAutoPopulationServiceImpl - all auto-populate methods
- [ ] TaxCalculationServiceImpl - all calculation methods
- [ ] ITRServiceImpl - ITR generation

### Integration Tests
- [ ] TaxController - all endpoints
- [ ] TaxAutoPopulationController - all endpoints
- [ ] TaxCalculationController - all endpoints
- [ ] ITRController - all endpoints

### End-to-End Tests
- [ ] Complete tax filing workflow
- [ ] ITR export and validation
- [ ] Auto-population from portfolio
- [ ] TDS reconciliation flow

---

## 🚨 Important Notes

### Capital Gains Holding Period
- **Equity**: LTCG if > 12 months
- **Debt**: LTCG if > 36 months
- **Property**: LTCG if > 24 months

### Tax Rates
- **STCG Equity**: 15%
- **LTCG Equity**: 10% (after ₹1L exemption)
- **STCG Debt**: At slab rates (30%)
- **LTCG Debt**: 20% with indexation

### Section Limits
- **80C**: Max ₹1.5L
- **80D**: ₹25K (self), ₹50K (senior citizen)
- **House Property Loss**: Max ₹2L set-off per year

### Surcharge Slabs
- **₹50L - ₹1Cr**: 10%
- **₹1Cr - ₹2Cr**: 15%
- **₹2Cr - ₹5Cr**: 25%
- **> ₹5Cr**: 37%

### Rebate 87A
- **Old Regime**: ₹12,500 if income ≤ ₹5L
- **New Regime**: ₹12,500 if income ≤ ₹7L

---

## 📚 Documentation Links

- [Complete API Reference](TAX_API_COMPLETE_REFERENCE.md)
- [Implementation Summary](TAX_MODULE_IMPLEMENTATION_COMPLETE.md)
- [Quick Reference](TAX_API_QUICK_REFERENCE.md)
- [Progress Tracker](PROGRESS.md)

---

**Last Updated**: February 2, 2026  
**Module Status**: 100% Complete  
**Ready for Frontend Development**: ✅ Yes
