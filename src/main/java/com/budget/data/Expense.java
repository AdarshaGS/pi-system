package com.budget.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ExpenseCategory category;

    @Column(name = "custom_category_name", length = 50)
    private String customCategoryName; // For user-defined categories

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    private String description;

    @Column(length = 500)
    private String notes; // Additional notes or comments

    @ManyToMany
    @JoinTable(
        name = "expense_tags",
        joinColumns = @JoinColumn(name = "expense_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

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

