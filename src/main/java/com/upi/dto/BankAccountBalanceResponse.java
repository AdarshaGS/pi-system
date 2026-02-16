package com.upi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BankAccountBalanceResponse {
    private BigDecimal balance;
    private String status;
    private String message;
}