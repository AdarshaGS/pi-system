package com.pisystem.modules.sms.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.NormalizedTransaction;
import com.pisystem.modules.sms.data.SMSTransaction;

import lombok.extern.slf4j.Slf4j;

/**
 * Deterministic, rule-based normalization engine for SMS-parsed transactions.
 *
 * <h3>Normalization rules</h3>
 * <ol>
 *   <li><b>Merchant normalization</b> – strips noisy prefixes (UPI-, TO, FROM,
 *       PAYTM, NEFT, IMPS, P2A, P2M) and title-cases the result.</li>
 *   <li><b>Mode detection</b> – UPI &gt; ATM &gt; CARD &gt; NETBANKING &gt; UNKNOWN</li>
 *   <li><b>Status detection</b> – scans raw SMS for "failed / declined" →
 *       FAILED, "pending / processing" → PENDING, else SUCCESS.</li>
 *   <li><b>Flow-type classification</b> – CREDIT → INCOME, DEBIT → EXPENSE;
 *       overridden to TRANSFER when a self-transfer is detected.</li>
 *   <li><b>Category mapping</b> – prebuilt keyword tables for 12 categories.</li>
 *   <li><b>Confidence scoring</b> – additive 0-100 score; each resolved field
 *       contributes a weight.</li>
 * </ol>
 */
@Service
@Slf4j
public class TransactionNormalizationServiceImpl implements TransactionNormalizationService {

    // =========================================================================
    // Merchant prefix cleanup
    // =========================================================================

