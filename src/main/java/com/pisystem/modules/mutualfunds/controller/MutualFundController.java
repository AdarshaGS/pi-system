package com.investments.mutualfunds.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.investments.mutualfunds.data.MutualFundHolding;
import com.investments.mutualfunds.data.MutualFundInsights;
import com.investments.mutualfunds.data.MutualFundSummary;
import com.investments.mutualfunds.service.MutualFundService;
import com.externalServices.mutualfund.dto.MFLatestNAVResponse;
import com.externalServices.mutualfund.dto.MFNAVHistoryResponse;
import com.externalServices.mutualfund.dto.MFSchemeListItem;
import com.externalServices.mutualfund.dto.MFSchemeSearchResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mutual-funds")
@RequiredArgsConstructor
@Tag(name = "Mutual Fund Management", description = "APIs for Mutual Fund Portfolio Analytics and External Data")
public class MutualFundController {

    private final MutualFundService mutualFundService;

    // ========== Portfolio Management APIs ==========
    
    @Operation(summary = "Get Mutual Fund Portfolio Summary")
    @GetMapping("/summary")
    public ResponseEntity<MutualFundSummary> getSummary(@RequestParam Long userId) {
        return ResponseEntity.ok(mutualFundService.getSummary(userId));
    }

    @Operation(summary = "Get Mutual Fund Holdings")
    @GetMapping("/holdings")
    public ResponseEntity<List<MutualFundHolding>> getHoldings(@RequestParam Long userId) {
        return ResponseEntity.ok(mutualFundService.getHoldings(userId));
    }

    @Operation(summary = "Get Mutual Fund Insights")
    @GetMapping("/insights")
    public ResponseEntity<MutualFundInsights> getInsights(@RequestParam Long userId) {
        return ResponseEntity.ok(mutualFundService.getInsights(userId));
    }
    
    // ========== External API - Scheme Discovery ==========
    
    @GetMapping("/external/search")
    @Operation(
        summary = "Search mutual fund schemes",
        description = "Search for mutual fund schemes by name from external API (mfapi.in)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
        @ApiResponse(responseCode = "400", description = "Invalid search query"),
        @ApiResponse(responseCode = "500", description = "Internal server error or external API failure")
    })
    public ResponseEntity<List<MFSchemeSearchResult>> searchSchemes(
            @Parameter(description = "Search query (e.g., 'HDFC', 'SBI', 'Axis')", required = true, example = "HDFC")
            @RequestParam("arg0") String query) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<MFSchemeSearchResult> results = mutualFundService.searchSchemes(query.trim());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/external/schemes")
    @Operation(
        summary = "List all mutual fund schemes",
        description = "Fetch a paginated list of all available mutual fund schemes from external API"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved schemes list"),
        @ApiResponse(responseCode = "500", description = "Internal server error or external API failure")
    })
    public ResponseEntity<List<MFSchemeListItem>> listAllSchemes(
            @Parameter(description = "Number of results per page (max: 500)", example = "100")
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            
            @Parameter(description = "Pagination offset", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer offset) {
        
        List<MFSchemeListItem> schemes = mutualFundService.listAllSchemes(limit, offset);
        return ResponseEntity.ok(schemes);
    }
    
    // ========== External API - NAV Data ==========

    @GetMapping("/external/schemes/{schemeCode}/nav")
    @Operation(
        summary = "Get NAV history for a scheme",
        description = "Fetch NAV history for a specific mutual fund scheme from external API. " +
                      "Optionally filter by date range using startDate and endDate parameters."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved NAV history"),
        @ApiResponse(responseCode = "404", description = "Scheme not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error or external API failure")
    })
    public ResponseEntity<MFNAVHistoryResponse> getNAVHistory(
            @Parameter(description = "Unique scheme code", required = true, example = "125497")
            @PathVariable("schemeCode") Long schemeCode,
            
            @Parameter(description = "Start date in YYYY-MM-DD format", example = "2023-01-01")
            @RequestParam(required = false) String startDate,
            
            @Parameter(description = "End date in YYYY-MM-DD format", example = "2023-12-31")
            @RequestParam(required = false) String endDate) {
        
        MFNAVHistoryResponse response;
        
        if (startDate != null && endDate != null) {
            response = mutualFundService.getNAVHistory(schemeCode, startDate, endDate);
        } else {
            response = mutualFundService.getNAVHistory(schemeCode);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/schemes/{schemeCode}/latest")
    @Operation(
        summary = "Get latest NAV for a scheme",
        description = "Fetch the most recent NAV for a specific mutual fund scheme from external API with metadata."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved latest NAV"),
        @ApiResponse(responseCode = "404", description = "Scheme not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error or external API failure")
    })
    public ResponseEntity<MFLatestNAVResponse> getLatestNAV(
            @Parameter(description = "Unique scheme code", required = true, example = "125497")
            @PathVariable("schemeCode") Long schemeCode) {
        
        MFLatestNAVResponse response = mutualFundService.getLatestNAV(schemeCode);
        return ResponseEntity.ok(response);
    }
}
