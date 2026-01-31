package com.budget.dto;

import com.budget.data.ExpenseCategory;
import com.budget.data.RecurrencePattern;
import com.budget.data.RecurringTemplate;
import com.budget.data.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating/updating recurring templates
 * Handles the JSON request properly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTemplateDTO {
    
    private Long id;
    private Long userId;
    private TransactionType type;
    private String name;
    private ExpenseCategory category; // For EXPENSE type
    private String customCategoryName;
    private String source; // For INCOME type (ignored if type is EXPENSE)
    private BigDecimal amount;
    private RecurrencePattern pattern;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private String description;
    
    /**
     * Convert DTO to Entity
     */
    public RecurringTemplate toEntity() {
        RecurringTemplate template = RecurringTemplate.builder()
                .id(this.id)
                .userId(this.userId)
                .type(this.type)
                .name(this.name)
                .amount(this.amount)
                .pattern(this.pattern)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .isActive(this.isActive != null ? this.isActive : true)
                .description(this.description)
                .build();
        
        // Set category/source based on transaction type
        if (this.type == TransactionType.EXPENSE) {
            template.setCategory(this.category);
            template.setCustomCategoryName(this.customCategoryName);
            template.setSource(null); // Explicitly null for expenses
        } else if (this.type == TransactionType.INCOME) {
            template.setSource(this.source);
            template.setCategory(null); // Explicitly null for income
            template.setCustomCategoryName(null);
        }
        
        return template;
    }
    
    /**
     * Convert Entity to DTO
     */
    public static RecurringTemplateDTO fromEntity(RecurringTemplate template) {
        return RecurringTemplateDTO.builder()
                .id(template.getId())
                .userId(template.getUserId())
                .type(template.getType())
                .name(template.getName())
                .category(template.getCategory())
                .customCategoryName(template.getCustomCategoryName())
                .source(template.getSource())
                .amount(template.getAmount())
                .pattern(template.getPattern())
                .startDate(template.getStartDate())
                .endDate(template.getEndDate())
                .isActive(template.getIsActive())
                .description(template.getDescription())
                .build();
    }
}
