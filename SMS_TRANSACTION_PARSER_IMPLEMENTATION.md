# SMS Transaction Parsing Module - Complete Implementation

## Overview
The SMS Transaction Parsing Module allows you to import bank SMS messages and automatically extract transaction details including amount, date, merchant, account information, and more. The parsed data is stored in the `sms_transactions` table for further processing.

## Features
- ✅ Batch import of multiple SMS messages
- ✅ Intelligent parsing of various Indian bank SMS formats
- ✅ Support for both Debit and Credit transactions
- ✅ Extraction of: Amount, Date, Time, Merchant, Account Number, Balance, UPI ID, Reference Number
- ✅ Confidence scoring for parsed data
- ✅ Duplicate detection
- ✅ Error handling and detailed reporting
- ✅ RESTful API endpoints

## Architecture

### Components Created

1. **Entity**: `SMSTransaction.java`
   - Enhanced entity with comprehensive fields
   - Enums for TransactionType and ParseStatus
   - Lombok annotations for cleaner code

2. **Repository**: `SMSTransactionRepository.java`
   - JPA repository with custom query methods
   - Date range queries, status filtering
   - Duplicate checking

3. **DTOs**:
   - `ParsedSMSData.java` - Internal DTO for parsed data
   - `SMSImportRequest.java` - Request DTO for batch import
   - `SMSImportResponse.java` - Response DTO with detailed results

4. **Services**:
   - `SMSParserService.java` - Core parsing logic with regex patterns
   - `SmsService.java` - Service interface
   - `SmsServiceImpl.java` - Service implementation

5. **Controller**: `SmsController.java`
   - RESTful endpoints for SMS management

## Database Schema

### Table: `sms_transactions`

```sql
CREATE TABLE sms_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_message TEXT NOT NULL,
    sender VARCHAR(50),
    amount DECIMAL(15,2),
    transaction_date DATE,
    transaction_time TIME,
    transaction_type VARCHAR(20),  -- DEBIT, CREDIT, UNKNOWN
    merchant VARCHAR(255),
    account_number VARCHAR(50),
    card_number VARCHAR(50),
    balance DECIMAL(15,2),
    reference_number VARCHAR(100),
    upi_id VARCHAR(100),
    parse_status VARCHAR(20) NOT NULL, -- SUCCESS, PARTIAL, FAILED
    parse_confidence DOUBLE,
    error_message VARCHAR(500),
    is_processed BOOLEAN DEFAULT FALSE,
    linked_expense_id BIGINT,
    linked_income_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_parse_status (parse_status),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_is_processed (is_processed)
);
```

### Migration SQL

You can add this to your database migration:

