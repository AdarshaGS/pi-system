package com.savings.data;

import java.math.BigDecimal;

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

@Entity
@Table(name = "savings_account_details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsAccount implements TypedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long Id;

    @Column(name = "entity_type")
    @Builder.Default
    private EntityType entityType = EntityType.SAVINGS_ACCOUNT;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "amount")
    private BigDecimal amount;

}
