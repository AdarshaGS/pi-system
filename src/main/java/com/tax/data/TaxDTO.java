package com.tax.data;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxDTO {
    private Long id;
    private Long userId;
    private String financialYear;
    private BigDecimal capitalGainsShortTerm;
    private BigDecimal capitalGainsLongTerm;
    private BigDecimal dividendIncome;
    private BigDecimal taxPaid;
    private BigDecimal taxPayable;
    private BigDecimal netTaxLiability;
}