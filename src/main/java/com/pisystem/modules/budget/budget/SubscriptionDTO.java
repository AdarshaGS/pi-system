package com.budget;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating/updating subscriptions
 */
public class SubscriptionDTO {

    private Long id;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Category is required")
    private SubscriptionCategory category;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate nextRenewalDate;

    private LocalDate cancellationDate;

    private SubscriptionStatus status;

    private Boolean autoRenewal = true;

    private String paymentMethod;

    private Integer reminderDaysBefore = 3;

    private LocalDate lastUsedDate;

    private String notes;

    private String websiteUrl;

    private BigDecimal annualCost;

    private Boolean isUnused;

    // Constructors

    public SubscriptionDTO() {
    }

    public SubscriptionDTO(Subscription subscription) {
        this.id = subscription.getId();
        this.serviceName = subscription.getServiceName();
        this.description = subscription.getDescription();
        this.amount = subscription.getAmount();
        this.billingCycle = subscription.getBillingCycle();
        this.category = subscription.getCategory();
        this.startDate = subscription.getStartDate();
        this.nextRenewalDate = subscription.getNextRenewalDate();
        this.cancellationDate = subscription.getCancellationDate();
        this.status = subscription.getStatus();
        this.autoRenewal = subscription.getAutoRenewal();
        this.paymentMethod = subscription.getPaymentMethod();
        this.reminderDaysBefore = subscription.getReminderDaysBefore();
        this.lastUsedDate = subscription.getLastUsedDate();
        this.notes = subscription.getNotes();
        this.websiteUrl = subscription.getWebsiteUrl();
        this.annualCost = subscription.calculateAnnualCost();
        this.isUnused = subscription.isUnused();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getAnnualCost() {
        return annualCost;
    }

    public void setAnnualCost(BigDecimal annualCost) {
        this.annualCost = annualCost;
    }

    public Boolean getIsUnused() {
        return isUnused;
    }

    public void setIsUnused(Boolean unused) {
        isUnused = unused;
    }
}
