package com.tax.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxSavingRecommendationDTO {
    private Long userId;
    private String financialYear;
    private BigDecimal currentIncome;
    private BigDecimal currentTaxLiability;
    
    private List<TaxSavingOpportunity> opportunities;
    private BigDecimal totalPotentialSavings;
    private BigDecimal totalRecommendedInvestment;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaxSavingOpportunity {
        private TaxSavingSection section;
        private BigDecimal currentInvestment;
        private BigDecimal availableLimit;
        private BigDecimal recommendedInvestment;
        private BigDecimal potentialTaxSavings;
        private String description;
        private Integer priority; // 1=High, 2=Medium, 3=Low
        private List<String> suggestedInstruments;
    }
}
