package com.upi.dto;

import lombok.Data;

@Data
public class BankAccountLinkRequest {
    private String userId;
    private String accountNumber;
    private String ifscCode;
    private String bankName;
}