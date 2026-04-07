package com.pisystem.modules.sms.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.SmsClassificationResult;
import com.pisystem.modules.sms.data.SmsClassificationResult.MessageType;

import lombok.extern.slf4j.Slf4j;

/**
 * Rule-based, deterministic SMS classifier.
 *
 * <h3>Classification pipeline (in priority order)</h3>
 * <ol>
 *   <li><b>OTP guard</b>    — matched first; OTPs are never financial.</li>
 *   <li><b>Promotional</b>  — ad / offer / discount signals → NOT financial.</li>
 *   <li><b>Service</b>      — utility / alert / info with no amount → NOT financial.</li>
 *   <li><b>Transaction</b>  — amount + action keyword → financial.</li>
 *   <li><b>Unknown</b>      — insufficient signal; low confidence.</li>
 * </ol>
 *
 * <h3>Confidence scoring (additive, capped at 100)</h3>
 * <table>
 *   <tr><th>Signal</th><th>Points</th></tr>
 *   <tr><td>Currency symbol / keyword (₹, Rs, INR)</td><td>+25</td></tr>
 *   <tr><td>Action verb (debited, credited, withdrawn…)</td><td>+25</td></tr>
 *   <tr><td>Payment rail (UPI, ATM, NEFT, IMPS, RTGS, CARD)</td><td>+15</td></tr>
 *   <tr><td>Balance update after transaction</td><td>+10</td></tr>
 *   <tr><td>Account / card number present</td><td>+10</td></tr>
 *   <tr><td>Reference / UPI RRN present</td><td>+10</td></tr>
 *   <tr><td>Bank sender pattern (e.g. VM-HDFCBK)</td><td>+5</td></tr>
 * </table>
 */
@Service
@Slf4j
public class SmsClassifierServiceImpl implements SmsClassifierService {

    // =========================================================================
    // Rule 1 — OTP signals (checked first — OTPs are never financial)
    // =========================================================================

    private static final Pattern OTP_PATTERN = Pattern.compile(
            "\\b(otp|one.?time.?password|verification.?code|is your (otp|code|pin)|do not share|" +
            "enter.*\\b\\d{4,8}\\b.*to (verify|login|authenticate))\\b",
            Pattern.CASE_INSENSITIVE);

    /** A standalone 4-8 digit block that is the dominant content signals OTP. */
    private static final Pattern OTP_CODE_PATTERN = Pattern.compile(
            "(?:^|\\s)(\\d{4,8})(?:\\s|$)");

    // =========================================================================
    // Rule 2 — Promotional signals
    // =========================================================================

