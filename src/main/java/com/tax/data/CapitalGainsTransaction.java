package com.tax.data;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "capital_gains_transactions", indexes = {
    @Index(name = "idx_user_financial_year", columnList = "user_id, financial_year"),
    @Index(name = "idx_asset_type", columnList = "asset_type"),
    @Index(name = "idx_gain_type", columnList = "gain_type"),
    @Index(name = "idx_sale_date", columnList = "sale_date")
})
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
    
    @Column(name = "asset_type", length = 50, nullable = false)
    private String assetType; // STOCK, MUTUAL_FUND, REAL_ESTATE, GOLD, BOND
    
    @Column(name = "asset_name", nullable = false)
    private String assetName;
    
    @Column(name = "asset_symbol", length = 50)
    private String assetSymbol; // For stocks/MF - symbol/ISIN
    
    @Column(name = "quantity", nullable = false, precision = 20, scale = 4)
    private BigDecimal quantity;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "purchase_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;
    
    @Column(name = "sale_price", nullable = false, precision = 20, scale = 2)
    private BigDecimal salePrice;
    
    @Column(name = "purchase_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal purchaseValue; // Quantity * Purchase Price
    
    @Column(name = "sale_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal saleValue; // Quantity * Sale Price
    
    @Column(name = "expenses", precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal expenses = BigDecimal.ZERO; // Brokerage, STT, etc.
    
    @Column(name = "indexed_cost", precision = 20, scale = 2)
    private BigDecimal indexedCost; // For LTCG - indexed cost of acquisition
    
    @Column(name = "holding_period_days", nullable = false)
    private Integer holdingPeriodDays;
    
    @Column(name = "gain_type", length = 20, nullable = false)
    private String gainType; // STCG or LTCG
    
    @Column(name = "capital_gain", nullable = false, precision = 20, scale = 2)
    private BigDecimal capitalGain;
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate; // Applicable tax rate %
    
    @Column(name = "tax_amount", precision = 20, scale = 2)
    private BigDecimal taxAmount; // Computed tax amount
    
    @Column(name = "financial_year", length = 10, nullable = false)
    private String financialYear; // FY in which sale occurred (e.g., 2025-26)
    
    @Column(name = "is_set_off")
    @Builder.Default
    private Boolean isSetOff = false; // Whether loss is set off
    
    @Column(name = "set_off_amount", precision = 20, scale = 2)
    @Builder.Default
    private BigDecimal setOffAmount = BigDecimal.ZERO;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Backward compatibility aliases
    public BigDecimal getTotalPurchaseValue() {
        return purchaseValue;
    }
    
    public void setTotalPurchaseValue(BigDecimal value) {
        this.purchaseValue = value;
    }
    
    public BigDecimal getTotalSaleValue() {
        return saleValue;
    }
    
    public void setTotalSaleValue(BigDecimal value) {
        this.saleValue = value;
    }
    
    public LocalDate getCreatedDate() {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }
    
    public void setCreatedDate(LocalDate date) {
        // Handled by @CreationTimestamp
    }
}
