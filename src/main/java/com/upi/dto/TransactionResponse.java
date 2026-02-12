package com.upi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionId;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String status;
    private String remarks;
    private String category;
    private String merchantName;
    private Date createdAt;
    private Date completedAt;
    private String receiptUrl;
    private String errorMessage;
}
