package com.budget.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ExpenseCategory category;

    @Column(name = "custom_category_name", length = 50)
    private String customCategoryName; // For user-defined categories

    @Column(name = "monthly_limit", nullable = false)
    private BigDecimal monthlyLimit;

    @Column(name = "month_year", nullable = false)
    private String monthYear; // Format: YYYY-MM

    // Helper method to get the effective category name
    public String getEffectiveCategoryName() {
        if (customCategoryName != null && !customCategoryName.isEmpty()) {
            return customCategoryName;
        }
        return category != null ? category.name() : null;
    }

    // Check if this is a custom category
    public boolean isCustomCategory() {
        return customCategoryName != null && !customCategoryName.isEmpty();
    }
}

