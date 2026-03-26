package com.tax.controller;

import com.tax.dto.*;
import com.tax.service.TaxCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for advanced tax calculations
 * Handles house property, business income, loss set-off, and complete tax computation
 */
@RestController
@Tag(name = "Advanced Tax Calculations", description = "Calculate house property income, business income, loss set-off, and complete tax with surcharge")
@RequestMapping("api/v1/tax/calculations")
@RequiredArgsConstructor
public class TaxCalculationController {

    private final TaxCalculationService taxCalculationService;

    // ========== House Property Income ==========

    @PostMapping("/house-property")
    @Operation(
        summary = "Calculate house property income",
        description = "Calculate income from house property (self-occupied, let-out, or deemed let-out) with standard deduction and interest"
    )
    public ResponseEntity<HousePropertyIncomeDTO> calculateHousePropertyIncome(
            @Valid @RequestBody HousePropertyIncomeDTO input) {
        HousePropertyIncomeDTO result = taxCalculationService.calculateHousePropertyIncome(input);
        return ResponseEntity.ok(result);
    }

    // ========== Business Income ==========

    @PostMapping("/business-income")
    @Operation(
        summary = "Calculate business income",
        description = "Calculate business income under normal taxation or presumptive taxation (44AD, 44ADA, 44AE)"
    )
    public ResponseEntity<BusinessIncomeDTO> calculateBusinessIncome(
            @Valid @RequestBody BusinessIncomeDTO input) {
        BusinessIncomeDTO result = taxCalculationService.calculateBusinessIncome(input);
        return ResponseEntity.ok(result);
    }

    // ========== Loss Set-Off ==========

    @PostMapping("/loss-setoff")
    @Operation(
        summary = "Process loss set-off and carry forward",
        description = "Calculate inter-head and intra-head set-off of losses (house property, business, capital gains)"
    )
    public ResponseEntity<LossSetOffDTO> processLossSetOff(
            @Valid @RequestBody LossSetOffDTO input) {
        LossSetOffDTO result = taxCalculationService.processLossSetOff(input);
        return ResponseEntity.ok(result);
    }

    // ========== Complete Tax Computation ==========

    @PostMapping("/complete-tax")
    @Operation(
        summary = "Calculate complete tax liability",
        description = "Calculate total tax with surcharge, cess, and rebate under Section 87A"
    )
    public ResponseEntity<TaxComputationDTO> calculateCompleteTax(
            @Valid @RequestBody TaxComputationDTO input) {
        TaxComputationDTO result = taxCalculationService.calculateCompleteTax(input);
        return ResponseEntity.ok(result);
    }

    // ========== Individual Tax Calculations ==========

    @GetMapping("/rebate-87a")
    @Operation(
        summary = "Calculate rebate under Section 87A",
        description = "Calculate tax rebate (₹12,500 if income ≤ ₹5L for old regime or ≤ ₹7L for new regime)"
    )
    public ResponseEntity<BigDecimal> calculateRebate87A(
            @RequestParam("totalIncome") BigDecimal totalIncome,
            @RequestParam(defaultValue = "OLD") String regime) {
        BigDecimal rebate = taxCalculationService.calculateRebate87A(totalIncome, regime);
        return ResponseEntity.ok(rebate);
    }

    @GetMapping("/surcharge")
    @Operation(
        summary = "Calculate surcharge",
        description = "Calculate surcharge based on income slabs (10%: ₹50L-1Cr, 15%: ₹1-2Cr, 25%: ₹2-5Cr, 37%: >₹5Cr)"
    )
    public ResponseEntity<BigDecimal> calculateSurcharge(
            @RequestParam("taxAmount") BigDecimal taxAmount,
            @RequestParam("totalIncome") BigDecimal totalIncome) {
        BigDecimal surcharge = taxCalculationService.calculateSurcharge(taxAmount, totalIncome);
        return ResponseEntity.ok(surcharge);
    }

    @GetMapping("/cess")
    @Operation(
        summary = "Calculate Health and Education Cess",
        description = "Calculate cess at 4% of (tax + surcharge)"
    )
    public ResponseEntity<BigDecimal> calculateHealthEducationCess(
            @RequestParam("taxAfterSurcharge") BigDecimal taxAfterSurcharge) {
        BigDecimal cess = taxCalculationService.calculateHealthEducationCess(taxAfterSurcharge);
        return ResponseEntity.ok(cess);
    }
}
