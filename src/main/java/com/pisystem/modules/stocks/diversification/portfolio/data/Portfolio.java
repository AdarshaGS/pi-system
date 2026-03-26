package com.investments.stocks.diversification.portfolio.data;

import java.math.BigDecimal;

import com.common.data.EntityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.common.data.TypedEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Table(name = "portfolio_holdings")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio implements TypedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "stock_symbol")
    private String stockSymbol;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "profit_and_loss_percentage")
    private BigDecimal profitAndLossPercentage;

}