```sql
-- Migration for SMS Transaction Parsing Module
-- Date: 2026-03-12

-- Drop table if exists (only for development)
-- DROP TABLE IF EXISTS sms_transactions;

-- Create sms_transactions table
CREATE TABLE IF NOT EXISTS sms_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_message TEXT NOT NULL,
    sender VARCHAR(50),
    amount DECIMAL(15,2),
    transaction_date DATE,
    transaction_time TIME,
    transaction_type VARCHAR(20) CHECK (transaction_type IN ('DEBIT', 'CREDIT', 'UNKNOWN')),
    merchant VARCHAR(255),
    account_number VARCHAR(50),
    card_number VARCHAR(50),
    balance DECIMAL(15,2),
    reference_number VARCHAR(100),
    upi_id VARCHAR(100),
    parse_status VARCHAR(20) NOT NULL CHECK (parse_status IN ('SUCCESS', 'PARTIAL', 'FAILED')),
    parse_confidence DOUBLE CHECK (parse_confidence BETWEEN 0.0 AND 1.0),
    error_message VARCHAR(500),
    is_processed BOOLEAN DEFAULT FALSE,
    linked_expense_id BIGINT,
    linked_income_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_parse_status (parse_status),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_is_processed (is_processed),
    INDEX idx_user_status (user_id, parse_status),
    INDEX idx_user_processed (user_id, is_processed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## API Endpoints

### 1. Import Multiple SMS Messages

**Endpoint**: `POST /api/v1/sms/import`

**Request Body**:
```json
{
  "userId": 123,
  "messages": [
    {
      "content": "Rs.500.00 debited from A/c XX1234 on 12-Mar-2026 at 14:30 at AMAZON INDIA. Avl Bal: Rs.45,500.00",
      "sender": "HDFCBK",
      "timestamp": 1710234567000
    },
    {
      "content": "INR 2500 credited to your account XXXX5678 on 11/03/2026. Ref: UPI/409876543210",
      "sender": "ICICIBK"
    },
    {
      "content": "You have paid Rs.1250 to SWIGGY using card XX9876 on 12-03-26 15:45. Available balance: 44250.00"
    }
  ]
}
```

**Response**:
```json
{
  "totalMessages": 3,
  "successfullyParsed": 3,
  "partiallyParsed": 0,
  "failed": 0,
  "duplicates": 0,
  "transactions": [
    {
      "transactionId": 101,
      "message": "Rs.500.00 debited from A/c XX1234 on 12-Mar...",
      "status": "SUCCESS",
      "confidence": 0.95
    },
    {
      "transactionId": 102,
      "message": "INR 2500 credited to your account XXXX5678...",
      "status": "SUCCESS",
      "confidence": 0.85
    },
    {
      "transactionId": 103,
      "message": "You have paid Rs.1250 to SWIGGY using car...",
      "status": "SUCCESS",
      "confidence": 0.9
    }
  ],
  "errors": []
}
```

### 2. Parse Single SMS Message

**Endpoint**: `POST /api/v1/sms/parse`

**Request Body**:
```json
{
  "userId": 123,
  "message": "Rs.750 debited from A/c XX3456 on 12-03-2026 to UBER",
  "sender": "SBIINB"
}
```

**Response**:
```json
{
  "id": 104,
  "userId": 123,
  "originalMessage": "Rs.750 debited from A/c XX3456 on 12-03-2026 to UBER",
  "sender": "SBIINB",
  "amount": 750.00,
  "transactionDate": "2026-03-12",
  "transactionTime": null,
  "transactionType": "DEBIT",
  "merchant": "UBER",
  "accountNumber": "XXXX3456",
  "cardNumber": null,
  "balance": null,
  "referenceNumber": null,
  "upiId": null,
  "parseStatus": "SUCCESS",
  "parseConfidence": 0.85,
  "errorMessage": null,
  "isProcessed": false,
  "linkedExpenseId": null,
  "linkedIncomeId": null,
  "createdAt": "2026-03-12T10:30:45.123"
}
```

### 3. Get User Transactions

**Endpoint**: `GET /api/v1/sms/user/{userId}`

**Response**: Array of SMSTransaction objects

### 4. Get Unprocessed Transactions

**Endpoint**: `GET /api/v1/sms/user/{userId}/unprocessed`

**Response**: Array of successfully parsed but unprocessed transactions

## Usage Examples

### Example 1: Import SMS from Phone Export

```bash
curl -X POST http://localhost:8080/api/v1/sms/import \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "messages": [
      {
        "content": "Dear Customer, Rs.1500.00 debited from A/c XX1234 on 12-03-2026 14:30 at FLIPKART. Available Bal: Rs.48,500.00. HDFC Bank",
        "sender": "HDFCBK",
        "timestamp": 1710234567000
      }
    ]
  }'
```

### Example 2: Parse Various Bank SMS Formats

The parser can handle multiple formats:

**HDFC Bank**:
```
Rs.500 debited from A/c XX1234 on 12-Mar-26 at AMAZON. Avl Bal: Rs.10,000
```

**ICICI Bank**:
```
INR 2500 credited to your account XXXX5678 on 11/03/2026. Ref: UPI/409876543210
```

**SBI**:
```
Your A/C XX9876 is debited with Rs.750.00 on 12-03-26 for SWIGGY transaction
```

**Axis Bank**:
```
Dear Customer, Rs 1250.50 has been debited from card XX4567 at UBER on 12/03/2026 15:30
```

**Paytm/UPI**:
```
Rs 250 debited from A/c XX1111 to merchant@paytm UPI ID: 123456789012
```

## Supported SMS Patterns

The parser recognizes:

### Transaction Types
- **Debit**: debited, withdrawn, paid, spent, deducted, purchase
- **Credit**: credited, deposited, received, refund, cashback

### Amount Formats
- Rs.500, Rs500, Rs.500.00
- INR 500, INR500
- ₹500, ₹ 500
- 500 Rs, 500.00 INR

### Date Formats
- DD-MM-YYYY, DD/MM/YYYY
- DD-MM-YY, DD/MM/YY
- DD-MMM-YYYY (12-Mar-2026)
- YYYY-MM-DD
- DDMMMYYYY, DDMMYY

### Time Formats
- HH:MM, HH:MM:SS
- HH:MM AM/PM

## Confidence Scoring

The parser assigns a confidence score (0.0 to 1.0):

- **Amount extracted**: +0.30
- **Transaction type identified**: +0.20
- **Date extracted**: +0.10
- **Time extracted**: +0.05
- **Merchant identified**: +0.10
- **Account number extracted**: +0.05
- **Card number extracted**: +0.05
- **Balance extracted**: +0.10
- **UPI ID extracted**: +0.05

**Parse Status**:
- `SUCCESS`: Amount AND transaction type extracted
- `PARTIAL`: Either amount OR transaction type extracted
- `FAILED`: Neither amount nor transaction type extracted

## Integration with Budget Module

Unprocessed transactions can be converted to expenses or income:

```java
// Get unprocessed transactions
List<SMSTransaction> unprocessed = smsService.getUnprocessedTransactions(userId);

