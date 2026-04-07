package com.pisystem.modules.sms.service;

import com.pisystem.modules.sms.data.DuplicateDetectionResult;
import com.pisystem.modules.sms.data.SMSTransaction;

import java.util.List;

/**
 * Contract for detecting duplicate SMS-parsed transactions.
 *
 * <h3>Detection tiers (highest to lowest priority)</h3>
 * <ol>
 *   <li><b>STRONG</b> – same amount + type + account + referenceId → 95–100</li>
 *   <li><b>MEDIUM</b> – same amount + merchant + account, ≤ 2 min apart → 80–94</li>
 *   <li><b>WEAK</b>   – same amount, ≤ 1 min apart → 60–79</li>
 *   <li><b>NONE</b>   – no duplicate detected → isDuplicate = false</li>
 * </ol>
 *
 * <p>FAILED and PENDING transactions in the existing list are always ignored.
 */
public interface DuplicateDetectionService {

    /**
     * Compare {@code candidate} against {@code existingTransactions} and decide
     * whether it is a duplicate.
     *
     * @param candidate            the newly-parsed transaction (not yet persisted)
     * @param existingTransactions transactions already stored for the same user,
     *                             pre-filtered to a narrow date window for performance
     * @return a {@link DuplicateDetectionResult} — never {@code null}
     */
    DuplicateDetectionResult detect(SMSTransaction candidate, List<SMSTransaction> existingTransactions);

    /**
     * Convenience method: loads the relevant window of existing transactions from
     * the database and delegates to {@link #detect(SMSTransaction, List)}.
     *
     * @param candidate the newly-parsed transaction (not yet persisted)
     * @return a {@link DuplicateDetectionResult} — never {@code null}
     */
    DuplicateDetectionResult detectWithDbLookup(SMSTransaction candidate);
}
