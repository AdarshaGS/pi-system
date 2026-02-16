package com.upi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountLinkResponse {
    private Long bankAccountId;
    private String status;
    private String message;
}