package com.upi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMoneyRequest {

    @NotBlank(message = "Sender UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$", message = "Invalid UPI ID format")
    private String senderUpiId;

    @NotBlank(message = "Receiver UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$", message = "Invalid UPI ID format")
    private String receiverUpiId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least ₹1")
    private BigDecimal amount;

    @NotBlank(message = "UPI PIN is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
    private String pin;

    private String remarks;
}
