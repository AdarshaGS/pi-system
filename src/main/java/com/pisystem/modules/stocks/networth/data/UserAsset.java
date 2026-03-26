package com.investments.stocks.networth.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

import com.common.data.EntityType;
import com.common.data.TypedEntity;

@Entity
@Table(name = "user_assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAsset implements TypedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "asset_type", nullable = false)
    private EntityType entityType;

    @Column(name = "reference_symbol")
    private String referenceSymbol;

    @Column(nullable = false)
    private String name;

    private BigDecimal quantity;

    @Column(name = "current_value", nullable = false)
    private BigDecimal currentValue;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Override
    public EntityType getEntityType() {
        return entityType;
    }
}
