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
    
    @Column(name = "created_date")
    private LocalDate createdDate;
}