// Convert to expense
for (SMSTransaction sms : unprocessed) {
    if (sms.getTransactionType() == TransactionType.DEBIT) {
        Expense expense = new Expense();
        expense.setUserId(sms.getUserId());
        expense.setAmount(sms.getAmount());
        expense.setExpenseDate(sms.getTransactionDate());
        expense.setDescription(sms.getMerchant() != null ? sms.getMerchant() : "SMS Transaction");
        // ... set other fields
        
        Expense saved = expenseRepository.save(expense);
        
        // Mark SMS as processed
        sms.setIsProcessed(true);
        sms.setLinkedExpenseId(saved.getId());
        smsRepository.save(sms);
    }
}
```

## Error Handling

The module includes comprehensive error handling:

1. **Duplicate Detection**: Checks if the same message has already been imported
2. **Parse Failures**: Captured with error messages in the response
3. **Validation**: Request validation using Jakarta Bean Validation
4. **Transaction Support**: Database operations are transactional

## Testing

### Manual Testing with curl

```bash
# Test import
curl -X POST http://localhost:8080/api/v1/sms/import \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "messages": [
      {
        "content": "Rs.100 debited from A/c XX1234 on 12-03-2026",
        "sender": "TEST"
      }
    ]
  }'

# Get transactions
curl http://localhost:8080/api/v1/sms/user/1

# Get unprocessed
curl http://localhost:8080/api/v1/sms/user/1/unprocessed
```

### Test Data Examples

```json
{
  "userId": 1,
  "messages": [
    {"content": "Rs.500.00 debited from A/c XX1234 on 12-Mar-2026 at 14:30 at AMAZON INDIA. Avl Bal: Rs.45,500.00"},
    {"content": "INR 2500 credited to your account XXXX5678 on 11/03/2026. Ref: UPI/409876543210"},
    {"content": "Dear Customer, your A/C XX9876 is debited by Rs 1250.50 on 12/03/2026 15:30 for FLIPKART"},
    {"content": "Rs 250 paid using card XX4567 to SWIGGY on 12-03-26. Balance: 44000"},
    {"content": "UPI Ref 123456789012: Rs 150 sent to merchant@paytm from A/c XX1111"}
  ]
}
```

## Future Enhancements

Potential improvements:

1. **Machine Learning**: Use ML for better merchant categorization
2. **Smart Categorization**: Auto-assign expense categories based on merchant
3. **Auto-Processing**: Automatically create expenses/income from parsed SMS
4. **Bank-Specific Parsers**: Dedicated parsers for each major bank
5. **SMS Notifications**: Alert user about parsed transactions
6. **Dashboard**: Visual analytics of SMS transactions
7. **Export**: Export parsed transactions to CSV/Excel

## Troubleshooting

### Common Issues

1. **Low Confidence Scores**
   - Check SMS format matches common patterns
   - Review parser regex patterns
   - Add bank-specific patterns if needed

2. **Parse Failures**
   - Check error messages in response
   - Verify SMS contains amount and transaction type keywords
   - Review original message format

3. **Duplicate Messages**
   - System automatically skips duplicates
   - Based on exact match of original message

## Files Created/Modified

### New Files
- `src/main/java/com/sms/data/SMSTransaction.java` (Enhanced)
- `src/main/java/com/sms/data/ParsedSMSData.java`
- `src/main/java/com/sms/data/SMSImportRequest.java`
- `src/main/java/com/sms/data/SMSImportResponse.java`
- `src/main/java/com/sms/repo/SMSTransactionRepository.java`
- `src/main/java/com/sms/service/SMSParserService.java`

### Modified Files
- `src/main/java/com/sms/service/SmsService.java`
- `src/main/java/com/sms/service/SmsServiceImpl.java`
- `src/main/java/com/sms/controller/SmsController.java`

## Summary

The SMS Transaction Parsing Module is now fully implemented and ready to use. You can:

✅ Import single or multiple SMS messages
✅ Automatically extract transaction details
✅ Store parsed data in the database
✅ Query transactions by user, status, date range
✅ Track parsing confidence and errors
✅ Integrate with expense/income tracking

The module supports various Indian bank SMS formats and provides comprehensive error handling and reporting.
