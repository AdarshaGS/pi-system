package com.pisystem.modules.stocks.networth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pisystem.shared.features.FeatureFlag;
import com.pisystem.shared.features.RequiresFeature;
import com.pisystem.shared.security.AuthenticationHelper;
import com.pisystem.modules.stocks.networth.data.AssetLiabilityTemplateDTO;
import com.pisystem.modules.stocks.networth.data.NetWorthDTO;
import com.pisystem.modules.stocks.networth.service.NetWorthReadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/net-worth")
@RequiresFeature(FeatureFlag.NET_WORTH_MODULE)
@Tag(name = "Net Worth Management", description = "APIs for calculating user net worth")
public class NetWorthController {

    private final NetWorthReadService netWorthReadService;
    private final AuthenticationHelper authenticationHelper;

    public NetWorthController(NetWorthReadService netWorthReadService, AuthenticationHelper authenticationHelper) {
        this.netWorthReadService = netWorthReadService;
        this.authenticationHelper = authenticationHelper;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get net worth", description = "Calculates total assets, liabilities, and net worth for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved net worth")
    @ApiResponse(responseCode = "403", description = "Access denied - can only view own data")
    public NetWorthDTO getNetWorth(@PathVariable("userId") Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return netWorthReadService.getNetWorth(userId);
    }

    @GetMapping("/template")
    @Operation(summary = "Get asset and liability templates", description = "Returns possible types of assets and liabilities.")
    public AssetLiabilityTemplateDTO getTemplates() {
        authenticationHelper.getCurrentUserId();
        return netWorthReadService.getEntityTemplates();
    }
}
