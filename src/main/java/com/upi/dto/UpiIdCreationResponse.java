package com.upi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpiIdCreationResponse {
    private String upiId;
    private String status;
    private String message;
}