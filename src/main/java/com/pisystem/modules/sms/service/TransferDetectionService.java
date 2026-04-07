package com.pisystem.modules.sms.service;

import java.util.List;
import java.util.Set;

import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.TransferDetectionResult;

/**
 * Detects whether a parsed SMS transaction is a fund transfer between accounts.
 *
 * <p>Three rules are evaluated in priority order:
 * <ol>
 *   <li><b>Own-account rule</b> — both fromAccount and toAccount are present
 *       and both belong to the user's known account set.</li>
 *   <li><b>Paired-transaction rule</b> — a matching CREDIT (same amount, ≤ 5 min
 *       apart) exists in the user's recent transaction history.</li>
 *   <li><b>UPI self-transfer rule</b> — the merchant / UPI ID matches a known
 *       personal UPI handle belonging to the user.</li>
 * </ol>
 */
public interface TransferDetectionService {

    /**
     * Detect with only the candidate transaction and the user's known accounts.
     * Rules 1 and 3 are applied; rule 2 requires a history list.
     */
    TransferDetectionResult detect(SMSTransaction candidate, Set<String> userAccountNos);

    /**
     * Full detection including paired-transaction lookback.
     *
     * @param candidate       the transaction being evaluated
     * @param userAccountNos  masked account numbers owned by the user
     * @param userUpiHandles  personal UPI IDs of the user (e.g. {@code "adarsh@ybl"})
     * @param recentHistory   candidate's nearby transactions (typically ±5 min window)
     */
    TransferDetectionResult detect(SMSTransaction candidate,
                                   Set<String> userAccountNos,
                                   Set<String> userUpiHandles,
                                   List<SMSTransaction> recentHistory);
}
