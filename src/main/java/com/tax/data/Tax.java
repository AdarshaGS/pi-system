package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tax_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "financial_year", nullable = false)
    private String financialYear;
    
    @Column(name = "capital_gains_short_term")
    private BigDecimal capitalGainsShortTerm;
    
    @Column(name = "capital_gains_long_term")
    private BigDecimal capitalGainsLongTerm;
    
    @Column(name = "dividend_income")
    private BigDecimal dividendIncome;
    
    @Column(name = "tax_paid")
    private BigDecimal taxPaid;
    
    @Column(name = "tax_payable")
    private BigDecimal taxPayable;
    
    @Column(name = "gross_salary")
    private BigDecimal grossSalary;
    
    @Column(name = "standard_deduction")
    private BigDecimal standardDeduction;
    
    @Column(name = "section_80c_deductions")
    private BigDecimal section80CDeductions;
    
    @Column(name = "section_80d_deductions")
    private BigDecimal section80DDeductions;
    
    @Column(name = "other_deductions")
    private BigDecimal otherDeductions;
    
    @Column(name = "house_property_income")
    private BigDecimal housePropertyIncome;
    
    @Column(name = "business_income")
    private BigDecimal businessIncome;
    
    @Column(name = "other_income")
    private BigDecimal otherIncome;
    
    @Column(name = "tds_deducted")
    private BigDecimal tdsDeducted;
    
    @Column(name = "advance_tax_paid")
    private BigDecimal advanceTaxPaid;
    
    @Column(name = "self_assessment_tax")
    private BigDecimal selfAssessmentTax;
    
    @Column(name = "selected_regime")
    @Enumerated(EnumType.STRING)
    private TaxRegime selectedRegime;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
    
    @Column(name = "updated_date")
    private LocalDate updatedDate;
}