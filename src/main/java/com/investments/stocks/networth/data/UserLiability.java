package com.investments.stocks.networth.data;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@Table(name = "user_liabilities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLiability implements TypedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "liability_type", nullable = false)
    private EntityType entityType;

    @Column(nullable = false)
    private String name;

    @Column(name = "outstanding_amount", nullable = false)
    private BigDecimal outstandingAmount;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Override
    public EntityType getEntityType() {
        return entityType;
    }
}
