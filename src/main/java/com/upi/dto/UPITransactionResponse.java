package com.upi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UPITransactionResponse {
    private String status;
    private String message;
    private Long transactionId; // For sendMoney, acceptRequest
    private Long requestId;     // For requestMoney
}