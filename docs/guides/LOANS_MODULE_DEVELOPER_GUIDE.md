# Loans Module - Developer Guide

## 📦 Module Structure

```
com.loan/
├── controller/
│   └── LoanController.java          # REST API endpoints
├── service/
│   ├── LoanService.java             # Service interface
│   └── LoanServiceImpl.java         # Service implementation
├── repo/
│   ├── LoanRepository.java          # Loan entity repository
│   └── LoanPaymentRepository.java   # Payment entity repository
├── data/
│   ├── Loan.java                    # Loan entity
│   ├── LoanPayment.java             # Payment entity
│   ├── LoanType.java                # Loan type enum
│   ├── PaymentType.java             # Payment type enum
│   └── PaymentStatus.java           # Payment status enum
└── dto/
    ├── AmortizationScheduleResponse.java
    ├── RecordPaymentRequest.java
    ├── ForeclosureCalculationResponse.java
    ├── PaymentHistoryResponse.java
    └── LoanAnalysisResponse.java
```

---

## 🗄️ Database Schema

### Table: `loans`
```sql
CREATE TABLE loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50),
    user_id BIGINT NOT NULL,
    loan_type VARCHAR(50) NOT NULL,
    provider VARCHAR(255),
    loan_account_number VARCHAR(100),
    principal_amount DECIMAL(15, 2),
    outstanding_amount DECIMAL(15, 2),
    interest_rate DECIMAL(5, 2),
    tenure_months INT,
    start_date DATE,
    end_date DATE,
    emi_amount DECIMAL(15, 2),
    is_auto_fetched BOOLEAN DEFAULT FALSE
);
```

### Table: `loan_payments`
```sql
CREATE TABLE loan_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    loan_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    payment_amount DECIMAL(15, 2) NOT NULL,
    principal_paid DECIMAL(15, 2) NOT NULL,
    interest_paid DECIMAL(15, 2) NOT NULL,
    outstanding_balance DECIMAL(15, 2) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(100),
    transaction_reference VARCHAR(255),
    notes TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loans(id) ON DELETE CASCADE
);
```

---

## 🧮 Core Calculations

### 1. EMI Calculation
**Formula**: EMI = [P × r × (1+r)^n] / [(1+r)^n - 1]

Where:
- P = Principal amount
- r = Monthly interest rate (annual rate / 1200)
- n = Tenure in months

**Implementation**:
```java
public BigDecimal calculateEMI(BigDecimal principal, BigDecimal rate, Integer tenureMonths) {
    BigDecimal monthlyRate = rate.divide(TWELVE_HUNDRED, MC);
    BigDecimal onePlusRToN = monthlyRate.add(BigDecimal.ONE).pow(tenureMonths, MC);
    BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
    BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
    return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
}
```

### 2. Interest Component Calculation
**Formula**: Interest for month = Outstanding Balance × Monthly Rate

**Implementation**:
```java
BigDecimal monthlyRate = loan.getInterestRate().divide(TWELVE_HUNDRED, MC);
BigDecimal interestForMonth = outstandingBalance.multiply(monthlyRate)
    .setScale(2, RoundingMode.HALF_UP);
```

### 3. Principal Component
**Formula**: Principal for month = EMI - Interest for month

```java
BigDecimal principalForMonth = emi.subtract(interestForMonth)
    .setScale(2, RoundingMode.HALF_UP);
```

### 4. Remaining Tenure Calculation
**Formula**: n = log(EMI / (EMI - P×r)) / log(1+r)

Where:
- EMI = Current EMI amount
- P = Outstanding principal
- r = Monthly interest rate

### 5. Total Interest
**Formula**: Total Interest = (EMI × Tenure) - Principal

---

## 🔄 Business Logic Flows

### Flow 1: Loan Creation
```
1. Receive loan details
2. Validate user access
3. Calculate EMI (if not provided)
4. Set outstanding = principal
5. Calculate end date = start date + tenure
6. Save to database
7. Return loan entity
```

### Flow 2: EMI Payment Recording
```
1. Receive payment request
2. Fetch loan and validate user
3. Calculate interest component (Outstanding × Monthly Rate)
4. Calculate principal component (Payment - Interest)
5. Update outstanding balance (Outstanding - Principal)
6. Create payment record
7. Save loan and payment
8. Return payment entity
```

### Flow 3: Prepayment
```
1. Receive prepayment request
2. Fetch loan and validate
3. Entire payment amount goes to principal (no interest)
4. Update outstanding (Outstanding - Payment)
5. Create payment record with PREPAYMENT type
6. Save loan and payment
7. Return payment entity
```

### Flow 4: Foreclosure
```
1. Calculate outstanding principal
2. Calculate accrued interest for current month
3. Calculate foreclosure charges (% of principal)
4. Sum = Principal + Interest + Charges
5. If confirmed:
   a. Create FORECLOSURE payment
   b. Set outstanding to zero
   c. Save loan and payment
6. Return foreclosure payment
```

### Flow 5: Amortization Schedule Generation
```
1. Fetch loan details
2. Initialize balance = principal
3. For each month (1 to tenure):
   a. Calculate interest = balance × monthly rate
   b. Calculate principal = EMI - interest
   c. Update balance = balance - principal
   d. Add entry to schedule
4. Return complete schedule
```

---

## 🛡️ Security Considerations

