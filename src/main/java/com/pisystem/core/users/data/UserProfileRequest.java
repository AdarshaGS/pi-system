package com.pisystem.core.users.data;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or updating a UserProfile.
 * Used as the request body for POST /api/v1/users/{userId}/profile
 * and PUT /api/v1/users/{userId}/profile
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileRequest {

    private LocalDate dateOfBirth;
    private Integer dependents;
    private String city;
    private CityTier cityTier;

    /** Annual gross income in INR */
    private BigDecimal annualIncome;

    /**
     * Monthly take-home income in INR (optional; derived from annualIncome if
     * omitted)
     */
    private BigDecimal monthlyIncome;

    private EmploymentType employmentType;
    private Integer emergencyFundMonths;
    private RiskTolerance riskTolerance;
    private Integer retirementAge;
    private Integer lifeExpectancy;
}
