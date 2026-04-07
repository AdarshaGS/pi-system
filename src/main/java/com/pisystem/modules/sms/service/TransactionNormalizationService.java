package com.pisystem.modules.sms.service;

import com.pisystem.modules.sms.data.NormalizedTransaction;
import com.pisystem.modules.sms.data.SMSTransaction;

/**
 * Contract for normalizing a parsed {@link SMSTransaction} into a clean,
 * structured {@link NormalizedTransaction}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Merchant name normalization (strip UPI-, TO, FROM, PAYTM prefixes)</li>
 *   <li>Payment mode detection (UPI / ATM / CARD / NETBANKING)</li>
 *   <li>Transaction status inference (SUCCESS / FAILED / PENDING)</li>
 *   <li>Flow-type classification (EXPENSE / INCOME / TRANSFER / SELF_TRANSFER)</li>
 *   <li>Spend-category mapping</li>
 *   <li>0–100 confidence scoring</li>
 * </ul>
 */
public interface TransactionNormalizationService {

    /**
     * Normalize a previously-parsed {@link SMSTransaction} together with the
     * raw SMS text that produced it.
     *
     * @param transaction parsed SMS entity (must not be null)
     * @param rawSms      original SMS text (may be null; used for status / mode
     *                    detection that requires raw signal words)
     * @return a fully-populated {@link NormalizedTransaction}
     */
    NormalizedTransaction normalize(SMSTransaction transaction, String rawSms);
}
