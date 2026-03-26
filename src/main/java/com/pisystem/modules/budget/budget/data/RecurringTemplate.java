package com.budget.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for recurring transaction templates
 */
@Entity
@Table(name = "recurring_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // EXPENSE or INCOME

    @Column(nullable = false, length = 100)
    private String name; // Template name (e.g., "Monthly Rent", "Weekly Groceries")

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ExpenseCategory category; // For expenses only

    @Column(name = "custom_category_name", length = 50)
    private String customCategoryName; // For user-defined categories

    @Column(length = 100)
    private String source; // For income only (e.g., "Salary", "Freelance")

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrencePattern pattern; // DAILY, WEEKLY, MONTHLY, etc.

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate; // Optional - null means indefinite

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_generated")
    private LocalDate lastGenerated; // Last date when transaction was generated

    @Column(name = "next_run_date")
    private LocalDate nextRunDate; // Next scheduled date for generation

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to get the effective category name
     */
    public String getEffectiveCategoryName() {
        if (customCategoryName != null && !customCategoryName.isEmpty()) {
            return customCategoryName;
        }
        return category != null ? category.name() : null;
    }

    /**
     * Check if this is a custom category
     */
    public boolean isCustomCategory() {
        return customCategoryName != null && !customCategoryName.isEmpty();
    }
}
