package com.tax.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRegimeComparisonDTO {
    private Long userId;
    private String financialYear;
    private BigDecimal grossIncome;
    
    // Old Regime
    private BigDecimal oldRegimeTaxableIncome;
    private BigDecimal oldRegimeDeductions;
    private BigDecimal oldRegimeTotalTax;
    private BigDecimal oldRegimeEffectiveRate;
    private Map<String, BigDecimal> oldRegimeTaxSlabs;
    
    // New Regime
    private BigDecimal newRegimeTaxableIncome;
    private BigDecimal newRegimeDeductions;
    private BigDecimal newRegimeTotalTax;
    private BigDecimal newRegimeEffectiveRate;
    private Map<String, BigDecimal> newRegimeTaxSlabs;
    
    // Comparison
    private BigDecimal taxSavings; // Negative if new regime is better
    private TaxRegime recommendedRegime;
    private String recommendation;
}
