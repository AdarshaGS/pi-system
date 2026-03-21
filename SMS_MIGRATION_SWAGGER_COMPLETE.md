# SMS Transaction Parser - Migration & Swagger Integration

## ✅ Completed Tasks

### 1. Database Migration Script Created
**File**: `src/main/resources/db/migration/V65__Create_SMS_Transactions_Table.sql`

#### Features:
- Creates `sms_transactions` table with all required fields
- Includes constraints for data integrity (CHECK constraints for enums)
- Optimized indexes for performance:
  - User ID lookup
  - Parse status filtering
  - Transaction date queries
  - Processed/unprocessed filtering
  - Composite indexes for common query patterns

#### Schema Highlights:
```sql
- id: BIGINT (Primary Key)
- user_id: BIGINT (Indexed)
- original_message: TEXT
- amount: DECIMAL(15,2)
- transaction_date: DATE
- transaction_type: VARCHAR(20) - DEBIT/CREDIT/UNKNOWN
- parse_status: VARCHAR(20) - SUCCESS/PARTIAL/FAILED
- parse_confidence: DOUBLE (0.0-1.0)
- is_processed: BOOLEAN
- Multiple indexed columns for performance
```

### 2. Swagger/OpenAPI Integration

#### Updated Files:

1. **SmsController.java** - Added comprehensive OpenAPI annotations:
   - `@Tag` for controller grouping
   - `@Operation` for each endpoint with detailed descriptions
   - `@ApiResponses` with status codes and response schemas
   - `@Parameter` annotations for path variables and request bodies

2. **SMSImportRequest.java** - Added schema annotations:
   - Field descriptions
   - Example values
   - Nested class documentation

3. **SMSImportResponse.java** - Added schema annotations:
   - Result field descriptions
   - Transaction summary documentation
   - Error detail schemas

4. **SMSTransaction.java** - Added entity schema annotation:
   - Main entity description

5. **OpenApiConfig.java** - Added SMS tag:
   - New tag: "SMS Transaction Parser"
   - Description: "Parse bank SMS messages and extract transaction details automatically"

## API Documentation in Swagger

### Endpoints Documented:

#### 1. POST /api/v1/sms/import
**Summary**: Import Multiple SMS Messages  
**Description**: Parse and import multiple bank SMS messages at once. Supports various Indian bank formats.  
**Request**: SMSImportRequest  
**Response**: SMSImportResponse  
**Status Codes**:
- 200: Success
- 400: Invalid request
- 500: Server error

#### 2. POST /api/v1/sms/parse
**Summary**: Parse Single SMS Message  
**Description**: Parse and store a single bank SMS message with confidence scoring.  
**Request**: ParseSingleRequest  
**Response**: SMSTransaction  
**Status Codes**:
- 201: Created
- 400: Invalid request
- 500: Server error

#### 3. GET /api/v1/sms/user/{userId}
**Summary**: Get User SMS Transactions  
**Description**: Retrieve all parsed SMS transactions for a user.  
**Response**: List<SMSTransaction>  
**Status Codes**:
- 200: Success
- 404: User not found

#### 4. GET /api/v1/sms/user/{userId}/unprocessed
**Summary**: Get Unprocessed SMS Transactions  
**Description**: Get successfully parsed but not yet processed transactions.  
**Response**: List<SMSTransaction>  
**Status Codes**:
- 200: Success
- 404: User not found

## How to Access Swagger UI

1. **Start the Application**:
   ```bash
   ./gradlew bootRun
   ```

2. **Open Swagger UI**:
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

3. **Find SMS Endpoints**:
   - Look for the "SMS Transaction Parser" tag
   - Expand to see all 4 endpoints
   - Click "Try it out" to test each endpoint

## Example Swagger Request

### Import Multiple Messages:

```json
{
  "userId": 123,
  "messages": [
    {
      "content": "Rs.500 debited from A/c XX1234 on 12-03-2026 at AMAZON. Avl Bal: Rs.10,000",
      "sender": "HDFCBK",
      "timestamp": 1710234567000
    },
    {
      "content": "INR 2500 credited to your account XXXX5678 on 11/03/2026",
      "sender": "ICICIBK"
    }
  ]
}
```

### Expected Response:

```json
{
  "totalMessages": 2,
  "successfullyParsed": 2,
  "partiallyParsed": 0,
  "failed": 0,
  "duplicates": 0,
  "transactions": [
    {
      "transactionId": 101,
      "message": "Rs.500 debited from A/c XX1234 on 12-03-2026...",
      "status": "SUCCESS",
      "confidence": 0.95
    },
    {
      "transactionId": 102,
      "message": "INR 2500 credited to your account XXXX5678...",
      "status": "SUCCESS",
      "confidence": 0.85
    }
  ],
  "errors": []
}
```

## Migration Execution

### Apply Migration:

The migration will run automatically on next application startup if using Flyway/Liquibase.

**Manual execution**:
```bash
# MySQL
mysql -u your_user -p your_database < src/main/resources/db/migration/V65__Create_SMS_Transactions_Table.sql

# Or via application startup
./gradlew bootRun
```

### Verify Migration:

```sql
-- Check table exists
SHOW TABLES LIKE 'sms_transactions';

-- Check structure
DESCRIBE sms_transactions;

-- Check indexes
SHOW INDEX FROM sms_transactions;
```

## Swagger Schema Features

### Request Schemas:
✅ Field descriptions with examples  
✅ Required field indicators (@NotNull)  
✅ Nested object documentation  
✅ Validation constraints shown  

### Response Schemas:
✅ Success response models  
✅ Error response descriptions  
✅ Enum value documentation  
✅ Field examples and constraints  

### Interactive Testing:
✅ Try endpoints directly from Swagger UI  
✅ Auto-generated request examples  
✅ Response schema validation  
✅ HTTP status code documentation  

## Benefits

### For Developers:
- Clear API contract documentation
- Interactive testing interface
- Request/response examples
- Validation rules visible

### For API Consumers:
- Self-service API exploration
- Clear endpoint descriptions
- Example payloads provided
- Error response documentation

### For Testing:
- No need for external tools like Postman
- Test directly in browser
- Pre-filled examples
- Immediate validation feedback

## Next Steps

1. ✅ Start application
2. ✅ Visit Swagger UI (http://localhost:8080/swagger-ui/index.html)
3. ✅ Test SMS import endpoint
4. ✅ Verify database records created
5. ✅ Check response confidence scores

## Files Summary

### Created:
- `V65__Create_SMS_Transactions_Table.sql` - Database migration

### Modified:
- `SmsController.java` - Added OpenAPI annotations
- `SMSImportRequest.java` - Added schema annotations
- `SMSImportResponse.java` - Added schema annotations
- `SMSTransaction.java` - Added schema annotation
- `OpenApiConfig.java` - Added SMS tag

All files are properly documented and ready for production use!
