package com.users.data;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO returned by GET /api/v1/users/{userId}/profile
 * Includes computed fields like age and effectiveMonthlyIncome.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private Long id;
    private Long userId;

    // Personal
    private LocalDate dateOfBirth;
    private Integer age; // computed from dateOfBirth
    private Integer dependents;
    private String city;
    private CityTier cityTier;

    // Financial
    private BigDecimal annualIncome;
    private BigDecimal monthlyIncome;
    private BigDecimal effectiveMonthlyIncome; // computed fallback
    private EmploymentType employmentType;
    private Integer emergencyFundMonths;

    // Investment
    private RiskTolerance riskTolerance;
    private Integer retirementAge;
    private Integer lifeExpectancy;

    // Meta
    private Boolean isProfileComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
