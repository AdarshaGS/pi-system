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
public class FileClaimRequest {

    private String claimNumber;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be positive")
    private BigDecimal claimAmount;

    @NotNull(message = "Claim date is required")
    private LocalDate claimDate;

    private LocalDate incidentDate;

    @NotNull(message = "Claim status is required")
    private String claimStatus; // SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, SETTLED, WITHDRAWN

    private String claimType;
    private String description;
}
