# Loans API Quick Reference

## Base URL: `/api/v1/loans`

---

## 🔵 Basic CRUD Operations

### Create Loan
```http
POST /api/v1/loans/create
Content-Type: application/json

{
  "userId": 1,
  "loanType": "HOME",
  "provider": "HDFC Bank",
  "loanAccountNumber": "LOAN123456",
  "principalAmount": 5000000,
  "interestRate": 8.5,
  "tenureMonths": 240,
  "startDate": "2024-01-01"
}
```

### Get All Loans (Admin)
```http
GET /api/v1/loans/all
```

### Get User's Loans
```http
GET /api/v1/loans/user/{userId}
```

### Get Loan by ID
```http
GET /api/v1/loans/{id}
```

### Delete Loan
```http
DELETE /api/v1/loans/{id}
```

---

## 📊 Advanced Calculations

### 1. Amortization Schedule
```http
GET /api/v1/loans/{id}/amortization-schedule
```

**Response:**
```json
{
  "loanId": 1,
  "totalPrincipal": 5000000.00,
  "totalInterest": 4500000.00,
  "totalPayable": 9500000.00,
  "tenureMonths": 240,
  "schedule": [
    {
      "paymentNumber": 1,
      "paymentDate": "2024-02-01",
      "emiAmount": 43366.00,
      "principalComponent": 8033.33,
      "interestComponent": 35332.67,
      "outstandingBalance": 4991966.67
    },
    ...
  ]
}
```

### 2. Loan Analysis
```http
GET /api/v1/loans/{id}/analysis
```

**Response:**
```json
{
  "loanId": 1,
  "totalInterestPayable": 4500000.00,
  "totalAmountPayable": 9500000.00,
  "interestToPrincipalRatio": 90.00,
  "effectiveInterestRate": 8.50,
  "remainingTenureMonths": 230,
  "remainingInterest": 4350000.00,
  "paymentsCompleted": 10,
  "totalPayments": 240,
  "completionPercentage": 4.17
}
```

### 3. Total Interest
```http
GET /api/v1/loans/{id}/total-interest
```

**Response:**
```json
4500000.00
```

### 4. Prepayment Simulation
```http
POST /api/v1/loans/{id}/simulate-prepayment?amount=500000
```

**Response:**
```json
{
  "originalTenureMonths": 240,
  "remainingTenureMonths": 230,
  "newTenureMonths": 195,
  "savedInterest": 850000.00
}
```

---

## 💰 Payment Tracking

### 1. Record Payment
```http
POST /api/v1/loans/payments
Content-Type: application/json

{
  "loanId": 1,
  "paymentDate": "2024-02-01",
  "paymentAmount": 43366.00,
  "paymentType": "EMI",
  "paymentMethod": "NEFT",
  "transactionReference": "TXN123456789",
  "notes": "January EMI payment"
}
```

**Response:**
```json
{
  "id": 1,
  "loanId": 1,
  "paymentDate": "2024-02-01",
  "paymentAmount": 43366.00,
  "principalPaid": 8033.33,
  "interestPaid": 35332.67,
  "outstandingBalance": 4991966.67,
  "paymentType": "EMI",
  "paymentStatus": "PAID",
  "paymentMethod": "NEFT",
  "transactionReference": "TXN123456789",
  "notes": "January EMI payment",
  "createdAt": "2024-02-01T10:30:00"
}
```

### 2. Record Prepayment
```http
POST /api/v1/loans/payments
Content-Type: application/json

{
  "loanId": 1,
  "paymentDate": "2024-02-15",
  "paymentAmount": 500000.00,
  "paymentType": "PREPAYMENT",
  "paymentMethod": "UPI",
  "notes": "Bonus prepayment"
}
```

### 3. Get Payment History
```http
GET /api/v1/loans/{id}/payments
```

**Response:**
```json
{
  "loanId": 1,
  "totalPayments": 15,
  "missedPayments": 1,
  "totalPaid": 1150490.00,
  "totalPrincipalPaid": 620490.00,
  "totalInterestPaid": 530000.00,
  "outstandingBalance": 4379510.00,
  "payments": [
    {
      "paymentId": 15,
      "paymentDate": "2024-04-01",
      "amount": 43366.00,
      "principalPaid": 8500.00,
      "interestPaid": 34866.00,
      "paymentType": "EMI",
      "paymentStatus": "PAID",
      "paymentMethod": "Auto-debit"
    },
    ...
  ]
}
```

### 4. Get Missed Payments
```http
GET /api/v1/loans/{id}/missed-payments
```

**Response:**
```json
[
  {
    "id": 7,
    "loanId": 1,
    "paymentDate": "2024-03-01",
    "paymentAmount": 43366.00,
    "paymentType": "EMI",
    "paymentStatus": "MISSED"
  }
]
```

