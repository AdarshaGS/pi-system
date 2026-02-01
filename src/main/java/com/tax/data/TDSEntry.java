package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tds_entries")
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
    
    @Column(name = "financial_year", nullable = false)
    private String financialYear;
    
    @Column(name = "deductor_name")
    private String deductorName;
    
    @Column(name = "deductor_tan")
    private String deductorTan;
    
    @Column(name = "tds_amount", nullable = false)
    private BigDecimal tdsAmount;
    
    @Column(name = "income_amount")
    private BigDecimal incomeAmount;
    
    @Column(name = "tds_section")
    private String tdsSection; // 194A, 194J, etc.
    
    @Column(name = "deduction_date")
    private LocalDate deductionDate;
    
    @Column(name = "certificate_number")
    private String certificateNumber;
    
    @Column(name = "challan_number")
    private String challanNumber;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TDSStatus status;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
    
    @Column(name = "updated_date")
    private LocalDate updatedDate;
}
