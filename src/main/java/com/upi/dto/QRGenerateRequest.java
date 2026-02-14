package com.upi.dto;

import lombok.Data;

@Data
public class QRGenerateRequest {
    private String upiId;
    private Double amount;
    private String merchantName;
    private String remarks;
}