---

## 🏁 Foreclosure

### 1. Calculate Foreclosure
```http
GET /api/v1/loans/{id}/foreclosure-calculation?foreclosureChargesPercentage=2
```

**Response:**
```json
{
  "loanId": 1,
  "outstandingPrincipal": 4379510.00,
  "outstandingInterest": 30965.19,
  "foreclosureCharges": 87590.20,
  "foreclosureChargesPercentage": 2.00,
  "totalForeclosureAmount": 4498065.39,
  "message": "Loan can be foreclosed by paying the total foreclosure amount"
}
```

### 2. Process Foreclosure
```http
POST /api/v1/loans/{id}/foreclose?foreclosureChargesPercentage=2
```

**Response:**
```json
{
  "id": 25,
  "loanId": 1,
  "paymentDate": "2024-05-15",
  "paymentAmount": 4498065.39,
  "principalPaid": 4379510.00,
  "interestPaid": 118555.39,
  "outstandingBalance": 0.00,
  "paymentType": "FORECLOSURE",
  "paymentStatus": "PAID",
  "notes": "Loan foreclosure with 2.0% charges",
  "createdAt": "2024-05-15T14:30:00"
}
```

---

## 📋 Enums Reference

### Payment Type
- `EMI` - Regular monthly EMI payment
- `PREPAYMENT` - Partial prepayment to reduce principal
- `FORECLOSURE` - Full loan closure payment
- `MISSED` - Missed/overdue payment record

### Payment Status
- `PAID` - Payment completed successfully
- `PENDING` - Payment due but not yet paid
- `MISSED` - Payment missed/overdue
- `SCHEDULED` - Future scheduled payment

### Loan Type
- `PERSONAL`
- `HOME`
- `AUTO`
- `EDUCATION`
- `BUSINESS`
- `OTHER`

---

## 💡 Common Use Cases

### Use Case 1: New Loan Onboarding
1. Create loan → `POST /api/v1/loans/create`
2. View amortization → `GET /api/v1/loans/{id}/amortization-schedule`
3. Analyze loan → `GET /api/v1/loans/{id}/analysis`

### Use Case 2: Monthly EMI Payment
1. Record payment → `POST /api/v1/loans/payments` with `paymentType: "EMI"`
2. View updated history → `GET /api/v1/loans/{id}/payments`
3. Check outstanding → `GET /api/v1/loans/{id}`

### Use Case 3: Prepayment Decision
1. Simulate prepayment → `POST /api/v1/loans/{id}/simulate-prepayment?amount=500000`
2. If beneficial, record prepayment → `POST /api/v1/loans/payments` with `paymentType: "PREPAYMENT"`
3. View updated schedule → `GET /api/v1/loans/{id}/analysis`

### Use Case 4: Loan Foreclosure
1. Calculate foreclosure amount → `GET /api/v1/loans/{id}/foreclosure-calculation?foreclosureChargesPercentage=2`
2. Review total amount
3. Process foreclosure → `POST /api/v1/loans/{id}/foreclose?foreclosureChargesPercentage=2`
4. Verify closure → `GET /api/v1/loans/{id}` (outstandingAmount = 0)

---

## 🔐 Authentication

All endpoints require authentication. Include JWT token in header:
```http
Authorization: Bearer <your-jwt-token>
```

Users can only access their own loans. Admin endpoints require admin role.

---

## ⚠️ Important Notes

1. **EMI Calculation**: Automatically calculated on loan creation if not provided
2. **Outstanding Balance**: Auto-updated on every payment
3. **Prepayment**: Entire amount goes to principal reduction (no interest)
4. **Foreclosure Charges**: Typically 2-4% of outstanding principal (bank-dependent)
5. **Payment History**: Maintains complete audit trail with timestamps
6. **Date Format**: All dates in `yyyy-MM-dd` format

---

## 🧪 Testing with cURL

### Create Loan
```bash
curl -X POST http://localhost:8080/api/v1/loans/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": 1,
    "loanType": "HOME",
    "provider": "HDFC Bank",
    "principalAmount": 5000000,
    "interestRate": 8.5,
    "tenureMonths": 240,
    "startDate": "2024-01-01"
  }'
```

### Record Payment
```bash
curl -X POST http://localhost:8080/api/v1/loans/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "loanId": 1,
    "paymentDate": "2024-02-01",
    "paymentAmount": 43366.00,
    "paymentType": "EMI",
    "paymentMethod": "NEFT"
  }'
```

### Get Amortization Schedule
```bash
curl -X GET http://localhost:8080/api/v1/loans/1/amortization-schedule \
  -H "Authorization: Bearer <token>"
```

---

**Version**: 1.0  
**Last Updated**: February 1, 2026
