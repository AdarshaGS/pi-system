# SMS Transaction Parser - Quick Start Guide

## Quick Test Commands

### 1. Import Multiple SMS Messages
```bash
curl -X POST http://localhost:8080/api/v1/sms/import \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "messages": [
      {
        "content": "Rs.500.00 debited from A/c XX1234 on 12-Mar-2026 at 14:30 at AMAZON INDIA. Avl Bal: Rs.45,500.00",
        "sender": "HDFCBK"
      },
      {
        "content": "INR 2500 credited to your account XXXX5678 on 11/03/2026. Ref: UPI/409876543210",
        "sender": "ICICIBK"
      },
      {
        "content": "You have paid Rs.1250 to SWIGGY using card XX9876 on 12-03-26 15:45",
        "sender": "PAYTM"
      }
    ]
  }'
```

### 2. Parse Single Message
```bash
curl -X POST http://localhost:8080/api/v1/sms/parse \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "message": "Rs.750 debited from A/c XX3456 on 12-03-2026 to UBER",
    "sender": "SBIINB"
  }'
```

### 3. Get All Transactions
```bash
curl http://localhost:8080/api/v1/sms/user/1
```

### 4. Get Unprocessed Transactions
```bash
curl http://localhost:8080/api/v1/sms/user/1/unprocessed
```

## Database Setup

Run this SQL to create the table:

```sql
CREATE TABLE IF NOT EXISTS sms_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_message TEXT NOT NULL,
    sender VARCHAR(50),
    amount DECIMAL(15,2),
    transaction_date DATE,
    transaction_time TIME,
    transaction_type VARCHAR(20),
    merchant VARCHAR(255),
    account_number VARCHAR(50),
    card_number VARCHAR(50),
    balance DECIMAL(15,2),
    reference_number VARCHAR(100),
    upi_id VARCHAR(100),
    parse_status VARCHAR(20) NOT NULL,
    parse_confidence DOUBLE,
    error_message VARCHAR(500),
    is_processed BOOLEAN DEFAULT FALSE,
    linked_expense_id BIGINT,
    linked_income_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_parse_status (parse_status),
    INDEX idx_transaction_date (transaction_date)
);
```

## Supported Bank SMS Examples

### HDFC Bank
```
Rs.500 debited from A/c XX1234 on 12-Mar-26 at AMAZON. Avl Bal: Rs.10,000
```

### ICICI Bank
```
INR 2500 credited to your account XXXX5678 on 11/03/2026. Ref: UPI/409876543210
```

### SBI
```
Your A/C XX9876 is debited with Rs.750.00 on 12-03-26 for SWIGGY transaction
```

### Axis Bank
```
Dear Customer, Rs 1250.50 has been debited from card XX4567 at UBER on 12/03/2026
```

### UPI/Paytm
```
Rs 250 debited from A/c XX1111 to merchant@paytm UPI ID: 123456789012
```

## What Gets Extracted

✅ **Amount** - Transaction amount (Rs./INR/₹)
✅ **Transaction Type** - DEBIT or CREDIT
✅ **Date** - Transaction date (multiple formats)
✅ **Time** - Transaction time (if available)
✅ **Merchant** - Where transaction occurred
✅ **Account Number** - Last 4 digits
✅ **Card Number** - Last 4 digits (if card transaction)
✅ **Balance** - Available balance
✅ **UPI ID** - UPI reference number
✅ **Reference Number** - Transaction reference

## Response Format

```json
{
  "totalMessages": 3,
  "successfullyParsed": 2,
  "partiallyParsed": 1,
  "failed": 0,
  "duplicates": 0,
  "transactions": [
    {
      "transactionId": 101,
      "message": "Rs.500.00 debited from A/c XX1234...",
      "status": "SUCCESS",
      "confidence": 0.95
    }
  ],
  "errors": []
}
```

## Parse Status

- **SUCCESS** - Amount AND transaction type extracted
- **PARTIAL** - Either amount OR transaction type extracted  
- **FAILED** - Neither amount nor transaction type extracted

## Confidence Score

Score ranges from 0.0 to 1.0:
- **0.8 - 1.0** - High confidence (all major fields extracted)
- **0.5 - 0.8** - Medium confidence (some fields missing)
- **0.0 - 0.5** - Low confidence (limited data extracted)

## Next Steps

1. Run the database migration
2. Test with sample SMS messages using curl
3. Integrate with your expense/income tracking
4. Build UI to display parsed transactions

See `SMS_TRANSACTION_PARSER_IMPLEMENTATION.md` for complete documentation.
