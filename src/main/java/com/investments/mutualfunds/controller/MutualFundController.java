package com.investments.mutualfunds.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.investments.mutualfunds.data.MutualFundHolding;
import com.investments.mutualfunds.data.MutualFundInsights;
import com.investments.mutualfunds.data.MutualFundSummary;
import com.investments.mutualfunds.service.MutualFundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mutual-funds")
@RequiredArgsConstructor
@Tag(name = "Mutual Fund Portfolio", description = "Read-only APIs for Mutual Fund Portfolio Analytics")
@PreAuthorize("isAuthenticated()")
public class MutualFundController {

    private final MutualFundService mutualFundService;

    @Operation(summary = "Get Mutual Fund Portfolio Summary")
    @GetMapping("/summary")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public ResponseEntity<MutualFundSummary> getSummary(@RequestParam Long userId) {
        return ResponseEntity.ok(mutualFundService.getSummary(userId));
    }

    @Operation(summary = "Get Mutual Fund Holdings")
    @GetMapping("/holdings")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public ResponseEntity<List<MutualFundHolding>> getHoldings(@RequestParam Long userId) {
        return ResponseEntity.ok(mutualFundService.getHoldings(userId));
    }

    @Operation(summary = "Get Mutual Fund Insights")
    @GetMapping("/insights")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public ResponseEntity<MutualFundInsights> getInsights(@RequestParam Long userId) {
        return ResponseEntity.ok(mutualFundService.getInsights(userId));
    }
}
