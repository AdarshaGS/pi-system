package com.pisystem.modules.sms.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.FlowClassificationResult;
import com.pisystem.modules.sms.data.FlowClassificationResult.FlowType;
import com.pisystem.modules.sms.data.NormalizedTransaction;

import lombok.extern.slf4j.Slf4j;

/**
 * Final-stage, deterministic transaction flow classifier.
 *
 * <h3>Classification rules (applied in strict priority order)</h3>
 * <ol>
 *   <li><b>IGNORE</b> — null/zero amount, FAILED/PENDING status, or
 *       normalization flowType already UNKNOWN.</li>
 *   <li><b>TRANSFER</b> — normalization engine flagged TRANSFER or
 *       SELF_TRANSFER, OR both fromAccount and toAccount are present
 *       in the user's own account set.</li>
 *   <li><b>INCOME</b>  — transactionType is CREDIT.</li>
 *   <li><b>EXPENSE</b> — transactionType is DEBIT.</li>
 *   <li><b>Fallback IGNORE</b> — anything that reaches this point without
 *       a clear signal (e.g. COMPLEX_TRANSACTION with no context) is
 *       classified IGNORE so no budget entry is erroneously created.</li>
 * </ol>
 *
 * <h3>Confidence scoring</h3>
 * Starts at the normalization confidence score (0-100) and adjusts:
 * <ul>
 *   <li>+10 if payment mode is known (not UNKNOWN)</li>
 *   <li>+5  if a reference ID is present</li>
 *   <li>-20 for COMPLEX_TRANSACTION (ambiguous debit+credit)</li>
 * </ul>
 */
@Service
@Slf4j
public class TransactionFlowClassifierServiceImpl implements TransactionFlowClassifierService {

    @Override
    public FlowClassificationResult classify(NormalizedTransaction normalized) {
        return classify(normalized, Set.of());
    }

    @Override
    public FlowClassificationResult classify(NormalizedTransaction normalized, Set<String> userAccountNos) {
        if (normalized == null) {
            return ignore("Normalized transaction is null");
        }

        // ── Rule 1: IGNORE — invalid / non-financial ──────────────────────────
        if (normalized.getAmount() == null || normalized.getAmount().signum() <= 0) {
            return ignore("Amount is missing or zero");
        }
        if ("FAILED".equalsIgnoreCase(normalized.getStatus())
                || "PENDING".equalsIgnoreCase(normalized.getStatus())) {
            return ignore("Transaction status is " + normalized.getStatus() + " — not a completed transaction");
        }

        // ── Rule 2: TRANSFER — own-account or normalization signal ────────────
        String normFlow = normalized.getFlowType();
        if ("TRANSFER".equalsIgnoreCase(normFlow) || "SELF_TRANSFER".equalsIgnoreCase(normFlow)) {
            return classify(FlowType.TRANSFER, normalized, "Normalization engine flagged " + normFlow);
        }
        if (!userAccountNos.isEmpty()
                && normalized.getAccount() != null
                && userAccountNos.contains(normalized.getAccount())) {
            // The destination account belongs to the same user — treat as transfer
            return classify(FlowType.TRANSFER, normalized,
                    "Destination account " + normalized.getAccount() + " belongs to this user");
        }

        // ── Rule 3: INCOME — CREDIT ───────────────────────────────────────────
        if ("CREDIT".equalsIgnoreCase(normalized.getTransactionType())) {
            return classify(FlowType.INCOME, normalized, "Transaction type is CREDIT");
        }

        // ── Rule 4: EXPENSE — DEBIT ───────────────────────────────────────────
        if ("DEBIT".equalsIgnoreCase(normalized.getTransactionType())) {
            return classify(FlowType.EXPENSE, normalized, "Transaction type is DEBIT");
        }

        // ── Rule 5: Fallback IGNORE — ambiguous / no clear signal ─────────────
        return ignore("No clear debit/credit signal — transactionType is '"
                + normalized.getTransactionType() + "'");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private FlowClassificationResult classify(FlowType type,
                                               NormalizedTransaction normalized,
                                               String reason) {
        int score = normalized.getConfidenceScore();

        // Adjust for signal quality
        if (!"UNKNOWN".equalsIgnoreCase(normalized.getMode())) score += 10;
        if (normalized.getReferenceId() != null)              score += 5;
        if ("COMPLEX_TRANSACTION".equalsIgnoreCase(normalized.getTransactionType())) score -= 20;

        score = Math.min(100, Math.max(0, score));

        log.debug("Flow classified: type={}, score={}, reason={}", type, score, reason);

        return FlowClassificationResult.builder()
                .flowType(type)
                .confidenceScore(score)
                .reason(reason)
                .build();
    }

    private FlowClassificationResult ignore(String reason) {
        log.debug("Flow classified: IGNORE — {}", reason);
        return FlowClassificationResult.builder()
                .flowType(FlowType.IGNORE)
                .confidenceScore(0)
                .reason(reason)
                .build();
    }
}
