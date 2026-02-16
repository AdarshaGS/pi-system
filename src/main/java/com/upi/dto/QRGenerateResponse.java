package com.upi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QRGenerateResponse {
    private String status;
    private String message;
    private String qrData;
    private String upiId;
    private BigDecimal amount;
    private String merchantName;
    private String remarks;
}