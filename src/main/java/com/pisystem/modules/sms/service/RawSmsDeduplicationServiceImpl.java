package com.pisystem.modules.sms.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.RawDedupRequest;
import com.pisystem.modules.sms.data.RawDedupRequest.RawSmsMessage;
import com.pisystem.modules.sms.data.RawSmsDuplicateResult;
import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.repo.SMSTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless, rule-based raw-SMS deduplication engine.
 *
 * <h3>Evaluation order for each message (first match wins)</h3>
 * <ol>
 *   <li>In-batch exact match   → 100 %</li>
 *   <li>In-batch near match    → 90 %  (similarity ≥ 90 % AND Δt &lt; 30 s)</li>
 *   <li>DB exact match         → 100 %</li>
 *   <li>DB near match          → 90 %  (same sender, similar body, Δt &lt; 30 s)</li>
 *   <li>No match               → not duplicate</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RawSmsDeduplicationServiceImpl implements RawSmsDeduplicationService {

    // ── Rule thresholds ───────────────────────────────────────────────────────

    /** Minimum normalized Levenshtein similarity for Rule 2 (near match). */
    private static final double SIMILARITY_THRESHOLD = 0.90;

    /** Maximum timestamp gap in milliseconds for Rule 2 (near match). */
    private static final long NEAR_MATCH_WINDOW_MS = 30_000L;

    private final SMSTransactionRepository repository;

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public List<RawSmsDuplicateResult> checkBatch(RawDedupRequest request) {
        List<RawSmsMessage> messages = request.getMessages();
        List<RawSmsDuplicateResult> results = new ArrayList<>(messages.size());

        for (int i = 0; i < messages.size(); i++) {
            RawSmsMessage current = messages.get(i);
            // Predecessors = messages already evaluated in this batch (indices 0..i-1)
            List<RawSmsMessage> predecessors = messages.subList(0, i);
            results.add(evaluate(current, predecessors, request.getUserId()));
        }

        return results;
    }

    // =========================================================================
    // Core evaluation logic
    // =========================================================================

    private RawSmsDuplicateResult evaluate(RawSmsMessage current,
                                           List<RawSmsMessage> predecessors,
                                           Long userId) {

        // ── Step 1: in-batch checks (no DB I/O) ───────────────────────────────
        for (RawSmsMessage prev : predecessors) {
            if (!sameSender(current.getSender(), prev.getSender())) {
                continue;
            }

            if (isExactBody(current.getBody(), prev.getBody())) {
                return match(current.getMessageId(), prev.getMessageId(), 100,
                        "Exact duplicate in batch (same sender and body)");
            }

            if (isNearBody(current.getBody(), prev.getBody())
                    && withinWindow(current.getTimestamp(), prev.getTimestamp())) {
                return match(current.getMessageId(), prev.getMessageId(), 90,
                        "Near-duplicate in batch (same sender, body similarity ≥ 90 %, within 30 s)");
            }
        }

        // ── Step 2: DB checks ─────────────────────────────────────────────────
        if (userId != null && current.getSender() != null) {
            RawSmsDuplicateResult dbResult = checkDb(current, userId);
            if (dbResult != null) {
                return dbResult;
            }
        }

        // ── Step 3: not a duplicate ───────────────────────────────────────────
        log.debug("raw-dedup: messageId={} → not duplicate", current.getMessageId());
        return noMatch(current.getMessageId());
    }

    /**
     * Database deduplication for a single message.
     *
     * <p>Two checks are performed in order:
     * <ol>
     *   <li>Exact body + sender lookup (O(1) index query).</li>
     *   <li>Time-window lookup restricted to the same sender, followed by an
     *       in-memory similarity comparison of all candidates.</li>
     * </ol>
     *
     * @return a matching result, or {@code null} when no DB duplicate is found
     */
    private RawSmsDuplicateResult checkDb(RawSmsMessage msg, Long userId) {
        String body   = msg.getBody();
        String sender = msg.getSender();

        // Rule 1 – DB exact match (same sender + same body)
        List<SMSTransaction> exactMatches =
                repository.findByUserIdAndSenderAndOriginalMessage(userId, sender, body);
        if (!exactMatches.isEmpty()) {
            long dbId = exactMatches.get(0).getId();
            log.debug("raw-dedup: messageId={} → exact DB match id={}", msg.getMessageId(), dbId);
            return match(msg.getMessageId(), String.valueOf(dbId), 100,
                    "Exact duplicate in database (same sender and body, db id=" + dbId + ")");
        }

        // Rule 2 – DB near match (same sender + similar body + within 30 s)
        if (msg.getTimestamp() != null) {
            LocalDateTime windowStart = toLocalDateTime(msg.getTimestamp() - NEAR_MATCH_WINDOW_MS);
            LocalDateTime windowEnd   = toLocalDateTime(msg.getTimestamp() + NEAR_MATCH_WINDOW_MS);

            List<SMSTransaction> nearby =
                    repository.findBySenderInTimeWindow(userId, sender, windowStart, windowEnd);

            for (SMSTransaction candidate : nearby) {
                if (isNearBody(body, candidate.getOriginalMessage())) {
                    long dbId = candidate.getId();
                    log.debug("raw-dedup: messageId={} → near DB match id={}", msg.getMessageId(), dbId);
                    return match(msg.getMessageId(), String.valueOf(dbId), 90,
                            "Near-duplicate in database (same sender, body similarity ≥ 90 %, within 30 s, db id=" + dbId + ")");
                }
            }
        }

        return null;
    }

    // =========================================================================
    // Matching predicates
    // =========================================================================

    /** True when both senders are equal (case-insensitive, trimmed). */
    private static boolean sameSender(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    /** True when both bodies are identical after trimming. */
    private static boolean isExactBody(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equals(b.trim());
    }

    /** True when normalized body similarity is at or above {@link #SIMILARITY_THRESHOLD}. */
    private static boolean isNearBody(String a, String b) {
        if (a == null || b == null) return false;
        return similarity(normalize(a), normalize(b)) >= SIMILARITY_THRESHOLD;
    }

    /** True when both timestamps are non-null and differ by less than 30 seconds. */
    private static boolean withinWindow(Long ts1, Long ts2) {
        if (ts1 == null || ts2 == null) return false;
        return Math.abs(ts1 - ts2) < NEAR_MATCH_WINDOW_MS;
    }

    // =========================================================================
    // Text similarity — normalized Levenshtein, O(n) space DP
    // =========================================================================

    /** Lowercase, trim, collapse all whitespace runs to a single space. */
    private static String normalize(String s) {
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * Returns a normalized Levenshtein similarity score in [0.0, 1.0].
     *
     * <p>Defined as {@code 1 - editDistance(a, b) / max(|a|, |b|)}.
     * Identical strings always return {@code 1.0}; empty strings return
     * {@code 1.0} regardless of the other string.
     */
    static double similarity(String a, String b) {
        if (a.equals(b)) return 1.0;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - (double) levenshtein(a, b) / maxLen;
    }

    /**
     * Classic Levenshtein edit distance using a single rolling row of DP state.
     * Time O(m·n), space O(n) — safe for typical SMS lengths (≤ 160 chars).
     */
    private static int levenshtein(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[] dp = new int[n + 1];

        for (int j = 0; j <= n; j++) dp[j] = j;

        for (int i = 1; i <= m; i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                dp[j] = (a.charAt(i - 1) == b.charAt(j - 1))
                        ? prev
                        : 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                prev = temp;
            }
        }
        return dp[n];
    }

    // =========================================================================
    // Result builders
    // =========================================================================

    private static RawSmsDuplicateResult match(String msgId, String duplicateOf,
                                               int score, String reason) {
        return RawSmsDuplicateResult.builder()
                .messageId(msgId)
                .isDuplicate(true)
                .duplicateOf(duplicateOf)
                .confidenceScore(score)
                .reason(reason)
                .build();
    }

    private static RawSmsDuplicateResult noMatch(String msgId) {
        return RawSmsDuplicateResult.builder()
                .messageId(msgId)
                .isDuplicate(false)
                .duplicateOf(null)
                .confidenceScore(0)
                .reason("No duplicate found")
                .build();
    }

    // =========================================================================
    // Timestamp utility
    // =========================================================================

    private static LocalDateTime toLocalDateTime(long epochMs) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault());
    }
}
