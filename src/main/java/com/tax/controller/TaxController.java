package com.tax.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.tax.data.*;
import com.tax.service.TaxService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "Income Tax Management", description = "Comprehensive tax planning, calculation and ITR assistance")
@RequestMapping("api/v1/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;

    // ========== Basic Tax Management ==========

    @PostMapping
    @Operation(summary = "Create tax details", description = "Create or update tax details for a financial year")
    public TaxDTO createTaxDetails(@Valid @RequestBody Tax tax) {
        return this.taxService.createTaxDetails(tax);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get tax details", description = "Get tax details by user and financial year")
    public TaxDTO getTaxDetails(@PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return this.taxService.getTaxDetailsByUserId(userId, financialYear);
    }

    @GetMapping("/{userId}/liability")
    @Operation(summary = "Get outstanding tax liability", description = "Calculate total outstanding tax liability across all years")
    public BigDecimal getOutstandingTaxLiability(@PathVariable("userId") Long userId) {
        return this.taxService.getOutstandingTaxLiability(userId);
    }

    // ========== Tax Regime Comparison ==========

    @GetMapping("/{userId}/regime-comparison")
    @Operation(summary = "Compare tax regimes", description = "Compare old vs new tax regime and get recommendations based on income and deductions")
    public ResponseEntity<TaxRegimeComparisonDTO> compareTaxRegimes(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear,
            @RequestParam("grossIncome") BigDecimal grossIncome) {
        return ResponseEntity.ok(taxService.compareTaxRegimes(userId, financialYear, grossIncome));
    }

    // ========== Capital Gains Management ==========

    @PostMapping("/{userId}/capital-gains")
    @Operation(summary = "Record capital gains transaction", description = "Record a capital gains transaction and auto-calculate STCG/LTCG tax")
    public ResponseEntity<CapitalGainsTransaction> recordCapitalGain(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody CapitalGainsTransaction transaction) {
        transaction.setUserId(userId);
        return ResponseEntity.ok(taxService.recordCapitalGain(transaction));
    }

    @GetMapping("/{userId}/capital-gains/summary")
    @Operation(summary = "Get capital gains summary", description = "Get comprehensive capital gains summary with STCG/LTCG breakdown")
    public ResponseEntity<CapitalGainsSummaryDTO> getCapitalGainsSummary(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getCapitalGainsSummary(userId, financialYear));
    }

    @GetMapping("/{userId}/capital-gains/transactions")
    @Operation(summary = "Get capital gains transactions", description = "List all capital gains transactions for a financial year")
    public ResponseEntity<List<CapitalGainsTransaction>> getCapitalGainsTransactions(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getCapitalGainsTransactions(userId, financialYear));
    }

    @PostMapping("/capital-gains/calculate")
    @Operation(summary = "Calculate capital gains", description = "Calculate capital gains and tax for a transaction (preview without saving)")
    public ResponseEntity<CapitalGainsTransaction> calculateCapitalGains(
            @Valid @RequestBody CapitalGainsTransaction transaction) {
        return ResponseEntity.ok(taxService.calculateCapitalGains(transaction));
    }

    // ========== Tax Saving Recommendations ==========

    @GetMapping("/{userId}/recommendations")
    @Operation(summary = "Get tax saving recommendations", description = "Get personalized tax saving recommendations based on income and current investments")
    public ResponseEntity<TaxSavingRecommendationDTO> getTaxSavingRecommendations(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getTaxSavingRecommendations(userId, financialYear));
    }

    @PostMapping("/{userId}/tax-savings")
    @Operation(summary = "Record tax saving investment", description = "Record a tax saving investment (80C, 80D, etc.)")
    public ResponseEntity<TaxSavingInvestment> recordTaxSavingInvestment(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TaxSavingInvestment investment) {
        investment.setUserId(userId);
        return ResponseEntity.ok(taxService.recordTaxSavingInvestment(investment));
    }

    @GetMapping("/{userId}/tax-savings")
    @Operation(summary = "Get tax saving investments", description = "List all tax saving investments for a financial year")
    public ResponseEntity<List<TaxSavingInvestment>> getTaxSavingInvestments(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getTaxSavingInvestments(userId, financialYear));
    }

    // ========== TDS Tracking & Reconciliation ==========

    @PostMapping("/{userId}/tds")
    @Operation(summary = "Record TDS entry", description = "Record a TDS deduction entry with deductor details")
    public ResponseEntity<TDSEntry> recordTDSEntry(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody TDSEntry tdsEntry) {
        tdsEntry.setUserId(userId);
        return ResponseEntity.ok(taxService.recordTDSEntry(tdsEntry));
    }

    @GetMapping("/{userId}/tds")
    @Operation(summary = "Get TDS entries", description = "List all TDS entries for a financial year")
    public ResponseEntity<List<TDSEntry>> getTDSEntries(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getTDSEntries(userId, financialYear));
    }

    @GetMapping("/{userId}/tds/reconciliation")
    @Operation(summary = "Get TDS reconciliation", description = "Get TDS reconciliation report with verification status and recommendations")
    public ResponseEntity<TDSReconciliationDTO> getTDSReconciliation(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getTDSReconciliation(userId, financialYear));
    }

    @PutMapping("/tds/{tdsId}/status")
    @Operation(summary = "Update TDS status", description = "Update the verification status of a TDS entry")
    public ResponseEntity<TDSEntry> updateTDSStatus(
            @PathVariable("tdsId") Long tdsId,
            @RequestParam("status") String status) {
        return ResponseEntity.ok(taxService.updateTDSStatus(tdsId, status));
    }

    // ========== Tax Projections ==========

    @GetMapping("/{userId}/projection")
    @Operation(summary = "Get tax projection", description = "Get tax projection for current financial year with month-wise recommendations")
    public ResponseEntity<TaxProjectionDTO> getTaxProjection(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getTaxProjection(userId, financialYear));
    }

    // ========== ITR Pre-fill Data Export ==========

    @GetMapping("/{userId}/itr-prefill")
    @Operation(summary = "Get ITR pre-fill data", description = "Export comprehensive data for ITR filing with all income, deductions, and tax details")
    public ResponseEntity<ITRPreFillDataDTO> getITRPreFillData(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        return ResponseEntity.ok(taxService.getITRPreFillData(userId, financialYear));
    }
}