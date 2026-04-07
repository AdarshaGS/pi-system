package com.pisystem.modules.sms.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.DuplicateDetectionResult;
import com.pisystem.modules.sms.data.DuplicateDetectionResult.MatchTier;
import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.SMSTransaction.ParseStatus;
import com.pisystem.modules.sms.repo.SMSTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rule-based duplicate detection engine for SMS-parsed transactions.
 *
 * <h3>Evaluation order (first match wins)</h3>
 * <ol>
 *   <li><b>Reference-ID fast path</b> – if the candidate carries a referenceNumber,
 *       do a direct DB lookup (O(1)); if a SUCCESS row exists → STRONG duplicate.</li>
 *   <li><b>STRONG match</b> – amount + type + account + referenceId all agree.</li>
 *   <li><b>MEDIUM match</b> – amount + merchant + account agree AND timestamps
 *       are ≤ {@value #MEDIUM_MATCH_MINUTES} minutes apart.</li>
 *   <li><b>WEAK match</b>   – same amount AND timestamps are ≤ {@value #WEAK_MATCH_MINUTES}
 *       minute(s) apart.</li>
 * </ol>
 *
 * <p>FAILED and PENDING transactions in the existing list are skipped.
 * When the candidate itself has no amount, detection returns NONE immediately.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DuplicateDetectionServiceImpl implements DuplicateDetectionService {

    // ── Configurable time windows ─────────────────────────────────────────────
    private static final long MEDIUM_MATCH_MINUTES = 2L;
    private static final long WEAK_MATCH_MINUTES   = 1L;

    /** How many days either side of the candidate date to load from the DB. */
    private static final int DB_WINDOW_DAYS = 1;

    // ── Confidence band boundaries ────────────────────────────────────────────
    private static final int STRONG_BASE  = 97;
    private static final int MEDIUM_BASE  = 85;
    private static final int WEAK_BASE    = 65;

    // ── Bonus points ──────────────────────────────────────────────────────────
    /** +bonus when reference IDs are identical (already implied by STRONG, used
     *  for fine-tuning score inside the loop). */
    private static final int BONUS_REF_MATCH   = 3;
    /** +bonus when accounts also match in MEDIUM tier. */
    private static final int BONUS_ACCOUNT     = 3;
    /** +bonus when timestamps are within 30 seconds. */
    private static final int BONUS_NEAR_INSTANT = 5;

    private final SMSTransactionRepository repository;

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public DuplicateDetectionResult detect(SMSTransaction candidate, List<SMSTransaction> existingTransactions) {
        if (candidate == null) {
            return noMatch("Candidate transaction is null");
        }

        // Guard: no amount → cannot determine duplicate
        if (candidate.getAmount() == null) {
            return noMatch("Candidate has no amount — cannot check for duplicate");
        }

        // ── Reference-ID fast path ─────────────────────────────────────────────
        // If the candidate has a referenceId, scan only the reference-matching rows
        // from the provided list first (avoids iterating the full window).
        if (hasRef(candidate)) {
            DuplicateDetectionResult refResult = checkReferenceMatch(candidate, existingTransactions);
            if (refResult.isDuplicate()) return refResult;
        }

        // ── Full scan of existing SUCCESS transactions ─────────────────────────
        DuplicateDetectionResult best = noMatch("No matching transaction found");

        for (SMSTransaction existing : existingTransactions) {
            // Rule: skip FAILED and PENDING rows unconditionally
            if (shouldSkip(existing)) continue;

            DuplicateDetectionResult result = evaluatePair(candidate, existing);
            if (result.isDuplicate() && result.getConfidenceScore() > best.getConfidenceScore()) {
                best = result;
                // Short-circuit on perfect STRONG match (nothing can beat 100)
                if (best.getConfidenceScore() >= 100) break;
            }
        }

        log.debug("Duplicate check: amount={} type={} → isDuplicate={} tier={} confidence={}",
                candidate.getAmount(), candidate.getTransactionType(),
                best.isDuplicate(), best.getMatchTier(), best.getConfidenceScore());

        return best;
    }

    @Override
    public DuplicateDetectionResult detectWithDbLookup(SMSTransaction candidate) {
        if (candidate == null || candidate.getUserId() == null) {
            return noMatch("Candidate or userId is null");
        }

        // ── Reference-ID fast path via DB ──────────────────────────────────────
        if (hasRef(candidate)) {
            List<SMSTransaction> refMatches = repository.findByUserIdAndReferenceNumber(
                    candidate.getUserId(), candidate.getReferenceNumber());
            if (!refMatches.isEmpty()) {
                DuplicateDetectionResult refResult = checkReferenceMatch(candidate, refMatches);
                if (refResult.isDuplicate()) return refResult;
            }
        }

        // ── Date-window lookup ─────────────────────────────────────────────────
        LocalDate anchor = candidate.getTransactionDate() != null
                ? candidate.getTransactionDate()
                : LocalDate.now();
        List<SMSTransaction> window = repository.findSuccessTransactionsInWindow(
                candidate.getUserId(),
                anchor.minusDays(DB_WINDOW_DAYS),
                anchor.plusDays(DB_WINDOW_DAYS));

        return detect(candidate, window);
    }

    // =========================================================================
    // Core pair evaluation
    // =========================================================================

    /**
     * Evaluate a single (candidate, existing) pair and return the best-matching
     * result tier. Returns {@code noMatch} if neither STRONG / MEDIUM / WEAK
     * conditions are satisfied.
     */
    private DuplicateDetectionResult evaluatePair(SMSTransaction candidate, SMSTransaction existing) {

        // Amount must match for any tier
        if (!amountsEqual(candidate.getAmount(), existing.getAmount())) {
            return noMatch("Amount differs");
        }

        // Transaction type must match (debit ≠ credit)
        if (!typesMatch(candidate, existing)) {
            return noMatch("Transaction type differs (DEBIT vs CREDIT)");
        }

        long minutesApart = minutesBetween(candidate, existing);

        // ── STRONG match ──────────────────────────────────────────────────────
        if (isStrongMatch(candidate, existing)) {
            int score = STRONG_BASE;
            // Fine-tune: if timestamps are also very close, max out to 100
            if (minutesApart == 0) score = 100;
            return DuplicateDetectionResult.builder()
                    .isDuplicate(true)
                    .matchedTransactionId(existing.getId())
                    .confidenceScore(Math.min(100, score))
                    .reason(buildStrongReason(candidate, existing, minutesApart))
                    .matchTier(MatchTier.STRONG)
                    .build();
        }

        // ── MEDIUM match ──────────────────────────────────────────────────────
        if (isMediumMatch(candidate, existing, minutesApart)) {
            int score = MEDIUM_BASE;
            if (minutesApart == 0) score += BONUS_NEAR_INSTANT;
            score += accountsMatch(candidate, existing) ? BONUS_ACCOUNT : 0;
            return DuplicateDetectionResult.builder()
                    .isDuplicate(true)
                    .matchedTransactionId(existing.getId())
                    .confidenceScore(Math.min(100, score))
                    .reason(buildMediumReason(candidate, existing, minutesApart))
                    .matchTier(MatchTier.MEDIUM)
                    .build();
        }

        // ── WEAK match ────────────────────────────────────────────────────────
        if (isWeakMatch(minutesApart)) {
            int score = WEAK_BASE;
            if (minutesApart == 0) score += BONUS_NEAR_INSTANT;
            return DuplicateDetectionResult.builder()
                    .isDuplicate(true)
                    .matchedTransactionId(existing.getId())
                    .confidenceScore(Math.min(100, score))
                    .reason(buildWeakReason(candidate, existing, minutesApart))
                    .matchTier(MatchTier.WEAK)
                    .build();
        }

        return noMatch("Amount matches but time gap > " + WEAK_MATCH_MINUTES + " min or merchants differ");
    }

    // =========================================================================
    // Reference-ID fast path
    // =========================================================================

    private DuplicateDetectionResult checkReferenceMatch(
            SMSTransaction candidate, List<SMSTransaction> pool) {
        for (SMSTransaction existing : pool) {
            if (shouldSkip(existing)) continue;
            if (!hasRef(existing)) continue;
            if (!candidate.getReferenceNumber().equalsIgnoreCase(existing.getReferenceNumber())) continue;
            if (!amountsEqual(candidate.getAmount(), existing.getAmount())) continue;
            if (!typesMatch(candidate, existing)) continue;

            int score = STRONG_BASE + BONUS_REF_MATCH;
            return DuplicateDetectionResult.builder()
                    .isDuplicate(true)
                    .matchedTransactionId(existing.getId())
                    .confidenceScore(Math.min(100, score))
                    .reason("Exact referenceId match: " + candidate.getReferenceNumber()
                            + " (amount=" + candidate.getAmount() + ", type=" + candidate.getTransactionType() + ")")
                    .matchTier(MatchTier.STRONG)
                    .build();
        }
        return noMatch("No reference-ID match found");
    }

    // =========================================================================
    // Match condition helpers
    // =========================================================================

    /** STRONG: amount + type + account all match AND referenceId matches (if present). */
    private boolean isStrongMatch(SMSTransaction candidate, SMSTransaction existing) {
        if (!accountsMatch(candidate, existing)) return false;

        // If both have a reference number, they must agree
        if (hasRef(candidate) && hasRef(existing)) {
            return candidate.getReferenceNumber().equalsIgnoreCase(existing.getReferenceNumber());
        }

        // If neither has a reference number, a same-account same-amount same-type
        // match with a very tight time window counts as STRONG too
        if (!hasRef(candidate) && !hasRef(existing)) {
            return minutesBetween(candidate, existing) == 0;
        }

        // One side has a ref and the other doesn't → not a reliable STRONG match
        return false;
    }

    /** MEDIUM: amount + merchant match AND timestamps ≤ MEDIUM_MATCH_MINUTES. */
    private boolean isMediumMatch(SMSTransaction candidate, SMSTransaction existing, long minutesApart) {
        if (minutesApart > MEDIUM_MATCH_MINUTES) return false;
        return merchantsMatch(candidate, existing);
    }

    /** WEAK: same amount, timestamps ≤ WEAK_MATCH_MINUTES. */
    private boolean isWeakMatch(long minutesApart) {
        return minutesApart <= WEAK_MATCH_MINUTES;
    }

    // =========================================================================
    // Field-level comparison utilities
    // =========================================================================

    private boolean amountsEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    private boolean typesMatch(SMSTransaction a, SMSTransaction b) {
        if (a.getTransactionType() == null || b.getTransactionType() == null) return false;
        return a.getTransactionType() == b.getTransactionType();
    }

    private boolean accountsMatch(SMSTransaction a, SMSTransaction b) {
        String accA = resolveAccount(a);
        String accB = resolveAccount(b);
        if (accA == null || accB == null) return false;
        return accA.equalsIgnoreCase(accB);
    }

    /**
     * Merchant matching: compares the last 4+ meaningful characters case-insensitively.
     * Handles null, blank, and very short merchant strings gracefully.
     */
    private boolean merchantsMatch(SMSTransaction a, SMSTransaction b) {
        String mA = normalize(a.getMerchant());
        String mB = normalize(b.getMerchant());
        if (mA == null || mB == null) return false;
        // Exact match after normalization
        if (mA.equals(mB)) return true;
        // Substring containment for partial merchant names
        return mA.contains(mB) || mB.contains(mA);
    }

    /**
     * Returns the absolute difference in minutes between the two transactions'
     * combined date+time. Uses only date portion when time is absent (max gap = 0
     * for same day or a large number for different days, effectively disqualifying
     * cross-day pairs for the WEAK/MEDIUM tiers).
     */
    private long minutesBetween(SMSTransaction a, SMSTransaction b) {
        LocalDateTime dtA = toDateTime(a);
        LocalDateTime dtB = toDateTime(b);
        if (dtA == null || dtB == null) {
            // No time info: same date → treat as 0, different date → treat as large gap
            if (a.getTransactionDate() != null && b.getTransactionDate() != null) {
                return ChronoUnit.DAYS.between(a.getTransactionDate(), b.getTransactionDate()) * 24L * 60L;
            }
            return Long.MAX_VALUE;
        }
        return Math.abs(ChronoUnit.MINUTES.between(dtA, dtB));
    }

    private LocalDateTime toDateTime(SMSTransaction tx) {
        if (tx.getTransactionDate() == null) return null;
        LocalTime t = tx.getTransactionTime() != null ? tx.getTransactionTime() : LocalTime.MIDNIGHT;
        return LocalDateTime.of(tx.getTransactionDate(), t);
    }

    private boolean hasRef(SMSTransaction tx) {
        return tx.getReferenceNumber() != null && !tx.getReferenceNumber().isBlank();
    }

    /** Use accountNumber, fall back to cardNumber. */
    private String resolveAccount(SMSTransaction tx) {
        if (tx.getAccountNumber() != null && !tx.getAccountNumber().isBlank()) return tx.getAccountNumber();
        if (tx.getCardNumber() != null && !tx.getCardNumber().isBlank()) return tx.getCardNumber();
        return null;
    }

    private boolean shouldSkip(SMSTransaction tx) {
        return tx.getParseStatus() == ParseStatus.FAILED
                || "PENDING".equalsIgnoreCase(tx.getCategory());
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    // =========================================================================
    // Reason builders
    // =========================================================================

    private String buildStrongReason(SMSTransaction c, SMSTransaction e, long minutes) {
        StringBuilder sb = new StringBuilder("Strong match:");
        sb.append(" amount=").append(c.getAmount());
        sb.append(", type=").append(c.getTransactionType());
        if (hasRef(c)) sb.append(", referenceId=").append(c.getReferenceNumber());
        if (accountsMatch(c, e)) sb.append(", account=").append(resolveAccount(c));
        sb.append(", ").append(minutes).append(" min apart");
        return sb.toString();
    }

    private String buildMediumReason(SMSTransaction c, SMSTransaction e, long minutes) {
        return "Medium match: amount=" + c.getAmount()
                + ", merchant=" + c.getMerchant()
                + ", " + minutes + " min apart (≤ " + MEDIUM_MATCH_MINUTES + " min threshold)";
    }

    private String buildWeakReason(SMSTransaction c, SMSTransaction e, long minutes) {
        return "Weak match: same amount (" + c.getAmount()
                + "), " + minutes + " min apart (≤ " + WEAK_MATCH_MINUTES
                + " min) — verify manually";
    }

    private DuplicateDetectionResult noMatch(String reason) {
        return DuplicateDetectionResult.builder()
                .isDuplicate(false)
                .matchedTransactionId(null)
                .confidenceScore(0)
                .reason(reason)
                .matchTier(MatchTier.NONE)
                .build();
    }
}
