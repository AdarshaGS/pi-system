package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_saving_investments", indexes = {
    @Index(name = "idx_user_financial_year", columnList = "user_id, financial_year"),
    @Index(name = "idx_investment_type", columnList = "investment_type"),
    @Index(name = "idx_linked_entity", columnList = "linked_entity_type, linked_entity_id"),
    @Index(name = "idx_investment_date", columnList = "investment_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxSavingInvestment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "investment_type", length = 50, nullable = false)
    private String investmentType; // 80C, 80D, 80E, 80G, 80CCD1B, 24B
    
    @Column(name = "category", length = 100, nullable = false)
    private String category; // PPF, ELSS, LIC, NSC, MEDICLAIM, etc.
    
    @Column(name = "investment_name", nullable = false)
    private String investmentName;
    
    @Column(name = "amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "investment_date", nullable = false)
    private LocalDate investmentDate;
    
    @Column(name = "financial_year", length = 10, nullable = false)
    private String financialYear; // e.g., 2025-26
    
    // Linking to actual records
    @Column(name = "linked_entity_type", length = 50)
    private String linkedEntityType; // INSURANCE, FD, LOAN, MUTUAL_FUND, etc.
    
    @Column(name = "linked_entity_id")
    private Long linkedEntityId;
    
    // Specific fields for different sections
    @Column(name = "policy_number", length = 100)
    private String policyNumber; // For insurance
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate; // For FD, insurance
    
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate; // For FD, NSC
    
    // 80D specific
    @Column(name = "self_or_family", length = 20)
    private String selfOrFamily; // SELF, PARENT - for 80D
    
    @Column(name = "is_senior_citizen")
    @Builder.Default
    private Boolean isSeniorCitizen = false; // For 80D - different limits
    
    // 80G specific
    @Column(name = "donation_mode", length = 50)
    private String donationMode; // CASH, CHEQUE, ONLINE - for 80G
    
    @Column(name = "pan_of_donee", length = 10)
    private String panOfDonee; // For 80G donations
    
    @Column(name = "is_100_percent_deduction")
    @Builder.Default
    private Boolean is100PercentDeduction = false; // 80G - 100% or 50%
    
    // Auto-population tracking
    @Column(name = "is_auto_populated")
    @Builder.Default
    private Boolean isAutoPopulated = false;
    
    @Column(name = "auto_populated_from", length = 100)
    private String autoPopulatedFrom; // Source of auto-population
    
    // Verification
    @Column(name = "has_proof")
    @Builder.Default
    private Boolean hasProof = false;
    
    @Column(name = "proof_uploaded")
    @Builder.Default
    private Boolean proofUploaded = false;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Backward compatibility aliases
    public String getSection() {
        return investmentType;
    }
    
    public void setSection(String section) {
        this.investmentType = section;
    }
    
    public LocalDate getCreatedDate() {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }
    
    public void setCreatedDate(LocalDate date) {
        // Handled by @CreationTimestamp
    }
}
