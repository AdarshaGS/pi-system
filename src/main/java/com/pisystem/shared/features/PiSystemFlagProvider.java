package com.pisystem.shared.features;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Reason;
import dev.openfeature.sdk.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

// Convenience alias — Reason is an enum; .name() gives the required String
// so define helpers here to keep the code readable.
// e.g.  r(Reason.STATIC) → "STATIC"

/**
 * OpenFeature {@link FeatureProvider} backed by the PI System database.
 *
 * <h3>What this gives you over the raw FeatureConfigService calls</h3>
 * <ol>
 *   <li><b>Standard API</b> – code against the OpenFeature {@code Client} interface;
 *       swap this provider for LaunchDarkly / Flagsmith / Unleash tomorrow with
 *       zero application-code changes.</li>
 *   <li><b>Evaluation Details</b> – every evaluation returns a {@code reason}
 *       (STATIC / DISABLED / TARGETING_MATCH / ERROR) and a {@code variant}
 *       ("enabled" / "disabled" / "parent-module-disabled" / "tier-restricted")
 *       that the aspect logs, making flag decisions auditable.</li>
 *   <li><b>EvaluationContext</b> – callers can pass a targeting key (userId)
 *       and attributes (subscriptionTier) so the provider enforces per-user
 *       tier gating without needing a separate {@code isFeatureEnabledForUser}
 *       method.</li>
 *   <li><b>FLAG_NOT_FOUND errors</b> – unknown flag keys return a structured
 *       error instead of a silent boolean false.</li>
 * </ol>
 *
 * <h3>Subscription-tier context (optional)</h3>
 * If the caller builds an {@link EvaluationContext} with attribute
 * {@code subscriptionTier} (values: FREE / BASIC / PREMIUM / ENTERPRISE),
 * the provider enforces the flag's {@code minSubscriptionTier} automatically.
 *
 * <pre>
 *   EvaluationContext ctx = new ImmutableContext(
 *       String.valueOf(userId),
 *       Map.of("subscriptionTier", new Value("PREMIUM"))
 *   );
 *   FlagEvaluationDetails&lt;Boolean&gt; details =
 *       openFeatureClient.getBooleanDetails("TAX_CAPITAL_GAINS", false, ctx);
 * </pre>
 */
@Component
public class PiSystemFlagProvider implements FeatureProvider {

    private static final Logger log = LoggerFactory.getLogger(PiSystemFlagProvider.class);

    static final String PROVIDER_NAME = "pi-system-db-provider";

    /** Subscription tier ordering — higher index means higher access. */
    private static final Map<String, Integer> TIER_ORDER = Map.of(
            "FREE",       0,
            "BASIC",      1,
            "PREMIUM",    2,
            "ENTERPRISE", 3
    );

    @Autowired
    private FeatureConfigService featureConfigService;

    // =========================================================================
    // FeatureProvider contract
    // =========================================================================

    @Override
    public Metadata getMetadata() {
        return () -> PROVIDER_NAME;
    }

