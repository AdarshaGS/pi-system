package com.investments.stocks.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateStockRequest {

    @Schema(description = "Stock Symbol (e.g., RELIANCE)", example = "RELIANCE")
    private String symbol;

    @Schema(description = "Company Name", example = "Reliance Industries Ltd")
    private String companyName;

    @Schema(description = "Current Price", example = "2500.50")
    private Double price;

    @Schema(description = "Stock Description", example = "A conglomerate company...")
    private String description;

    @Schema(description = "Sector ID", example = "1")
    private Long sectorId;

    @Schema(description = "Market Cap", example = "15000000.00")
    private Double marketCap;

    @Schema(description = "User ID", example = "1")
    private Long userId;
}
