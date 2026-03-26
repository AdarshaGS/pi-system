package com.budget;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a recurring subscription (Netflix, Spotify, etc.)
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Service name is required")
    @Column(name = "service_name", nullable = false, length = 200)
    private String serviceName;

    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Billing cycle is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private SubscriptionCategory category;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_renewal_date")
    private LocalDate nextRenewalDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = true;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "reminder_days_before", nullable = false)
    private Integer reminderDaysBefore = 3; // Default: remind 3 days before renewal

    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SubscriptionStatus.ACTIVE;
        }
        if (autoRenewal == null) {
            autoRenewal = true;
        }
        if (reminderDaysBefore == null) {
            reminderDaysBefore = 3;
        }
        calculateNextRenewalDate();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate next renewal date based on billing cycle
     */
    public void calculateNextRenewalDate() {
        if (startDate != null && status == SubscriptionStatus.ACTIVE) {
            LocalDate baseDate = (nextRenewalDate != null && nextRenewalDate.isAfter(LocalDate.now())) 
                ? nextRenewalDate 
                : startDate;

            switch (billingCycle) {
                case WEEKLY:
                    this.nextRenewalDate = baseDate.plusWeeks(1);
                    break;
                case MONTHLY:
                    this.nextRenewalDate = baseDate.plusMonths(1);
                    break;
                case QUARTERLY:
                    this.nextRenewalDate = baseDate.plusMonths(3);
                    break;
                case HALF_YEARLY:
                    this.nextRenewalDate = baseDate.plusMonths(6);
                    break;
                case YEARLY:
                    this.nextRenewalDate = baseDate.plusYears(1);
                    break;
            }
        }
    }

    /**
     * Check if subscription is unused (last used more than 30 days ago)
     */
    public boolean isUnused() {
        if (lastUsedDate == null) {
            return false; // No usage data
        }
        return lastUsedDate.plusDays(30).isBefore(LocalDate.now());
    }

    /**
     * Check if renewal reminder should be sent
     */
    public boolean shouldSendRenewalReminder() {
        if (status != SubscriptionStatus.ACTIVE || nextRenewalDate == null) {
            return false;
        }
        LocalDate reminderDate = nextRenewalDate.minusDays(reminderDaysBefore);
        return LocalDate.now().equals(reminderDate) || LocalDate.now().isAfter(reminderDate);
    }

    /**
     * Cancel subscription
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancellationDate = LocalDate.now();
        this.autoRenewal = false;
    }

    /**
     * Pause subscription
     */
    public void pause() {
        this.status = SubscriptionStatus.PAUSED;
    }

    /**
     * Resume subscription
     */
    public void resume() {
        if (status == SubscriptionStatus.PAUSED) {
            this.status = SubscriptionStatus.ACTIVE;
            calculateNextRenewalDate();
        }
    }

    /**
     * Mark subscription as expired
     */
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /**
     * Calculate annual cost
     */
    public BigDecimal calculateAnnualCost() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        
        return switch (billingCycle) {
            case WEEKLY -> amount.multiply(BigDecimal.valueOf(52));
            case MONTHLY -> amount.multiply(BigDecimal.valueOf(12));
            case QUARTERLY -> amount.multiply(BigDecimal.valueOf(4));
            case HALF_YEARLY -> amount.multiply(BigDecimal.valueOf(2));
            case YEARLY -> amount;
        };
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public SubscriptionCategory getCategory() {
        return category;
    }

    public void setCategory(SubscriptionCategory category) {
        this.category = category;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getNextRenewalDate() {
        return nextRenewalDate;
    }

    public void setNextRenewalDate(LocalDate nextRenewalDate) {
        this.nextRenewalDate = nextRenewalDate;
    }

    public LocalDate getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDate cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Boolean getAutoRenewal() {
        return autoRenewal;
    }

    public void setAutoRenewal(Boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getReminderDaysBefore() {
        return reminderDaysBefore;
    }

    public void setReminderDaysBefore(Integer reminderDaysBefore) {
        this.reminderDaysBefore = reminderDaysBefore;
    }

    public LocalDate getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(LocalDate lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
