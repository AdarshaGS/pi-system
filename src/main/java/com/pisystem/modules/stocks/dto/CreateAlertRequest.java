package com.investments.stocks.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.investments.stocks.data.AlertType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    private BigDecimal targetPrice;
    private BigDecimal percentageChange;
}
