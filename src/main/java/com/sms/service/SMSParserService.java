package com.sms.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.sms.data.ParsedSMSData;
import com.sms.data.SMSTransaction.ParseStatus;
import com.sms.data.SMSTransaction.TransactionType;

import lombok.extern.slf4j.Slf4j;

/**
 * Service to parse SMS messages from various banks and payment systems
 */
@Service
@Slf4j
public class SMSParserService {

    // Common patterns for Indian bank SMS
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?:Rs\\.?|INR|₹)\\s*([0-9,]+\\.?[0-9]*)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern AMOUNT_PATTERN_2 = Pattern.compile(
        "([0-9,]+\\.?[0-9]*)\\s*(?:Rs\\.?|INR|₹)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Debit keywords
    private static final List<String> DEBIT_KEYWORDS = List.of(
        "debited", "withdrawn", "paid", "spent", "deducted", "purchase", "debit"
    );
    
    // Credit keywords
    private static final List<String> CREDIT_KEYWORDS = List.of(
        "credited", "deposited", "received", "refund", "cashback", "credit"
    );
    
    // Date patterns - various formats used by Indian banks
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yy"),
        DateTimeFormatter.ofPattern("dd/MM/yy"),
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
        DateTimeFormatter.ofPattern("dd-MMM-yy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("ddMMMyyyy"),
        DateTimeFormatter.ofPattern("ddMMMyy")
    );
    
    // Time pattern
    private static final Pattern TIME_PATTERN = Pattern.compile(
        "([0-2]?[0-9]):([0-5][0-9])(?::([0-5][0-9]))?\\s*(AM|PM)?",
        Pattern.CASE_INSENSITIVE
    );
    
    // Account number pattern (last 4 digits)
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile(
        "(?:A/c|Account|a/c|acc)\\s*(?:no\\.?)?\\s*(?:ending\\s*)?(?:XX+)?([0-9]{4})",
        Pattern.CASE_INSENSITIVE
    );
    
    // Card number pattern (last 4 digits)
    private static final Pattern CARD_PATTERN = Pattern.compile(
        "(?:Card|card)\\s*(?:no\\.?)?\\s*(?:ending\\s*)?(?:XX+)?([0-9]{4})",
        Pattern.CASE_INSENSITIVE
    );
    
    // Balance pattern
    private static final Pattern BALANCE_PATTERN = Pattern.compile(
        "(?:avl\\s*bal|available\\s*balance|balance|avl\\.\\s*bal|bal)\\s*(?:is)?\\s*(?:Rs\\.?|INR|₹)?\\s*([0-9,]+\\.?[0-9]*)",
        Pattern.CASE_INSENSITIVE
    );
    
    // UPI reference pattern
    private static final Pattern UPI_PATTERN = Pattern.compile(
        "(?:UPI|upi)\\s*(?:Ref|ref|ID|id)?\\s*(?::|no\\.?)?\\s*([0-9]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Reference number pattern
    private static final Pattern REF_PATTERN = Pattern.compile(
        "(?:Ref\\s*no|Reference\\s*no|Txn\\s*ID|Transaction\\s*ID|UTR)\\s*(?::|\\.)\\s*([A-Z0-9]+)",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Parse an SMS message and extract transaction details
     */
    public ParsedSMSData parseSMS(String message) {
        log.debug("Parsing SMS: {}", message);
        
        ParsedSMSData.ParsedSMSDataBuilder builder = ParsedSMSData.builder();
        double confidence = 0.0;
        
        try {
            // Extract amount (most critical field)
            BigDecimal amount = extractAmount(message);
            if (amount != null) {
                builder.amount(amount);
                confidence += 0.3; // Amount is weighted more
            }
            
            // Extract transaction type
            TransactionType type = extractTransactionType(message);
            builder.transactionType(type);
            if (type != TransactionType.UNKNOWN) {
                confidence += 0.2; // Type is important
            }
            
            // Extract date
            LocalDate date = extractDate(message);
            if (date != null) {
                builder.transactionDate(date);
                confidence += 0.1;
            }
            
            // Extract time
            LocalTime time = extractTime(message);
            if (time != null) {
                builder.transactionTime(time);
                confidence += 0.05;
            }
            
            // Extract merchant/description
            String merchant = extractMerchant(message);
            if (merchant != null && !merchant.isEmpty()) {
                builder.merchant(merchant);
                confidence += 0.1;
            }
            
            // Extract account number
            String account = extractAccount(message);
            if (account != null) {
                builder.accountNumber(account);
                confidence += 0.05;
            }
            
            // Extract card number
            String card = extractCard(message);
            if (card != null) {
                builder.cardNumber(card);
                confidence += 0.05;
            }
            
            // Extract balance
            BigDecimal balance = extractBalance(message);
            if (balance != null) {
                builder.balance(balance);
                confidence += 0.1;
            }
            
            // Extract UPI reference
            String upiId = extractUpiId(message);
            if (upiId != null) {
                builder.upiId(upiId);
                confidence += 0.05;
            }
            
            // Extract reference number
            String refNumber = extractReferenceNumber(message);
            if (refNumber != null) {
                builder.referenceNumber(refNumber);
            }
            
            // Normalize confidence to 0-1 range
            builder.confidence(Math.min(1.0, confidence));
            
            // Determine parse status
            if (amount != null && type != TransactionType.UNKNOWN) {
                builder.parseStatus(ParseStatus.SUCCESS);
            } else if (amount != null || type != TransactionType.UNKNOWN) {
                builder.parseStatus(ParseStatus.PARTIAL);
                builder.errorMessage("Could not extract all critical fields");
            } else {
                builder.parseStatus(ParseStatus.FAILED);
                builder.errorMessage("Could not extract amount or transaction type");
            }
            
        } catch (Exception e) {
            log.error("Error parsing SMS", e);
            builder.parseStatus(ParseStatus.FAILED);
            builder.errorMessage("Parsing exception: " + e.getMessage());
            builder.confidence(0.0);
        }
        
        return builder.build();
    }
    
    /**
     * Extract amount from SMS
     */
    private BigDecimal extractAmount(String message) {
        Matcher matcher = AMOUNT_PATTERN.matcher(message);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            try {
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse amount: {}", amountStr);
            }
        }
        
        // Try alternative pattern
        matcher = AMOUNT_PATTERN_2.matcher(message);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            try {
                return new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse amount: {}", amountStr);
            }
        }
        
        return null;
    }
    
    /**
     * Extract transaction type (DEBIT/CREDIT)
     */
    private TransactionType extractTransactionType(String message) {
        String lowerMsg = message.toLowerCase();
        
        for (String keyword : DEBIT_KEYWORDS) {
            if (lowerMsg.contains(keyword)) {
                return TransactionType.DEBIT;
            }
        }
        
        for (String keyword : CREDIT_KEYWORDS) {
            if (lowerMsg.contains(keyword)) {
                return TransactionType.CREDIT;
            }
        }
        
        return TransactionType.UNKNOWN;
    }
    
    /**
     * Extract date from SMS
     */
    private LocalDate extractDate(String message) {
        // Try each date format
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            Pattern datePattern = Pattern.compile(
                "\\b([0-3]?[0-9][-/][0-1]?[0-9][-/][0-9]{2,4}|[0-3]?[0-9]-[A-Za-z]{3}-[0-9]{2,4})\\b"
            );
            Matcher matcher = datePattern.matcher(message);
            
            while (matcher.find()) {
                String dateStr = matcher.group(1);
                try {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    // Sanity check: date should not be in future
                    if (!date.isAfter(LocalDate.now())) {
                        return date;
                    }
                } catch (DateTimeParseException e) {
                    // Try next format
                }
            }
        }
        
        // If no date found, assume today
        return LocalDate.now();
    }
    
    /**
     * Extract time from SMS
     */
    private LocalTime extractTime(String message) {
        Matcher matcher = TIME_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = Integer.parseInt(matcher.group(2));
                String ampm = matcher.group(4);
                
                if (ampm != null) {
                    if (ampm.equalsIgnoreCase("PM") && hour < 12) {
                        hour += 12;
                    } else if (ampm.equalsIgnoreCase("AM") && hour == 12) {
                        hour = 0;
                    }
                }
                
                return LocalTime.of(hour, minute);
            } catch (Exception e) {
                log.warn("Failed to parse time", e);
            }
        }
        return null;
    }
    
    /**
     * Extract merchant/description from SMS
     */
    private String extractMerchant(String message) {
        // Look for common patterns
        Pattern merchantPattern = Pattern.compile(
            "(?:at|to|from)\\s+([A-Z][A-Za-z0-9\\s&.-]{2,30})(?:\\s+on|\\.|,|\\s+A/c)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = merchantPattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    /**
     * Extract account number (last 4 digits)
     */
    private String extractAccount(String message) {
        Matcher matcher = ACCOUNT_PATTERN.matcher(message);
        if (matcher.find()) {
            return "XXXX" + matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extract card number (last 4 digits)
     */
    private String extractCard(String message) {
        Matcher matcher = CARD_PATTERN.matcher(message);
        if (matcher.find()) {
            return "XXXX" + matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extract balance
     */
    private BigDecimal extractBalance(String message) {
        Matcher matcher = BALANCE_PATTERN.matcher(message);
        if (matcher.find()) {
            String balanceStr = matcher.group(1).replace(",", "");
            try {
                return new BigDecimal(balanceStr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse balance: {}", balanceStr);
            }
        }
        return null;
    }
    
    /**
     * Extract UPI ID
     */
    private String extractUpiId(String message) {
        Matcher matcher = UPI_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extract reference number
     */
    private String extractReferenceNumber(String message) {
        Matcher matcher = REF_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
