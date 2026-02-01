package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "capital_gains_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapitalGainsTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "financial_year", nullable = false)
    private String financialYear;
    
    @Column(name = "asset_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetType assetType;
    
    @Column(name = "asset_name")
    private String assetName;
    
    @Column(name = "isin_code")
    private String isinCode;
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "sale_date")
    private LocalDate saleDate;
    
    @Column(name = "quantity")
    private BigDecimal quantity;
    
    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;
    
    @Column(name = "sale_price")
    private BigDecimal salePrice;
    
    @Column(name = "total_purchase_value")
    private BigDecimal totalPurchaseValue;
    
    @Column(name = "total_sale_value")
    private BigDecimal totalSaleValue;
    
    @Column(name = "gain_type")
    @Enumerated(EnumType.STRING)
    private CapitalGainType gainType;
    
    @Column(name = "capital_gain")
    private BigDecimal capitalGain;
    
    @Column(name = "tax_amount")
    private BigDecimal taxAmount;
    
    @Column(name = "is_grandfathered")
    private Boolean isGrandfathered; // For equity before 31-Jan-2018
    
    @Column(name = "indexation_factor")
    private BigDecimal indexationFactor;
    
    @Column(name = "indexed_cost")
    private BigDecimal indexedCost;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
}
