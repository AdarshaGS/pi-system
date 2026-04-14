package com.pisystem.shared.features;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the PI System database-backed feature flag provider with the
 * OpenFeature singleton and exposes a ready-to-use {@link Client} bean.
 *
 * <h3>How the pieces fit together</h3>
 * <pre>
 *   ┌────────────────────────────────────────────────────────────┐
 *   │  Application code / FeatureCheckAspect                    │
 *   │       ↓ openFeatureClient.getBooleanDetails(key, ctx)      │
 *   ├────────────────────────────────────────────────────────────┤
 *   │  OpenFeature Client  (standard evaluation API)            │
 *   │       ↓ getBooleanEvaluation(key, default, ctx)            │
 *   ├────────────────────────────────────────────────────────────┤
 *   │  PiSystemFlagProvider  (our custom OpenFeature provider)  │
 *   │       ↓ featureConfigService.isFeatureEnabled()            │
 *   ├────────────────────────────────────────────────────────────┤
 *   │  FeatureConfigService → FeatureConfig DB table + Cache    │
 *   └────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * To swap providers (e.g. move to LaunchDarkly in the future), replace
 * {@link PiSystemFlagProvider} with any OpenFeature-compatible provider
 * and update this class only — zero changes to callers.
 */
@Configuration
public class OpenFeatureConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenFeatureConfig.class);

    /**
     * Registers {@link PiSystemFlagProvider} with the global
     * {@link OpenFeatureAPI} and returns a named client for this application.
     *
     * <p>Spring guarantees that {@code PiSystemFlagProvider} (and therefore
     * {@code FeatureConfigService} with its {@code @PostConstruct} bootstrap)
     * is fully initialised before this bean is created.
     */
    @Bean
    public Client openFeatureClient(PiSystemFlagProvider provider) {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(provider);
        log.info("OpenFeature provider registered: '{}'", provider.getMetadata().getName());
        return api.getClient("pi-system");
    }
}
