package com.investments.stocks.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "corporate_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorporateAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, length = 50)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private CorporateActionType actionType;

    @Column(name = "announcement_date")
    private LocalDate announcementDate;

    @Column(name = "ex_date")
    private LocalDate exDate;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "dividend_amount", precision = 10, scale = 2)
    private BigDecimal dividendAmount;

    @Column(name = "split_ratio", length = 20)
    private String splitRatio;

    @Column(name = "bonus_ratio", length = 20)
    private String bonusRatio;

    @Column(name = "rights_ratio", length = 20)
    private String rightsRatio;

    @Column(name = "rights_price", precision = 15, scale = 2)
    private BigDecimal rightsPrice;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
