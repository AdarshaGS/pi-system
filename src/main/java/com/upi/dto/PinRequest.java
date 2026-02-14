package com.upi.dto;

import lombok.Data;

@Data
public class PinRequest {
    private String userId;
    private String pin;
    private String oldPin;
    private String confirmPin;
}