### Access Control
```java
// Validate user can access loan
authenticationHelper.validateUserAccess(loan.getUserId());

// Admin-only endpoints
authenticationHelper.validateAdminAccess();
```

### Data Validation
- All payment amounts must be positive
- Payment date cannot be in future (configurable)
- Loan tenure must be > 0
- Interest rate must be > 0
- Principal must be > 0

### Transaction Management
- All write operations wrapped in `@Transactional`
- Rollback on exception
- Consistent state maintained

---

## 🧪 Testing Guidelines

### Unit Test Examples

#### Test EMI Calculation
```java
@Test
void testEMICalculation() {
    BigDecimal principal = new BigDecimal("5000000");
    BigDecimal rate = new BigDecimal("8.5");
    Integer tenure = 240;
    
    BigDecimal emi = loanService.calculateEMI(principal, rate, tenure);
    
    // Expected EMI for 50L @ 8.5% for 20 years ≈ 43,366
    assertThat(emi).isBetween(
        new BigDecimal("43000"), 
        new BigDecimal("44000")
    );
}
```

#### Test Payment Recording
```java
@Test
void testRecordPayment_UpdatesOutstanding() {
    // Given
    Loan loan = createTestLoan();
    RecordPaymentRequest request = new RecordPaymentRequest();
    request.setLoanId(loan.getId());
    request.setPaymentAmount(loan.getEmiAmount());
    request.setPaymentType(PaymentType.EMI);
    
    BigDecimal initialOutstanding = loan.getOutstandingAmount();
    
    // When
    LoanPayment payment = loanService.recordPayment(request);
    
    // Then
    Loan updatedLoan = loanService.getLoanById(loan.getId());
    assertThat(updatedLoan.getOutstandingAmount())
        .isLessThan(initialOutstanding);
    assertThat(payment.getPrincipalPaid()
        .add(payment.getInterestPaid()))
        .isEqualTo(loan.getEmiAmount());
}
```

### Integration Test Checklist
- [ ] Create loan with all fields
- [ ] Create loan with minimal fields
- [ ] Record EMI payment
- [ ] Record prepayment
- [ ] Process foreclosure
- [ ] Generate amortization schedule
- [ ] Get payment history
- [ ] Get missed payments
- [ ] Simulate prepayment
- [ ] Access control validation
- [ ] Concurrent payment recording

---

## 🚀 Performance Optimization

### Database Indexes
```sql
-- Already added in migration
CREATE INDEX idx_loan_id ON loan_payments(loan_id);
CREATE INDEX idx_payment_date ON loan_payments(payment_date);
CREATE INDEX idx_payment_status ON loan_payments(payment_status);
```

### Caching Considerations
- Amortization schedules are expensive to compute
- Consider caching with loan_id + modification_timestamp as key
- Invalidate cache on loan update or payment

### Query Optimization
- Use `findByLoanIdOrderByPaymentDateDesc` for latest payments
- Paginate payment history for loans with many payments
- Use count queries for statistics

---

## 🐛 Common Issues & Solutions

### Issue 1: EMI Calculation Returns Zero
**Cause**: Division by zero when tenure = 0  
**Solution**: Add validation in calculateEMI()

### Issue 2: Outstanding Balance Goes Negative
**Cause**: Last payment calculation error  
**Solution**: Check and set to zero if negative

```java
if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
    newOutstanding = BigDecimal.ZERO;
}
```

### Issue 3: Amortization Schedule Doesn't Balance
**Cause**: Rounding errors accumulate  
**Solution**: Adjust last payment to exactly zero out balance

```java
if (i == tenureMonths || principalForMonth.compareTo(balance) > 0) {
    principalForMonth = balance;
    interestForMonth = emi.subtract(principalForMonth);
}
```

### Issue 4: Payment History Query Slow
**Cause**: Missing index on loan_id  
**Solution**: Already added in migration (idx_loan_id)

---

## 📚 Further Reading

### Financial Concepts
- [EMI Calculation Formula](https://en.wikipedia.org/wiki/Equated_monthly_installment)
- [Amortization Schedule](https://en.wikipedia.org/wiki/Amortization_schedule)
- [Prepayment Penalties](https://en.wikipedia.org/wiki/Prepayment_of_loan)

### Code References
- `BigDecimal` best practices for financial calculations
- Spring Data JPA query methods
- Transaction management in Spring

### Related Modules
- **User Module**: For authentication and authorization
- **Budget Module**: Loan payments affect monthly budget
- **Tax Module**: Interest deduction for certain loan types

---

## 🔧 Future Enhancements

### Planned
1. **Scheduled Jobs**
   - Auto-detect missed payments
   - Send payment reminders
   - Generate monthly statements

2. **Analytics**
   - Payment trends
   - Prepayment impact analysis
   - Loan comparison tool

3. **Integration**
   - Bank account linking
   - Auto-debit setup
   - SMS/Email notifications

### Under Consideration
- Support for reducing EMI vs reducing tenure on prepayment
- Variable interest rate tracking over time
- EMI holiday/moratorium support
- Joint loan holders
- Guarantor management

---

## 📞 Support

For questions or issues:
1. Check this guide first
2. Review code comments in `LoanServiceImpl.java`
3. Check [LOANS_API_QUICK_REFERENCE.md](./LOANS_API_QUICK_REFERENCE.md)
4. Contact: dev-team@example.com

---

**Last Updated**: February 1, 2026  
**Module Version**: 2.0  
**Status**: Production Ready ✅
