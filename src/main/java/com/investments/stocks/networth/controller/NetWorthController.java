package com.investments.stocks.networth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investments.stocks.networth.data.AssetLiabilityTemplateDTO;
import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.service.NetWorthReadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/net-worth")
@Tag(name = "Net Worth Management", description = "APIs for calculating user net worth")
@PreAuthorize("isAuthenticated()")
public class NetWorthController {

    private final NetWorthReadService netWorthReadService;

    public NetWorthController(NetWorthReadService netWorthReadService) {
        this.netWorthReadService = netWorthReadService;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get net worth", description = "Calculates total assets, liabilities, and net worth.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved net worth")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public NetWorthDTO getNetWorth(@PathVariable("userId") Long userId) {
        return netWorthReadService.getNetWorth(userId);
    }

    @GetMapping("/template")
    @Operation(summary = "Get asset and liability templates", description = "Returns possible types of assets and liabilities.")
    public AssetLiabilityTemplateDTO getTemplates() {
        return netWorthReadService.getEntityTemplates();
    }
}
