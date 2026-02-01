package com.tax.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapitalGainsSummaryDTO {
    private Long userId;
    private String financialYear;
    
    // STCG Summary
    private BigDecimal totalSTCG;
    private BigDecimal totalSTCGTax;
    private List<CapitalGainsDetail> stcgTransactions;
    
    // LTCG Summary
    private BigDecimal totalLTCG;
    private BigDecimal totalLTCGTax;
    private BigDecimal ltcgExemptionUsed; // 1 lakh exemption for equity
    private List<CapitalGainsDetail> ltcgTransactions;
    
    // Overall
    private BigDecimal totalCapitalGainsTax;
    private Integer transactionCount;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CapitalGainsDetail {
        private Long transactionId;
        private AssetType assetType;
        private String assetName;
        private String saleDate;
        private String purchaseDate;
        private Integer holdingPeriodDays;
        private BigDecimal purchaseValue;
        private BigDecimal saleValue;
        private BigDecimal capitalGain;
        private CapitalGainType gainType;
        private BigDecimal taxAmount;
        private BigDecimal taxRate;
    }
}