    /** Patterns stripped from the front of raw merchant strings (in order). */
    private static final List<Pattern> MERCHANT_PREFIX_PATTERNS = List.of(
            Pattern.compile("^(UPI[-/])", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(PAYTM[-/\\s]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(NEFT[-/\\s]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(IMPS[-/\\s]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(P2A[-/\\s]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(P2M[-/\\s]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(RTGS[-/\\s]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(TO\\s+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(FROM\\s+)", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Well-known brand aliases: rawMerchant fragment → canonical display name.
     * Matching is done case-insensitively; first winning key wins.
     */
    private static final Map<String, String> BRAND_ALIASES = Map.ofEntries(
            Map.entry("swiggy",       "Swiggy"),
            Map.entry("zomato",       "Zomato"),
            Map.entry("amazon",       "Amazon"),
            Map.entry("flipkart",     "Flipkart"),
            Map.entry("myntra",       "Myntra"),
            Map.entry("ajio",         "Ajio"),
            Map.entry("nykaa",        "Nykaa"),
            Map.entry("meesho",       "Meesho"),
            Map.entry("snapdeal",     "Snapdeal"),
            Map.entry("uber",         "Uber"),
            Map.entry("ola",          "Ola"),
            Map.entry("rapido",       "Rapido"),
            Map.entry("irctc",        "IRCTC"),
            Map.entry("makemytrip",   "MakeMyTrip"),
            Map.entry("goibibo",      "Goibibo"),
            Map.entry("redbus",       "RedBus"),
            Map.entry("yatra",        "Yatra"),
            Map.entry("netflix",      "Netflix"),
            Map.entry("spotify",      "Spotify"),
            Map.entry("hotstar",      "Disney+ Hotstar"),
            Map.entry("prime",        "Amazon Prime"),
            Map.entry("zee5",         "ZEE5"),
            Map.entry("sonyliv",      "SonyLIV"),
            Map.entry("youtube",      "YouTube"),
            Map.entry("bpcl",         "BPCL"),
            Map.entry("iocl",         "IOCL"),
            Map.entry("hpcl",         "HPCL"),
            Map.entry("dominos",      "Domino's"),
            Map.entry("pizza hut",    "Pizza Hut"),
            Map.entry("kfc",          "KFC"),
            Map.entry("mcdonalds",    "McDonald's"),
            Map.entry("mcdonald",     "McDonald's"),
            Map.entry("starbucks",    "Starbucks"),
            Map.entry("subway",       "Subway"),
            Map.entry("bigbasket",    "BigBasket"),
            Map.entry("blinkit",      "Blinkit"),
            Map.entry("zepto",        "Zepto"),
            Map.entry("dunzo",        "Dunzo"),
            Map.entry("phonepe",      "PhonePe"),
            Map.entry("gpay",         "Google Pay"),
            Map.entry("google pay",   "Google Pay"),
            Map.entry("paytm",        "Paytm"),
            Map.entry("razorpay",     "Razorpay"),
            Map.entry("cashfree",     "Cashfree"),
            Map.entry("juspay",       "Juspay")
    );

    // =========================================================================
    // Category keyword tables
    // =========================================================================

    private static final Map<String, Set<String>> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry("Food",         Set.of("swiggy", "zomato", "restaurant", "cafe", "dominos", "domino",
                                             "kfc", "mcdonald", "burger", "pizza", "subway", "starbucks",
                                             "dunkin", "biryani", "dhaba")),
            Map.entry("Groceries",    Set.of("bigbasket", "blinkit", "zepto", "dunzo", "grofers", "jiomart",
                                             "grocery", "supermarket", "dmart", "reliance fresh")),
            Map.entry("Shopping",     Set.of("amazon", "flipkart", "myntra", "ajio", "snapdeal", "meesho",
                                             "nykaa", "firstcry", "shopify", "mall", "retail")),
            Map.entry("Travel",       Set.of("uber", "ola", "rapido", "irctc", "makemytrip", "goibibo",
                                             "redbus", "yatra", "indigo", "spicejet", "airindia",
                                             "air india", "vistara", "airport", "railway", "metro")),
            Map.entry("Fuel",         Set.of("bpcl", "iocl", "hpcl", "petrol", "fuel", "shell", "bharat petroleum",
                                             "indian oil", "hp petro")),
            Map.entry("Entertainment",Set.of("netflix", "spotify", "hotstar", "prime video", "zee5",
                                             "sonyliv", "youtube", "bookmyshow", "pvr", "inox", "cinema")),
            Map.entry("Utilities",    Set.of("electricity", "bescom", "tneb", "msedcl", "bsnl", "jio",
                                             "airtel", "vodafone", "vi ", "internet", "broadband", "gas",
                                             "water bill", "municipal")),
            Map.entry("Healthcare",   Set.of("hospital", "clinic", "pharmacy", "apollo", "fortis",
                                             "medplus", "netmeds", "1mg", "practo", "doctor", "medical")),
            Map.entry("Education",    Set.of("school", "college", "university", "coursera", "udemy",
                                             "byjus", "vedantu", "classplus", "fees", "tuition")),
            Map.entry("Investment",   Set.of("zerodha", "groww", "upstox", "coin", "mutual fund",
                                             "icici direct", "hdfc securities", "sip", "nps", "ppf")),
            Map.entry("Insurance",    Set.of("lic", "insurance", "premium", "policy", "term plan",
                                             "health insurance", "motor insurance")),
            Map.entry("Transfer",     Set.of("self", "own account", "neft", "imps", "rtgs"))
    );

    // =========================================================================
    // Mode detection patterns
    // =========================================================================

    private static final Pattern UPI_MODE_PATTERN = Pattern.compile(
            "\\b(upi|vpa|@ybl|@oksbi|@okaxis|@okhdfcbank|@paytm|@ibl|@yesbank)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ATM_MODE_PATTERN = Pattern.compile(
            "\\b(atm|cash withdrawal|cash withdraw)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CARD_MODE_PATTERN = Pattern.compile(
            "\\b(pos|card|swipe|contactless|tap|ecommerce|online purchase)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern NETBANKING_MODE_PATTERN = Pattern.compile(
            "\\b(neft|imps|rtgs|netbanking|net banking|online transfer)\\b",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Status detection patterns
    // =========================================================================

    private static final Pattern FAILED_PATTERN = Pattern.compile(
            "\\b(failed|failure|declined|rejected|unsuccessful|could not be processed|not processed)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PENDING_PATTERN = Pattern.compile(
            "\\b(pending|processing|initiated|in process|under process)\\b",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Self-transfer signals
    // =========================================================================

    private static final Pattern SELF_TRANSFER_PATTERN = Pattern.compile(
            "\\b(transfer to own|self transfer|own account|moved to a/c|moved to account|fund transfer)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Matches bank-to-bank account transfers e.g. "to credit a/c 1614" or "credited to account XXXX". */
    private static final Pattern INTER_ACCOUNT_TRANSFER_PATTERN = Pattern.compile(
            "to\\s+credit\\s+(?:a/c|account|acc)|credit(?:ed)?\\s+(?:to|a/c|account)\\s+(?:a/c|account|acc|[0-9X])",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // UPI personal-id pattern  (matches abc@ybl style)
    // =========================================================================

    private static final Pattern UPI_PERSONAL_ID_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z]+",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Confidence weights (sum to ~100 for a perfectly parsed message)
    // =========================================================================

    private static final int W_AMOUNT          = 20;
    private static final int W_TYPE            = 15;
    private static final int W_DATE            = 10;
    private static final int W_MERCHANT        = 10;
    private static final int W_BRAND_MATCH     = 10;
    private static final int W_CATEGORY        = 8;
    private static final int W_MODE            = 8;
    private static final int W_REFERENCE       = 7;
    private static final int W_ACCOUNT         = 7;
    private static final int W_STATUS_CLEAR    = 5;

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public NormalizedTransaction normalize(SMSTransaction transaction, String rawSms) {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction must not be null");
        }

        String sms = rawSms != null ? rawSms : "";
        String smsLower = sms.toLowerCase();

        int confidence = 0;

        // ── Amount ──────────────────────────────────────────────────────────
        BigDecimal amount = transaction.getAmount();
        if (amount != null) confidence += W_AMOUNT;

        // ── Transaction type ────────────────────────────────────────────────
        String transactionType = resolveTransactionType(transaction);
        if (!"UNKNOWN".equals(transactionType)) confidence += W_TYPE;

        // ── Timestamp ───────────────────────────────────────────────────────
        String timestamp = transaction.getTransactionDate() != null
                ? transaction.getTransactionDate().toString()
                : null;
        if (timestamp != null) confidence += W_DATE;

        // ── Merchant normalization ───────────────────────────────────────────
        String rawMerchant = transaction.getMerchant();
        String strippedMerchant = stripMerchantPrefixes(rawMerchant);
        String normalizedMerchant = resolveNormalizedMerchant(strippedMerchant, smsLower);
        if (rawMerchant != null && !rawMerchant.isBlank()) confidence += W_MERCHANT;
        boolean brandMatched = !normalizedMerchant.equals(titleCase(strippedMerchant));
        if (brandMatched) confidence += W_BRAND_MATCH;

        // ── Category ────────────────────────────────────────────────────────
        String category = resolveCategory(normalizedMerchant, rawMerchant, smsLower, transaction.getCategory());
        if (!"UNKNOWN".equals(category)) confidence += W_CATEGORY;

        // ── Mode ─────────────────────────────────────────────────────────────
        String mode = detectMode(sms, transaction);
        if (!"UNKNOWN".equals(mode)) confidence += W_MODE;

        // ── Account ──────────────────────────────────────────────────────────
        String account = resolveAccount(transaction);
        if (account != null) confidence += W_ACCOUNT;

        // ── Reference ID ─────────────────────────────────────────────────────
        String referenceId = resolveReference(transaction);
        if (referenceId != null) confidence += W_REFERENCE;

        // ── Status ───────────────────────────────────────────────────────────
        String status = detectStatus(smsLower);
        if (!"UNKNOWN".equals(status)) confidence += W_STATUS_CLEAR;

        // ── Flow type ────────────────────────────────────────────────────────
        String flowType = resolveFlowType(transactionType, smsLower, transaction);

        // ── Cap confidence ───────────────────────────────────────────────────
        confidence = Math.min(100, confidence);

        log.debug("Normalized transaction: merchant='{}' → '{}', mode={}, flow={}, confidence={}",
                rawMerchant, normalizedMerchant, mode, flowType, confidence);

        return NormalizedTransaction.builder()
                .amount(amount)
                .transactionType(transactionType)
                .flowType(flowType)
                .merchantName(rawMerchant)
                .normalizedMerchant(normalizedMerchant)
                .category(category)
                .account(account)
                .mode(mode)
                .referenceId(referenceId)
                .timestamp(timestamp)
                .status(status)
                .confidenceScore(confidence)
                .build();
    }

    // =========================================================================
    // Transaction type
    // =========================================================================

    private String resolveTransactionType(SMSTransaction tx) {
        if (tx.getTransactionType() == null) return "UNKNOWN";
        return switch (tx.getTransactionType()) {
            case DEBIT  -> "DEBIT";
            case CREDIT -> "CREDIT";
            default     -> "UNKNOWN";
        };
    }

    // =========================================================================
    // Merchant strip & alias
    // =========================================================================

    /**
     * Strips known noise prefixes (UPI-, TO, FROM, etc.) from raw merchant string.
     * Returns empty string if input is null/blank.
     */
    String stripMerchantPrefixes(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String cleaned = raw.trim();
        for (Pattern p : MERCHANT_PREFIX_PATTERNS) {
            cleaned = p.matcher(cleaned).replaceFirst("").trim();
        }
        return cleaned;
    }

    /**
     * Resolves to a canonical brand name when possible; falls back to title-cased
     * stripped merchant, then "UNKNOWN".
     *
     * Special-cases: UPI personal IDs (user@vpa) → "Self / UPI"
     */
    private String resolveNormalizedMerchant(String stripped, String smsLower) {
        if (stripped == null || stripped.isBlank()) {
            // Check raw SMS for any @vpa pattern that signals a personal transfer
            if (UPI_PERSONAL_ID_PATTERN.matcher(smsLower).find()) {
                return "Self / UPI";
            }
            return "UNKNOWN";
        }

        // Personal UPI ID in the merchant field itself
        if (UPI_PERSONAL_ID_PATTERN.matcher(stripped).find()) {
            return "Self / UPI";
        }

        String lower = stripped.toLowerCase();

        // Check brand alias table — longest match wins
        String bestKey   = null;
        String bestAlias = null;
        for (Map.Entry<String, String> entry : BRAND_ALIASES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                if (bestKey == null || entry.getKey().length() > bestKey.length()) {
                    bestKey   = entry.getKey();
                    bestAlias = entry.getValue();
                }
            }
        }
        if (bestAlias != null) return bestAlias;

        // Fallback: title-case and trim
        String tc = titleCase(stripped);
        return tc.isBlank() ? "UNKNOWN" : tc;
    }

    // =========================================================================
    // Category
    // =========================================================================

    private String resolveCategory(String normalizedMerchant, String rawMerchant, String smsLower, String existingCategory) {
        // Prefer already-inferred category from parser unless it's blank/null
        if (existingCategory != null && !existingCategory.isBlank()
                && !"unknown".equalsIgnoreCase(existingCategory)) {
            return titleCase(existingCategory);
        }

        String searchText = (normalizedMerchant + " " + (rawMerchant != null ? rawMerchant : "") + " " + smsLower).toLowerCase();

        for (Map.Entry<String, Set<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (searchText.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "UNKNOWN";
    }

    // =========================================================================
    // Payment mode
    // =========================================================================

    String detectMode(String rawSms, SMSTransaction tx) {
        if (rawSms == null) rawSms = "";

        // Check explicit tags stored during parsing
        if (tx.getTags() != null) {
            String tags = tx.getTags().toUpperCase();
            if (tags.contains("UPI"))  return "UPI";
            if (tags.contains("ATM"))  return "ATM";
            if (tags.contains("CARD")) return "CARD";
        }

        // UPI — highest priority
        if (UPI_MODE_PATTERN.matcher(rawSms).find()) return "UPI";
        if (tx.getUpiId() != null && !tx.getUpiId().isBlank()) return "UPI";

        // ATM
        if (ATM_MODE_PATTERN.matcher(rawSms).find()) return "ATM";

        // CARD
        if (CARD_MODE_PATTERN.matcher(rawSms).find()) return "CARD";
        if (tx.getCardNumber() != null && !tx.getCardNumber().isBlank()) return "CARD";

        // NETBANKING
        if (NETBANKING_MODE_PATTERN.matcher(rawSms).find()) return "NETBANKING";

        return "UNKNOWN";
    }

    // =========================================================================
    // Transaction status
    // =========================================================================

    String detectStatus(String smsLower) {
        if (smsLower == null || smsLower.isBlank()) return "SUCCESS";
        if (FAILED_PATTERN.matcher(smsLower).find())  return "FAILED";
        if (PENDING_PATTERN.matcher(smsLower).find()) return "PENDING";
        return "SUCCESS";
    }

    // =========================================================================
    // Flow type
    // =========================================================================

    private String resolveFlowType(String transactionType, String smsLower, SMSTransaction tx) {
        // Self-transfer — override everything
        if ("self_transfer".equalsIgnoreCase(tx.getCategory())) {
            return "SELF_TRANSFER";
        }
        if (SELF_TRANSFER_PATTERN.matcher(smsLower).find()) {
            return "SELF_TRANSFER";
        }
        // Inter-account bank transfer: "to credit a/c XXXX" style messages
        if (INTER_ACCOUNT_TRANSFER_PATTERN.matcher(smsLower).find()) {
            return "TRANSFER";
        }
        // Parser detected both fromAccount + toAccount → tagged TRANSFER
        if (tx.getTags() != null && tx.getTags().toUpperCase().contains("TRANSFER")) {
            return "TRANSFER";
        }
        // Personal UPI transfer (user@vpa pattern in merchant or SMS)
        if (tx.getMerchant() != null && UPI_PERSONAL_ID_PATTERN.matcher(tx.getMerchant()).find()) {
            return "TRANSFER";
        }

        if ("CREDIT".equals(transactionType)) return "INCOME";
        if ("DEBIT".equals(transactionType))  return "EXPENSE";
        return "UNKNOWN";
    }

    // =========================================================================
    // Account / Reference helpers
    // =========================================================================

    private String resolveAccount(SMSTransaction tx) {
        if (tx.getAccountNumber() != null && !tx.getAccountNumber().isBlank()) {
            return tx.getAccountNumber();
        }
        if (tx.getCardNumber() != null && !tx.getCardNumber().isBlank()) {
            return "CARD-" + tx.getCardNumber();
        }
        return null;
    }

    private String resolveReference(SMSTransaction tx) {
        if (tx.getReferenceNumber() != null && !tx.getReferenceNumber().isBlank()) {
            return tx.getReferenceNumber();
        }
        if (tx.getUpiId() != null && !tx.getUpiId().isBlank()) {
            return tx.getUpiId();
        }
        return null;
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /** Title-cases each word in the input string. */
    String titleCase(String input) {
        if (input == null || input.isBlank()) return "";
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }
}
