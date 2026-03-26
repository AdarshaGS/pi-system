package com.investments.stocks.diversification.portfolio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.common.features.FeatureFlag;
import com.common.features.RequiresFeature;
import com.common.security.AuthenticationHelper;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.service.PortfolioReadService;
import com.investments.stocks.diversification.portfolio.service.PortfolioWriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiresFeature(FeatureFlag.INVESTMENTS_MODULE)
@Tag(name = "Portfolio Management", description = "APIs for managing and analyzing portfolios")
public class PortfolioController {

    private final PortfolioWriteService portfolioWriteService;
    private final PortfolioReadService portfolioReadService;
    private final AuthenticationHelper authenticationHelper;

    public PortfolioController(final PortfolioWriteService portfolioWriteService,
            final PortfolioReadService portfolioReadService,
            final AuthenticationHelper authenticationHelper) {
        this.portfolioWriteService = portfolioWriteService;
        this.portfolioReadService = portfolioReadService;
        this.authenticationHelper = authenticationHelper;
    }

    @PostMapping()
    @Operation(summary = "Add portfolio item", description = "Adds a stock to the user's portfolio.")
    @ApiResponse(responseCode = "200", description = "Successfully added portfolio item")
    public Portfolio postPortfolioData(@Valid @RequestBody Portfolio portfolio) {
        authenticationHelper.validateUserAccess(portfolio.getUserId());
        return this.portfolioWriteService.addPortfolio(portfolio);
    }

    @GetMapping("/summary/{userId}")
    @Operation(summary = "Get portfolio summary", description = "Returns a comprehensive summary including investment, value, and analysis.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved portfolio summary")
    public PortfolioDTOResponse getPortfolioSummary(@PathVariable("userId") Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return this.portfolioReadService.getPortfolioSummary(userId);
    }

}
