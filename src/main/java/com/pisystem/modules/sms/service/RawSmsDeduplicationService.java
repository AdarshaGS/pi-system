package com.pisystem.modules.sms.service;

import com.pisystem.modules.sms.data.RawDedupRequest;
import com.pisystem.modules.sms.data.RawSmsDuplicateResult;

import java.util.List;

/**
 * Raw-text deduplication engine for incoming SMS messages.
 *
 * <p>Operates on the original sender + body + timestamp fields — before any
 * financial parsing — using two rules (first match wins):
 *
 * <ol>
 *   <li><b>Exact match (100 %)</b> – same sender AND identical body.</li>
 *   <li><b>Near match (90 %)</b>  – same sender AND body similarity ≥ 90 %
 *       (normalized Levenshtein) AND timestamp difference &lt; 30 seconds.</li>
 * </ol>
 *
 * <p>Each incoming message is first compared against earlier messages in the
 * same batch, then against persisted {@code SMSTransaction} records for the
 * same user. This ordering minimises unnecessary database calls.
 */
public interface RawSmsDeduplicationService {

    /**
     * Evaluate every message in {@code request} and return one result per
     * message, in the same order as {@code request.getMessages()}.
     *
     * @param request batch request containing userId and a non-empty message list
     * @return list of deduplication results, never {@code null}, same size as
     *         {@code request.getMessages()}
     */
    List<RawSmsDuplicateResult> checkBatch(RawDedupRequest request);
}
