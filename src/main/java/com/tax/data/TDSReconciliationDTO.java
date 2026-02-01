package com.tax.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TDSReconciliationDTO {
    private Long userId;
    private String financialYear;
    
    private BigDecimal totalTDSDeducted;
    private BigDecimal totalTDSVerified;
    private BigDecimal totalTDSClaimed;
    private BigDecimal tdsBalance; // Unclaimed TDS
    
    private List<TDSEntryDTO> tdsEntries;
    private List<String> mismatches;
    private List<String> recommendations;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TDSEntryDTO {
        private Long id;
        private String deductorName;
        private String deductorTan;
        private BigDecimal tdsAmount;
        private BigDecimal incomeAmount;
        private String tdsSection;
        private String deductionDate;
        private String certificateNumber;
        private TDSStatus status;
        private String remarks;
    }
}
