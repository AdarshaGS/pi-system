package com.budget;

import com.common.features.FeatureFlag;
import com.common.features.RequiresFeature;
import com.common.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Subscription Management
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscription Management", description = "APIs for managing recurring subscriptions (Netflix, Spotify, etc.)")
@RequiresFeature(FeatureFlag.SUBSCRIPTIONS)
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private AuthenticationHelper authenticationHelper;

    /**
     * Create a new subscription
     */
    @PostMapping
    @Operation(summary = "Create subscription", description = "Create a new recurring subscription")
    public ResponseEntity<SubscriptionDTO> createSubscription(
            @Valid @RequestBody SubscriptionDTO subscriptionDTO) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to create subscription for user: {}", userId);

        SubscriptionDTO created = subscriptionService.createSubscription(userId, subscriptionDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update an existing subscription
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update subscription", description = "Update an existing subscription")
    public ResponseEntity<SubscriptionDTO> updateSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id,
            @Valid @RequestBody SubscriptionDTO subscriptionDTO) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to update subscription ID: {} for user: {}", id, userId);

        SubscriptionDTO updated = subscriptionService.updateSubscription(userId, id, subscriptionDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get subscription by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get subscription", description = "Get subscription details by ID")
    public ResponseEntity<SubscriptionDTO> getSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get subscription ID: {} for user: {}", id, userId);

        SubscriptionDTO subscription = subscriptionService.getSubscriptionById(userId, id);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Get all subscriptions with pagination
     */
    @GetMapping
    @Operation(summary = "Get all subscriptions", description = "Get all subscriptions with pagination and sorting")
    public ResponseEntity<Page<SubscriptionDTO>> getAllSubscriptions(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "nextRenewalDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get all subscriptions for user: {}", userId);

        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SubscriptionDTO> subscriptions = subscriptionService.getAllSubscriptions(userId, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Get subscriptions by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get subscriptions by status", description = "Get subscriptions filtered by status")
    public ResponseEntity<Page<SubscriptionDTO>> getSubscriptionsByStatus(
            @Parameter(description = "Subscription status") @PathVariable("status") SubscriptionStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get subscriptions by status: {} for user: {}", status, userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("nextRenewalDate").ascending());
        Page<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsByStatus(userId, status, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Get subscriptions by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get subscriptions by category", description = "Get subscriptions filtered by category")
    public ResponseEntity<Page<SubscriptionDTO>> getSubscriptionsByCategory(
            @Parameter(description = "Subscription category") @PathVariable("category") SubscriptionCategory category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get subscriptions by category: {} for user: {}", category, userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("serviceName").ascending());
        Page<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsByCategory(userId, category, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Get active subscriptions
     */
    @GetMapping("/active")
    @Operation(summary = "Get active subscriptions", description = "Get all active subscriptions")
    public ResponseEntity<List<SubscriptionDTO>> getActiveSubscriptions() {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get active subscriptions for user: {}", userId);

        List<SubscriptionDTO> subscriptions = subscriptionService.getActiveSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Get subscriptions expiring soon
     */
    @GetMapping("/expiring-soon")
    @Operation(summary = "Get subscriptions expiring soon", description = "Get subscriptions expiring within specified days")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionsExpiringSoon(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "30") int days) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get subscriptions expiring within {} days for user: {}", days, userId);

        List<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsExpiringSoon(userId, days);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Get unused subscriptions
     */
    @GetMapping("/unused")
    @Operation(summary = "Get unused subscriptions", description = "Get subscriptions not used in last 30 days")
    public ResponseEntity<List<SubscriptionDTO>> getUnusedSubscriptions() {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to get unused subscriptions for user: {}", userId);

        List<SubscriptionDTO> subscriptions = subscriptionService.getUnusedSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Cancel a subscription
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancel an active subscription")
    public ResponseEntity<Map<String, String>> cancelSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to cancel subscription ID: {} for user: {}", id, userId);

        subscriptionService.cancelSubscription(userId, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Subscription cancelled successfully");
        response.put("subscriptionId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Pause a subscription
     */
    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause subscription", description = "Pause an active subscription")
    public ResponseEntity<Map<String, String>> pauseSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to pause subscription ID: {} for user: {}", id, userId);

        subscriptionService.pauseSubscription(userId, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Subscription paused successfully");
        response.put("subscriptionId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Resume a paused subscription
     */
    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume subscription", description = "Resume a paused subscription")
    public ResponseEntity<Map<String, String>> resumeSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to resume subscription ID: {} for user: {}", id, userId);

        subscriptionService.resumeSubscription(userId, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Subscription resumed successfully");
        response.put("subscriptionId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Renew a subscription (update next renewal date)
     */
    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew subscription", description = "Manually renew a subscription and update next renewal date")
    public ResponseEntity<Map<String, String>> renewSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to renew subscription ID: {} for user: {}", id, userId);

        subscriptionService.renewSubscription(userId, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Subscription renewed successfully");
        response.put("subscriptionId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Update last used date
     */
    @PutMapping("/{id}/last-used")
    @Operation(summary = "Update last used date", description = "Update the last used date for a subscription")
    public ResponseEntity<Map<String, String>> updateLastUsedDate(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id,
            @Parameter(description = "Last used date") @RequestParam String lastUsedDate) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to update last used date for subscription ID: {} to: {}", id, lastUsedDate);

        LocalDate date = LocalDate.parse(lastUsedDate);
        subscriptionService.updateLastUsedDate(userId, id, date);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Last used date updated successfully");
        response.put("subscriptionId", id.toString());
        response.put("lastUsedDate", lastUsedDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a subscription
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete subscription", description = "Permanently delete a subscription")
    public ResponseEntity<Map<String, String>> deleteSubscription(
            @Parameter(description = "Subscription ID") @PathVariable("id") Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to delete subscription ID: {} for user: {}", id, userId);

        subscriptionService.deleteSubscription(userId, id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Subscription deleted successfully");
        response.put("subscriptionId", id.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Get subscription analytics
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get subscription analytics", description = "Get comprehensive analytics and cost analysis for all subscriptions")
    public ResponseEntity<SubscriptionAnalyticsDTO> getSubscriptionAnalytics() {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.info("REST request to get subscription analytics for user: {}", userId);

        SubscriptionAnalyticsDTO analytics = subscriptionService.getSubscriptionAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Search subscriptions by service name
     */
    @GetMapping("/search")
    @Operation(summary = "Search subscriptions", description = "Search subscriptions by service name")
    public ResponseEntity<List<SubscriptionDTO>> searchSubscriptions(
            @Parameter(description = "Service name to search") @RequestParam String serviceName) {
        Long userId = authenticationHelper.getCurrentUserId();
        logger.debug("REST request to search subscriptions by service name: {} for user: {}", serviceName, userId);

        List<SubscriptionDTO> subscriptions = subscriptionService.searchByServiceName(userId, serviceName);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Get subscription categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get categories", description = "Get all available subscription categories")
    public ResponseEntity<SubscriptionCategory[]> getCategories() {
        return ResponseEntity.ok(SubscriptionCategory.values());
    }

    /**
     * Get billing cycles
     */
    @GetMapping("/billing-cycles")
    @Operation(summary = "Get billing cycles", description = "Get all available billing cycles")
    public ResponseEntity<BillingCycle[]> getBillingCycles() {
        return ResponseEntity.ok(BillingCycle.values());
    }

    /**
     * Get subscription statuses
     */
    @GetMapping("/statuses")
    @Operation(summary = "Get statuses", description = "Get all available subscription statuses")
    public ResponseEntity<SubscriptionStatus[]> getStatuses() {
        return ResponseEntity.ok(SubscriptionStatus.values());
    }
}
