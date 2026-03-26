package com.budget.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeDTO {
    
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    private Long userId;
    
    private String source; // SALARY, DIVIDEND, RENTAL, FREELANCE, BONUS, INTEREST, OTHER
    
    private BigDecimal amount;
    
    private LocalDate date;
    
    private Boolean isRecurring;
    
    private Boolean isStable;
    
    private String description;
}
