package com.upi.model;

import com.users.data.Users;
import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank_accounts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NotNull
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    private String accountNumber;
    private String ifscCode;
    private String bankName;
    
    @Builder.Default
    private Boolean isPrimary = false;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
