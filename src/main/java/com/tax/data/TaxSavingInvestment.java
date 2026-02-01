package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tax_saving_investments")
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
    
    @Column(name = "financial_year", nullable = false)
    private String financialYear;
    
    @Column(name = "section", nullable = false)
    @Enumerated(EnumType.STRING)
    private TaxSavingSection section;
    
    @Column(name = "investment_name")
    private String investmentName;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "investment_date")
    private LocalDate investmentDate;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "is_claimed")
    private Boolean isClaimed;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
}
