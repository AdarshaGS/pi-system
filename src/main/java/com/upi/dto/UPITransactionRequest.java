package com.upi.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UPITransactionRequest {
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String pin;
    private String remarks = "";
}