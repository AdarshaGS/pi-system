package com.investments.stocks.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockDTO {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String description;
    private Long sectorId;
    private BigDecimal marketCap;
}