    /**
     * Core evaluation path.  Checks (in priority order):
     * <ol>
     *   <li>Flag existence</li>
     *   <li>Parent module enabled</li>
     *   <li>Subscription tier from {@link EvaluationContext}</li>
     *   <li>Flag's own enabled state</li>
     * </ol>
     */
    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key,
                                                             Boolean defaultValue,
                                                             EvaluationContext ctx) {
        // ── 1. Flag must exist in the enum ────────────────────────────────────
        FeatureFlag flag;
        try {
            flag = FeatureFlag.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("OpenFeature: unknown flag key '{}'", key);
            return ProviderEvaluation.<Boolean>builder()
                    .value(defaultValue)
                    .reason(Reason.ERROR.name())
                    .errorCode(ErrorCode.FLAG_NOT_FOUND)
                    .errorMessage("Feature flag '" + key + "' is not defined in FeatureFlag enum")
                    .build();
        }

        // ── 2. Parent module must be enabled ──────────────────────────────────
        FeatureFlag parent = flag.getParentFlag();
        if (parent != null && !featureConfigService.isFeatureEnabled(parent)) {
            return ProviderEvaluation.<Boolean>builder()
                    .value(false)
                    .reason(Reason.TARGETING_MATCH.name())
                    .variant("parent-module-disabled")
                    .build();
        }

        // ── 3. Subscription tier gate (context-driven) ────────────────────────
        Optional<FeatureConfig> configOpt = featureConfigService.getFeatureConfig(flag);

        if (ctx != null && configOpt.isPresent()) {
            Value tierValue = ctx.getValue("subscriptionTier");
            String minTier = configOpt.get().getMinSubscriptionTier();

            if (tierValue != null && minTier != null
                    && !meetsTierRequirement(tierValue.asString(), minTier)) {
                log.debug("OpenFeature: flag '{}' denied — user tier '{}' < required '{}'",
                        key, tierValue.asString(), minTier);
                return ProviderEvaluation.<Boolean>builder()
                        .value(false)
                        .reason(Reason.TARGETING_MATCH.name())
                        .variant("tier-restricted")
                        .build();
            }
        }

        // ── 4. Flag's own enabled state ───────────────────────────────────────
        boolean enabled = configOpt.map(FeatureConfig::getEnabled).orElse(true);
        String resolvedReason = enabled ? Reason.STATIC.name() : Reason.DISABLED.name();
        String variant = enabled ? "enabled" : "disabled";

        return ProviderEvaluation.<Boolean>builder()
                .value(enabled)
                .reason(resolvedReason)
                .variant(variant)
                .build();
    }

    /**
     * String evaluation.
     *
     * <p>Supports a {@code "FLAG_KEY.minTier"} virtual key that returns the
     * flag's configured minimum subscription tier — useful for the frontend to
     * show upgrade prompts without a separate endpoint.
     *
     * <pre>
     *   // example
     *   String minTier = client.getStringValue("TAX_CAPITAL_GAINS.minTier", "FREE");
     * </pre>
     */
    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key,
                                                           String defaultValue,
                                                           EvaluationContext ctx) {
        if (key.endsWith(".minTier")) {
            String flagKey = key.substring(0, key.length() - ".minTier".length());
            try {
                FeatureFlag flag = FeatureFlag.valueOf(flagKey.toUpperCase());
                return featureConfigService.getFeatureConfig(flag)
                        .map(config -> {
                            String tier = config.getMinSubscriptionTier();
                            return ProviderEvaluation.<String>builder()
                                    .value(tier != null ? tier : defaultValue)
                                    .reason(Reason.STATIC.name())
                                    .variant("config")
                                    .build();
                        })
                        .orElseGet(() -> ProviderEvaluation.<String>builder()
                                .value(defaultValue)
                                .reason(Reason.DEFAULT.name())
                                .build());
            } catch (IllegalArgumentException ignored) {
                // fall through to default
            }
        }
        return ProviderEvaluation.<String>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.name())
                .build();
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key,
                                                             Integer defaultValue,
                                                             EvaluationContext ctx) {
        return ProviderEvaluation.<Integer>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.name())
                .build();
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key,
                                                           Double defaultValue,
                                                           EvaluationContext ctx) {
        return ProviderEvaluation.<Double>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.name())
                .build();
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key,
                                                          Value defaultValue,
                                                          EvaluationContext ctx) {
        return ProviderEvaluation.<Value>builder()
                .value(defaultValue)
                .reason(Reason.DEFAULT.name())
                .build();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Returns true if {@code userTier} meets or exceeds {@code minTier}. */
    private boolean meetsTierRequirement(String userTier, String minTier) {
        int userLevel = TIER_ORDER.getOrDefault(userTier.toUpperCase(), 0);
        int minLevel  = TIER_ORDER.getOrDefault(minTier.toUpperCase(), 0);
        return userLevel >= minLevel;
    }
}
