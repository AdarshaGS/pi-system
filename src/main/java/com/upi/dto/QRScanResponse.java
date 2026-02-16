package com.upi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QRScanResponse {
    private String upiId;
    private BigDecimal amount;
    private String remarks;
    private String status;
    private String message;
}