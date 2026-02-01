package com.loan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.loan.data.PaymentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Payment date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    private String paymentMethod;
    
    private String transactionReference;
    
    private String notes;
}
