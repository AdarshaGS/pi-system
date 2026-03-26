package com.payments.upi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UPIRequest {
    private String payerVPA;
    private String payeeVPA;
    private double amount;
    private String remarks;
}
