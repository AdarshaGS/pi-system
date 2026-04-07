package com.pisystem.modules.sms.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.SMSTransaction.TransactionType;
import com.pisystem.modules.sms.data.TransferDetectionResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Rule-based, deterministic transfer detection engine.
 *
 * <h3>Rules (applied in priority order — first match wins)</h3>
 *
 * <ol>
 *   <li><b>Own-account rule (confidence 95)</b><br>
 *       Both {@code fromAccount} and {@code toAccount} are present in the
 *       parsed transaction <em>and</em> both are found in the user's known
 *       account set. Classic bank transfer between own accounts.</li>
 *
 *   <li><b>Paired-transaction rule (confidence 90)</b><br>
 *       The candidate is a DEBIT, and within the {@code recentHistory} list
 *       there is a CREDIT from the <em>same user</em> with:
 *       <ul>
 *         <li>identical amount (exact match)</li>
 *         <li>transaction time within ± {@value #PAIRED_WINDOW_MINUTES} minutes</li>
 *       </ul>
 *       Indicates money deducted from one account and credited to another.</li>
 *
 *   <li><b>UPI self-transfer rule (confidence 85)</b><br>
 *       The merchant field or UPI ID of the candidate matches a personal UPI
 *       handle supplied in {@code userUpiHandles}. e.g. {@code adarsh@ybl}
 *       sending money to {@code adarsh@okaxis}.</li>
 *
 *   <li><b>Partial own-account rule (confidence 70)</b><br>
 *       Only <em>one</em> of fromAccount / toAccount is in the user's account
 *       set (the other may be a new / unregistered account).</li>
 * </ol>
 *
 * <h3>Non-transfer</h3>
 * If none of the above rules match, returns {@code isTransfer=false} with
 * confidence 0 so downstream classifiers can proceed normally.
 */
@Service
@Slf4j
public class TransferDetectionServiceImpl implements TransferDetectionService {

    /** Maximum time difference (minutes) for the paired-transaction rule. */
    static final long PAIRED_WINDOW_MINUTES = 5;

    /** Matches VPA-style UPI IDs: word@word */
    private static final Pattern UPI_ID_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z]+",
            Pattern.CASE_INSENSITIVE);

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public TransferDetectionResult detect(SMSTransaction candidate, Set<String> userAccountNos) {
        return detect(candidate, userAccountNos, Set.of(), List.of());
    }

    @Override
    public TransferDetectionResult detect(SMSTransaction candidate,
                                          Set<String> userAccountNos,
                                          Set<String> userUpiHandles,
                                          List<SMSTransaction> recentHistory) {
        if (candidate == null) {
            return noTransfer("Candidate transaction is null");
        }

        String from = candidate.getFromAccount();
        String to   = candidate.getToAccount();

        // ── Rule 1: Both accounts present and both owned by user ──────────────
        boolean fromOwned = from != null && userAccountNos.contains(from);
        boolean toOwned   = to   != null && userAccountNos.contains(to);

        if (fromOwned && toOwned) {
            return TransferDetectionResult.builder()
                    .isTransfer(true)
                    .fromAccount(from)
                    .toAccount(to)
                    .confidenceScore(95)
                    .reason("Both fromAccount (" + from + ") and toAccount (" + to +
                            ") belong to the user — own-account transfer")
                    .build();
        }

        // ── Rule 2: Paired DEBIT/CREDIT within 5 minutes ─────────────────────
        if (!recentHistory.isEmpty() && candidate.getTransactionType() == TransactionType.DEBIT) {
            for (SMSTransaction peer : recentHistory) {
                if (peer.getId() != null && peer.getId().equals(candidate.getId())) continue;
                if (peer.getTransactionType() != TransactionType.CREDIT) continue;
                if (peer.getAmount() == null || candidate.getAmount() == null) continue;
                if (peer.getAmount().compareTo(candidate.getAmount()) != 0) continue;

                long minutesDiff = minutesBetween(candidate, peer);
                if (minutesDiff <= PAIRED_WINDOW_MINUTES) {
                    String peerAccount = peer.getAccountNumber() != null
                            ? peer.getAccountNumber() : "unknown";
                    return TransferDetectionResult.builder()
                            .isTransfer(true)
                            .fromAccount(candidate.getAccountNumber())
                            .toAccount(peerAccount)
                            .confidenceScore(90)
                            .reason("Paired DEBIT/CREDIT: same amount (" + candidate.getAmount() +
                                    "), " + minutesDiff + " minute(s) apart — matched to tx id " + peer.getId())
                            .build();
                }
            }
        }

        // ── Rule 3: UPI self-transfer (personal handle match) ─────────────────
        if (!userUpiHandles.isEmpty()) {
            String merchantLower = candidate.getMerchant() != null
                    ? candidate.getMerchant().toLowerCase() : "";
            String upiIdLower = candidate.getUpiId() != null
                    ? candidate.getUpiId().toLowerCase() : "";

            for (String handle : userUpiHandles) {
                String h = handle.toLowerCase();
                if (merchantLower.contains(h) || upiIdLower.contains(h)) {
                    return TransferDetectionResult.builder()
                            .isTransfer(true)
                            .fromAccount(candidate.getAccountNumber())
                            .toAccount(null)
                            .confidenceScore(85)
                            .reason("UPI self-transfer: merchant/UPI ID matches user handle '" + handle + "'")
                            .build();
                }
            }

            // Generic VPA-in-merchant pattern (xyz@ybl, @okaxis) against UPI_ID_PATTERN
            if (UPI_ID_PATTERN.matcher(merchantLower).find()) {
                return TransferDetectionResult.builder()
                        .isTransfer(true)
                        .fromAccount(candidate.getAccountNumber())
                        .toAccount(null)
                        .confidenceScore(75)
                        .reason("Merchant contains a UPI VPA pattern and user has registered UPI handles — likely self-transfer")
                        .build();
            }
        }

        // ── Rule 4: Only one account owned — partial match ────────────────────
        if (fromOwned || toOwned) {
            return TransferDetectionResult.builder()
                    .isTransfer(true)
                    .fromAccount(from)
                    .toAccount(to)
                    .confidenceScore(70)
                    .reason("Partial own-account match: " +
                            (fromOwned ? "fromAccount " + from : "toAccount " + to) +
                            " belongs to user — possible transfer to/from unregistered account")
                    .build();
        }

        // ── No transfer detected ──────────────────────────────────────────────
        return noTransfer("No transfer signals found");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private long minutesBetween(SMSTransaction a, SMSTransaction b) {
        if (a.getTransactionDate() == null || b.getTransactionDate() == null) return Long.MAX_VALUE;

        LocalTime timeA = a.getTransactionTime() != null ? a.getTransactionTime() : LocalTime.NOON;
        LocalTime timeB = b.getTransactionTime() != null ? b.getTransactionTime() : LocalTime.NOON;

        LocalDateTime dtA = a.getTransactionDate().atTime(timeA);
        LocalDateTime dtB = b.getTransactionDate().atTime(timeB);

        return Math.abs(ChronoUnit.MINUTES.between(dtA, dtB));
    }

    private TransferDetectionResult noTransfer(String reason) {
        return TransferDetectionResult.builder()
                .isTransfer(false)
                .fromAccount(null)
                .toAccount(null)
                .confidenceScore(0)
                .reason(reason)
                .build();
    }
}
