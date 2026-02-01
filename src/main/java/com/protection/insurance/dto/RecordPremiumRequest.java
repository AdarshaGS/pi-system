package com.protection.insurance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPremiumRequest {

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private LocalDate dueDate;

    @NotNull(message = "Payment status is required")
    private String paymentStatus; // PAID, PENDING, MISSED, SCHEDULED

    private String paymentMethod;
    private String transactionReference;
    private Boolean isAutoRenewal;
    private String notes;
}