    private static final Pattern PROMO_PATTERN = Pattern.compile(
            "\\b(offer|discount|cashback offer|win|congratulations|you have won|" +
            "click here|apply now|limited time|exclusive deal|coupon|promo|" +
            "buy now|shop now|get \\d+%|flat \\d+% off|upto \\d+% off|" +
            "unsubscribe|opt.?out|reply stop|advertisement|advert|ad:|" +
            "upgrade now|free trial|no cost emi|no-cost emi)\\b",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Rule 3 — Service / informational signals (no amount involved)
    // =========================================================================

    private static final Pattern SERVICE_PATTERN = Pattern.compile(
            "\\b(your (account|profile|password|login|kyc|mobile number|email) (has been|is|was) " +
            "(updated|changed|verified|registered|activated|linked|blocked|unblocked)|" +
            "kyc (completed|pending|required|verified)|" +
            "feedback|rate us|how was your experience|survey|" +
            "scheduled maintenance|service (downtime|unavailable)|" +
            "congratulations.*account.*opened|welcome to|" +
            "your (request|complaint|ticket|issue) (number|id|has been))\\b",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Rule 4 — Transaction signals
    // =========================================================================

    /** Indian currency amount — presence is a strong TRANSACTION signal. */
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
            "(?:Rs\\.?|INR|₹)\\s*[0-9,]+\\.?[0-9]*|[0-9,]+\\.?[0-9]*\\s*(?:Rs\\.?|INR|₹)",
            Pattern.CASE_INSENSITIVE);

    /** Debit / spend action words. */
    private static final Pattern DEBIT_ACTION_PATTERN = Pattern.compile(
            "\\b(debited|deducted|withdrawn|paid|spent|charged|purchase|" +
            "payment (of|for)|amount (of|deducted)|debit)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Credit / receive action words. */
    private static final Pattern CREDIT_ACTION_PATTERN = Pattern.compile(
            "\\b(credited|deposited|received|refunded|cashback|" +
            "amount credited|credit)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Payment channel / rail keywords. */
    private static final Pattern PAYMENT_RAIL_PATTERN = Pattern.compile(
            "\\b(upi|atm|neft|imps|rtgs|pos|card|netbanking|net banking|" +
            "nach|ecs|cheque|dd|demand draft|wallet|gpay|phonepe|paytm)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Balance update — strong confirmation of a completed transaction. */
    private static final Pattern BALANCE_UPDATE_PATTERN = Pattern.compile(
            "\\b(avl\\.?\\s*bal|available balance|a/c bal|balance is|" +
            "bal\\.?\\s*(?:rs|inr|₹)|closing balance|ac bal)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Masked account or card number. */
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile(
            "\\b(?:a/c|account|acc|card)\\s*(?:no\\.?\\s*)?(?:xx+|\\*+)?[0-9]{4}\\b",
            Pattern.CASE_INSENSITIVE);

    /** UPI RRN, Ref no, UTR — confirmation reference. */
    private static final Pattern REFERENCE_PATTERN = Pattern.compile(
            "\\b(?:upi\\s*(?:rrn|ref|id)|ref\\s*no|txn\\s*id|transaction\\s*id|utr|rrn)\\s*[:\\-]?\\s*[a-z0-9]+\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Future-intent phrases — these look financial but are NOT completed transactions.
     * They DISQUALIFY a message from being TRANSACTION even if it has an amount.
     */
    private static final Pattern FUTURE_INTENT_PATTERN = Pattern.compile(
            "\\b(will be debited|will be credited|scheduled|due on|due date|" +
            "to be debited|to be credited|payment due|reminder|" +
            "auto.?debit.*on|emi.*due|emi.*on)\\b",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Confidence weights
    // =========================================================================

    private static final int W_CURRENCY       = 25;
    private static final int W_ACTION         = 25;
    private static final int W_PAYMENT_RAIL   = 15;
    private static final int W_BALANCE_UPDATE = 10;
    private static final int W_ACCOUNT        = 10;
    private static final int W_REFERENCE      =  5;

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public SmsClassificationResult classify(String rawSms) {
        if (rawSms == null || rawSms.isBlank()) {
            return SmsClassificationResult.builder()
                    .isFinancial(false)
                    .messageType(MessageType.UNKNOWN)
                    .confidenceScore(0)
                    .reason("Empty or null SMS")
                    .build();
        }

        final String lower = rawSms.toLowerCase();

        // ── Stage 1: OTP ──────────────────────────────────────────────────────
        if (isOtp(rawSms, lower)) {
            return SmsClassificationResult.builder()
                    .isFinancial(false)
                    .messageType(MessageType.OTP)
                    .confidenceScore(95)
                    .reason("Contains OTP / verification code signals")
                    .build();
        }

        // ── Stage 2: Promotional ──────────────────────────────────────────────
        if (isPromotional(lower)) {
            return SmsClassificationResult.builder()
                    .isFinancial(false)
                    .messageType(MessageType.PROMOTIONAL)
                    .confidenceScore(90)
                    .reason("Contains promotional / marketing signals (offer, discount, win, etc.)")
                    .build();
        }

        // ── Stage 3: Future intent — scheduled / reminder messages ────────────
        // Check before scoring: they may have amounts but are not real transactions.
        if (FUTURE_INTENT_PATTERN.matcher(lower).find()) {
            return SmsClassificationResult.builder()
                    .isFinancial(false)
                    .messageType(MessageType.SERVICE)
                    .confidenceScore(85)
                    .reason("Future-intent / reminder message — not a completed transaction")
                    .build();
        }

        // ── Stage 4: Transaction scoring ─────────────────────────────────────
        List<String> matchedSignals = new ArrayList<>();
        int score = 0;

        boolean hasCurrency = CURRENCY_PATTERN.matcher(rawSms).find();
        boolean hasDebit    = DEBIT_ACTION_PATTERN.matcher(lower).find();
        boolean hasCredit   = CREDIT_ACTION_PATTERN.matcher(lower).find();
        boolean hasAction   = hasDebit || hasCredit;
        boolean hasRail     = PAYMENT_RAIL_PATTERN.matcher(lower).find();
        boolean hasBalance  = BALANCE_UPDATE_PATTERN.matcher(lower).find();
        boolean hasAccount  = ACCOUNT_PATTERN.matcher(lower).find();
        boolean hasRef      = REFERENCE_PATTERN.matcher(lower).find();

        if (hasCurrency) { score += W_CURRENCY;       matchedSignals.add("currency"); }
        if (hasAction)   { score += W_ACTION;          matchedSignals.add(hasDebit ? "debit-action" : "credit-action"); }
        if (hasRail)     { score += W_PAYMENT_RAIL;    matchedSignals.add("payment-rail"); }
        if (hasBalance)  { score += W_BALANCE_UPDATE;  matchedSignals.add("balance-update"); }
        if (hasAccount)  { score += W_ACCOUNT;         matchedSignals.add("account-number"); }
        if (hasRef)      { score += W_REFERENCE;       matchedSignals.add("reference-number"); }

        score = Math.min(100, score);

        // A message is a TRANSACTION only when BOTH a currency amount AND an
        // action verb are present (score ≥ 50 gives cushion for edge cases).
        if (hasCurrency && hasAction && score >= 50) {
            String reason = "Matched: " + String.join(", ", matchedSignals);
            return SmsClassificationResult.builder()
                    .isFinancial(true)
                    .messageType(MessageType.TRANSACTION)
                    .confidenceScore(score)
                    .reason(reason)
                    .build();
        }

        // ── Stage 5: Service / informational ─────────────────────────────────
        if (isService(lower)) {
            return SmsClassificationResult.builder()
                    .isFinancial(false)
                    .messageType(MessageType.SERVICE)
                    .confidenceScore(80)
                    .reason("Contains service / account-update signals without a transaction amount")
                    .build();
        }

        // ── Stage 6: Unknown ─────────────────────────────────────────────────
        String unknownReason = score > 0
                ? "Partial financial signals (" + String.join(", ", matchedSignals) + ") but missing "
                        + (!hasCurrency ? "currency amount" : "action verb")
                : "No financial signals detected";

        return SmsClassificationResult.builder()
                .isFinancial(false)
                .messageType(MessageType.UNKNOWN)
                .confidenceScore(score)
                .reason(unknownReason)
                .build();
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private boolean isOtp(String rawSms, String lower) {
        if (OTP_PATTERN.matcher(lower).find()) return true;
        // Standalone digit-only code that dominates a short message (< 120 chars)
        if (rawSms.length() < 120 && OTP_CODE_PATTERN.matcher(rawSms).find()) {
            // Must NOT have currency + action to avoid mis-classifying "Rs 500 debited. OTP..."
            boolean hasCurrency = CURRENCY_PATTERN.matcher(rawSms).find();
            boolean hasAction   = DEBIT_ACTION_PATTERN.matcher(lower).find()
                               || CREDIT_ACTION_PATTERN.matcher(lower).find();
            return !(hasCurrency && hasAction);
        }
        return false;
    }

    private boolean isPromotional(String lower) {
        return PROMO_PATTERN.matcher(lower).find();
    }

    private boolean isService(String lower) {
        return SERVICE_PATTERN.matcher(lower).find();
    }
}
