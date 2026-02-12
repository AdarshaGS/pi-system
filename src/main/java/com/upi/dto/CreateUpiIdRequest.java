package com.upi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUpiIdRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$", message = "Invalid UPI ID format")
    private String upiId;
}
