-- V70: SMS regex pattern registry
-- Stores all bank SMS parsing regex as rows, allowing admin to update or add
-- new patterns without a code change or redeploy.
-- SMSParserService falls back to its hardcoded static patterns when no DB
-- row is present, so this table starts as a mirror of the code defaults.
--
-- patternValue format: raw regex string, exactly as passed to Pattern.compile().
-- Use single backslash where the regex engine needs a backslash (\\b, \\s, etc.)
-- In this SQL file every \ must be written as \\ so MySQL stores a single \.

CREATE TABLE IF NOT EXISTS sms_regex_patterns (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_key      VARCHAR(100) NOT NULL UNIQUE COMMENT 'Logical name, e.g. PARSER_AMOUNT_1',
    pattern_value    TEXT         NOT NULL         COMMENT 'Raw regex passed to Pattern.compile()',
    case_insensitive BOOLEAN      NOT NULL DEFAULT TRUE,
    description      VARCHAR(500),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Seed: PARSER patterns (SMSParserService)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO sms_regex_patterns (pattern_key, pattern_value, description) VALUES

('PARSER_AMOUNT_1',
 '(?:Rs\\.?|INR|₹)\\s*([0-9,]+\\.?[0-9]*)',
 'Currency-prefix amount: Rs 500, INR 25000, ₹150'),

('PARSER_AMOUNT_2',
 '([0-9,]+\\.?[0-9]*)\\s*(?:Rs\\.?|INR|₹)',
 'Currency-suffix amount: 500 Rs'),

('PARSER_FUTURE_INTENT',
 '\\b(will be debited|will be credited|scheduled|due on|due date|to be debited|to be credited|payment due|reminder)\\b',
 'Scheduled/reminder messages — not real transactions'),

('PARSER_DEBIT',
 '\\b(debited|withdrawn|paid|spent|deducted|purchase|debit|charged)\\b',
 'Debit action keywords'),

('PARSER_CREDIT',
 '\\b(credited|deposited|received|refund|cashback|credit)\\b',
 'Credit action keywords'),

('PARSER_RECURRING',
 '\\b(policy|premium|subscription|emi|auto debit|autopay|standing instruction|si debit|recurring|monthly)\\b',
 'Recurring payment signals'),

('PARSER_MERCHANT',
 '(?:at|to|from)\\s+([A-Z][A-Za-z0-9\\s&.-]{2,30})(?:\\s+on|\\.|,|\\s+A/c|\\s+using|\\s+via)',
 'Merchant / payee name extraction'),

('PARSER_ACCOUNT',
 '(?:A/c|Account|a/c|acc)\\s*(?:no\\.?)?\\s*(?:ending\\s*)?(?:[X*]+)?([0-9]{4,12})',
 'General account number (last 4-12 digits after X-mask)'),

('PARSER_FROM_ACCOUNT',
 'Your\\s+(?:a/c|account|acc)\\s*(?:no\\.?|number)?\\s*(?:XX+|X+)?([0-9]{4,10})',
 'Source account for transfer messages'),

('PARSER_TO_ACCOUNT',
 'to\\s+credit\\s+(?:a/c|account|acc)\\s*(?:no\\.?)?\\s*(?:ending\\s*)?(?:XX+|X+)?([0-9]{4,10})',
 'Destination account for transfer messages'),

('PARSER_UPI',
 '(?:UPI|upi)\\s*(?:Ref|ref|RRN|rrn|ID|id)?\\s*(?::|no\\.?)?\\s*([0-9]+)',
 'UPI reference / RRN number'),

('PARSER_REFERENCE',
 '(?:Ref\\s*no|Reference\\s*no|Txn\\s*ID|Transaction\\s*ID|UTR)\\s*(?::|\\.)\\s*([A-Z0-9]+)',
 'Transaction reference / UTR number'),

('PARSER_BALANCE',
 '(?:avl\\s*bal|available\\s*balance|balance|avl\\.\\s*bal|bal)\\s*(?:is)?\\s*(?:Rs\\.?|INR|₹)?\\s*([0-9,]+\\.?[0-9]*)',
 'Available balance after transaction'),

('PARSER_DATE_WITH_ON',
 '(?:on|date:?)\\s+([0-3]?[0-9][-/\\s][A-Za-z0-9]{2,4}[-/\\s][0-9]{2,4})',
 'Date preceded by "on" keyword, e.g. "on 23-07-2025"'),

('PARSER_DATE',
 '\\b([0-3]?[0-9][-/][0-1]?[0-9][-/][0-9]{2,4}|[0-3]?[0-9][-\\s][A-Za-z]{3}[-\\s,]*[0-9]{2,4}|[A-Za-z]{3}\\s+[0-3]?[0-9],?\\s+[0-9]{4})\\b',
 'Generic date: dd-MM-yyyy, dd/MM/yy, dd MMM yyyy, MMM dd yyyy, etc.');
