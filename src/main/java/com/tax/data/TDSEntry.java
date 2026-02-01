package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tds_entries", indexes = {
    @Index(name = "idx_user_financial_year", columnList = "user_id, financial_year"),
    @Index(name = "idx_quarter", columnList = "quarter"),
    @Index(name = "idx_deductor_tan", columnList = "deductor_tan"),
    @Index(name = "idx_reconciliation_status", columnList = "reconciliation_status"),
    @Index(name = "idx_section", columnList = "section"),
    @Index(name = "idx_tds_deposited_date", columnList = "tds_deposited_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TDSEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "financial_year", length = 10, nullable = false)
    private String financialYear; // e.g., 2025-26
    
    @Column(name = "quarter", nullable = false)
    private Integer quarter; // 1, 2, 3, or 4
    
    // Deductor details
    @Column(name = "deductor_name", nullable = false)
    private String deductorName;
    
    @Column(name = "deductor_tan", length = 10, nullable = false)
    private String deductorTan; // Tax Deduction Account Number
    
    @Column(name = "deductor_pan", length = 10)
    private String deductorPan; // PAN of deductor
    
    // TDS details
    @Column(name = "section", length = 20, nullable = false)
    private String section; // 192, 194A, 194C, 194H, 194J, etc.
    
    @Column(name = "income_type", length = 100, nullable = false)
    private String incomeType; // SALARY, INTEREST, PROFESSIONAL, etc.
    
    @Column(name = "amount_paid", nullable = false, precision = 20, scale = 2)
    private BigDecimal amountPaid; // Gross amount paid
    
    @Column(name = "tds_deducted", nullable = false, precision = 20, scale = 2)
    private BigDecimal tdsDeducted;
    
    @Column(name = "tds_deposited_date")
    private LocalDate tdsDepositedDate; // Date when TDS was deposited
    
    // Backward compatibility aliases
    public BigDecimal getTdsAmount() {
        return tdsDeducted;
    }
    
    public void setTdsAmount(BigDecimal amount) {
        this.tdsDeducted = amount;
    }
    
    public BigDecimal getIncomeAmount() {
        return amountPaid;
    }
    
    public void setIncomeAmount(BigDecimal amount) {
        this.amountPaid = amount;
    }
    
    public String getTdsSection() {
        return section;
    }
    
    public void setTdsSection(String section) {
        this.section = section;
    }
    
    public LocalDate getDeductionDate() {
        return tdsDepositedDate;
    }
    
    public void setDeductionDate(LocalDate date) {
        this.tdsDepositedDate = date;
    }
    
    public String getStatus() {
        return reconciliationStatus;
    }
    
    public void setStatus(String status) {
        this.reconciliationStatus = status;
    }
    
    public LocalDate getCreatedDate() {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }
    
    public void setCreatedDate(LocalDate date) {
        // Handled by @CreationTimestamp
    }
    
    public LocalDate getUpdatedDate() {
        return updatedAt != null ? updatedAt.toLocalDate() : null;
    }
    
    public void setUpdatedDate(LocalDate date) {
        // Handled by @UpdateTimestamp
    }
    
    // Certificate details
    @Column(name = "certificate_number", length = 50)
    private String certificateNumber;
    
    @Column(name = "certificate_date")
    private LocalDate certificateDate;
    
    // Reconciliation
    @Column(name = "reconciliation_status", length = 50)
    @Builder.Default
    private String reconciliationStatus = "PENDING"; // PENDING, MATCHED, MISMATCHED, MISSING
    
    @Column(name = "form_26as_amount", precision = 20, scale = 2)
    private BigDecimal form26asAmount; // Amount as per Form 26AS
    
    @Column(name = "difference_amount", precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal differenceAmount = BigDecimal.ZERO; // Difference between claimed and 26AS
    
    // Matching details
    @Column(name = "is_matched_with_26as")
    @Builder.Default
    private Boolean isMatchedWith26as = false;
    
    @Column(name = "matched_on")
    private LocalDateTime matchedOn;
    
    // Claim status
    @Column(name = "is_claimed_in_itr")
    @Builder.Default
    private Boolean isClaimedInItr = false;
    
    @Column(name = "itr_acknowledgement_number", length = 50)
    private String itrAcknowledgementNumber;
    
    // Additional info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "uploaded_certificate_path", length = 500)
    private String uploadedCertificatePath;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
