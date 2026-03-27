package com.pisystem.core.users.data;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one link with the users table.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // ──────────────────────────────────────────────────────────────
    // Personal Info
    // ──────────────────────────────────────────────────────────────

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Number of financial dependents (spouse, children, parents).
     * Used by Insurance module for life cover recommendations.
     */
    @Column(name = "dependents", nullable = false, columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer dependents = 0;

    @Column(name = "city", length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "city_tier", length = 20)
    @Builder.Default
    private CityTier cityTier = CityTier.METRO;

    // ──────────────────────────────────────────────────────────────
    // Financial Info
    // ──────────────────────────────────────────────────────────────

    /**
     * Annual gross income in INR.
     * Used by: Insurance (life cover = 10x income), Budget (savings rate %), Loan
     * (DTI ratio).
     */
    @Column(name = "annual_income", precision = 19, scale = 2)
    private BigDecimal annualIncome;

    /**
     * Monthly take-home (net) income in INR.
     * Derived value — can be set independently for salaried users with known
     * in-hand salary.
     */
    @Column(name = "monthly_income", precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 30)
    @Builder.Default
    private EmploymentType employmentType = EmploymentType.SALARIED;

    /**
     * Emergency fund target in months of expenses.
     * Default 6 months. Used in Budget health score.
     */
    @Column(name = "emergency_fund_months", nullable = false, columnDefinition = "INT DEFAULT 6")
    @Builder.Default
    private Integer emergencyFundMonths = 6;

    // ──────────────────────────────────────────────────────────────
    // Investment Preferences
    // ──────────────────────────────────────────────────────────────

    /**
     * User's declared risk appetite.
     * Used by: Portfolio Risk Engine (alignment check), Retirement Planner (asset
     * allocation advice).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance", length = 20)
    @Builder.Default
    private RiskTolerance riskTolerance = RiskTolerance.MODERATE;

    /**
     * Target retirement age. Default 60.
     * Used by Retirement Planning Service.
     */
    @Column(name = "retirement_age", nullable = false, columnDefinition = "INT DEFAULT 60")
    @Builder.Default
    private Integer retirementAge = 60;

    /**
     * Life expectancy used for retirement corpus calculation. Default 80.
     */
    @Column(name = "life_expectancy", nullable = false, columnDefinition = "INT DEFAULT 80")
    @Builder.Default
    private Integer lifeExpectancy = 80;

    // ──────────────────────────────────────────────────────────────
    // Profile Completeness
    // ──────────────────────────────────────────────────────────────

    @Column(name = "is_profile_complete", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isProfileComplete = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ──────────────────────────────────────────────────────────────
    // Convenience helpers (not persisted)
    // ──────────────────────────────────────────────────────────────

    /**
     * Derives age from dateOfBirth. Returns 0 if not set.
     */
    @Transient
    public int getAge() {
        if (dateOfBirth == null)
            return 0;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    /**
     * Returns monthly income. Falls back to annualIncome / 12 when monthlyIncome is
     * null.
     */
    @Transient
    public BigDecimal getEffectiveMonthlyIncome() {
        if (monthlyIncome != null)
            return monthlyIncome;
        if (annualIncome != null)
            return annualIncome.divide(java.math.BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
        return BigDecimal.ZERO;
    }
}
