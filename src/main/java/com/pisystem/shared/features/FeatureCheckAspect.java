package com.pisystem.shared.features;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.ImmutableContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect that enforces {@link RequiresFeature} on methods and classes.
 *
 * <p>Uses the OpenFeature {@link Client} to evaluate each flag, which means:
 * <ul>
 *   <li>Every gate check returns a structured {@link FlagEvaluationDetails}
 *       with a {@code reason} (STATIC / DISABLED / TARGETING_MATCH / ERROR)
 *       and a {@code variant} ("enabled" / "parent-module-disabled" /
 *       "tier-restricted") — both are logged for auditability.</li>
 *   <li>The current user's principal name is passed as the OpenFeature
 *       <em>targeting key</em>, enabling future per-user targeting rules
 *       without changing this aspect.</li>
 * </ul>
 */
@Aspect
@Component
public class FeatureCheckAspect {

    private static final Logger logger = LoggerFactory.getLogger(FeatureCheckAspect.class);

    /** OpenFeature client — backed by {@link PiSystemFlagProvider}. */
    @Autowired
    private Client openFeatureClient;

    @Around("@annotation(com.pisystem.shared.features.RequiresFeature) || @within(com.pisystem.shared.features.RequiresFeature)")
    public Object checkFeature(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Method-level annotation takes precedence over class-level
        RequiresFeature annotation = method.getAnnotation(RequiresFeature.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(RequiresFeature.class);
        }

        if (annotation != null) {
            FeatureFlag requiredFeature = annotation.value();

            // Build EvaluationContext from the current security principal.
            // The targeting key (user identity) enables per-user flag targeting
            // in the future — e.g. beta cohorts, subscription tier gating.
            EvaluationContext ctx = buildEvaluationContext();

            // getBooleanDetails returns value + reason + variant + errorCode
            FlagEvaluationDetails<Boolean> details =
                    openFeatureClient.getBooleanDetails(requiredFeature.name(), false, ctx);

            logger.debug("Feature evaluation: flag={}, value={}, reason={}, variant={}",
                    requiredFeature.name(),
                    details.getValue(),
                    details.getReason(),
                    details.getVariant());

            if (!Boolean.TRUE.equals(details.getValue())) {
                String message = (annotation.message() == null || annotation.message().isEmpty())
                        ? "Feature '" + requiredFeature.getDisplayName() + "' is not enabled"
                        : annotation.message();

                logger.warn("Feature denied: flag={}, reason={}, variant={}, method={}",
                        requiredFeature.name(),
                        details.getReason(),
                        details.getVariant(),
                        method.getName());

                throw new FeatureNotEnabledException(message, requiredFeature.name());
            }
        }

        return joinPoint.proceed();
    }

    /**
     * Builds an {@link EvaluationContext} from the current Spring Security
     * authentication. The principal name becomes the OpenFeature targeting key.
     *
     * <p>Extend this method to enrich the context with subscription tier,
     * beta-user flag, or any other attribute you want providers to target on:
     * <pre>
     *   return new ImmutableContext(
     *       auth.getName(),
     *       Map.of("subscriptionTier", new Value(userSubscriptionTier))
     *   );
     * </pre>
     */
    private EvaluationContext buildEvaluationContext() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return new ImmutableContext(auth.getName());
            }
        } catch (Exception e) {
            logger.debug("Could not build EvaluationContext from SecurityContext", e);
        }
        return new ImmutableContext();
    }
}

