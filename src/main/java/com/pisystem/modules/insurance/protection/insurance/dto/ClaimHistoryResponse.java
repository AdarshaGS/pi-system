package com.protection.insurance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimHistoryResponse {

    private Long insuranceId;
    private String policyNumber;
    private Integer totalClaims;
    private Integer approvedClaims;
    private Integer rejectedClaims;
    private Integer pendingClaims;
    private BigDecimal totalClaimAmount;
    private BigDecimal totalApprovedAmount;
    private BigDecimal totalSettledAmount;
    private List<ClaimSummary> claims;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaimSummary {
        private Long id;
        private String claimNumber;
        private BigDecimal claimAmount;
        private BigDecimal approvedAmount;
        private String claimDate;
        private String incidentDate;
        private String settlementDate;
        private String claimStatus;
        private String claimType;
        private String description;
        private String rejectionReason;
    }
}
