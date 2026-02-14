package com.upi.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UPICollectRequest {
    private String requesterUpiId;
    private String payerUpiId;
    private BigDecimal amount;
    private String remarks = "";
}