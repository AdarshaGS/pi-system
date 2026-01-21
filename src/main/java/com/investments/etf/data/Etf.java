package com.investments.etf.data;

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

import io.swagger.v3.oas.annotations.media.Schema;

@Table(name = "etfs")
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Etf implements TypedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "entity_type")
    @Builder.Default
    private EntityType entityType = EntityType.ETF;

    @Column(name = "symbol", unique = true)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price")
    private Double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_expense_ratio")
    private Double totalExpenseRatio;
}
