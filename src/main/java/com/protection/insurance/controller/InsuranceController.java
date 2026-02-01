package com.protection.insurance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.protection.insurance.data.Insurance;
import com.protection.insurance.data.InsuranceClaim;
import com.protection.insurance.data.InsurancePremium;
import com.protection.insurance.dto.ClaimHistoryResponse;
import com.protection.insurance.dto.CoverageAnalysisResponse;
import com.protection.insurance.dto.FileClaimRequest;
import com.protection.insurance.dto.PremiumHistoryResponse;
import com.protection.insurance.dto.RecordPremiumRequest;
import com.protection.insurance.service.InsuranceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/insurance")
@RequiredArgsConstructor
@Tag(name = "Insurance", description = "Insurance Management Service")
public class InsuranceController {

    private final InsuranceService insuranceService;

    @PostMapping
    @Operation(summary = "Create Insurance Policy", description = "Create Insurance Policy Details for a user")
    @ApiResponse(responseCode = "201", description = "Successfully created")
    public ResponseEntity<Insurance> createInsuranceDetails(@Valid @RequestBody Insurance insurance) {
        Insurance createdInsurance = insuranceService.createInsurancePolicy(insurance);
        return new ResponseEntity<>(createdInsurance, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all insurance policies", description = "Get all insurance policies")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Insurance>> getAllInsuranceDetails() {
        return new ResponseEntity<>(insuranceService.getAllInsurancePolicies(), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get insurance policies by user id", description = "Get insurance policies by user id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Insurance>> getInsuranceDetailsByUserId(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(insuranceService.getInsurancePoliciesByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get insurance policy by id", description = "Get insurance policy by id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<Insurance> getInsuranceDetailsById(@PathVariable("id") Long id) {
        Insurance insurance = insuranceService.getInsurancePolicyById(id);
        if (insurance != null) {
            return new ResponseEntity<>(insurance, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete insurance policy", description = "Delete insurance policy by id")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    public ResponseEntity<Void> deleteInsurancePolicy(@PathVariable("id") Long id) {
        insuranceService.deleteInsurancePolicy(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ==================== Premium Management ====================

    @PostMapping("/{id}/premium")
    @Operation(summary = "Record premium payment", description = "Record a premium payment for an insurance policy")
    @ApiResponse(responseCode = "201", description = "Premium recorded successfully")
    public ResponseEntity<InsurancePremium> recordPremium(
            @PathVariable("id") Long insuranceId,
            @Valid @RequestBody RecordPremiumRequest request) {
        InsurancePremium premium = insuranceService.recordPremium(insuranceId, request);
        return new ResponseEntity<>(premium, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/premiums")
    @Operation(summary = "Get premium history", description = "Get complete premium payment history for an insurance policy")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<PremiumHistoryResponse> getPremiumHistory(@PathVariable("id") Long insuranceId) {
        PremiumHistoryResponse response = insuranceService.getPremiumHistory(insuranceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ==================== Claims Management ====================

    @PostMapping("/{id}/claim")
    @Operation(summary = "File insurance claim", description = "File a new insurance claim")
    @ApiResponse(responseCode = "201", description = "Claim filed successfully")
    public ResponseEntity<InsuranceClaim> fileClaim(
            @PathVariable("id") Long insuranceId,
            @Valid @RequestBody FileClaimRequest request) {
        InsuranceClaim claim = insuranceService.fileClaim(insuranceId, request);
        return new ResponseEntity<>(claim, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/claims")
    @Operation(summary = "Get claim history", description = "Get complete claim history for an insurance policy")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<ClaimHistoryResponse> getClaimHistory(@PathVariable("id") Long insuranceId) {
        ClaimHistoryResponse response = insuranceService.getClaimHistory(insuranceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ==================== Coverage Analysis ====================

    @GetMapping("/user/{userId}/analysis")
    @Operation(summary = "Analyze insurance coverage", description = "Get comprehensive coverage analysis for a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<CoverageAnalysisResponse> analyzeCoverage(@PathVariable("userId") Long userId) {
        CoverageAnalysisResponse response = insuranceService.analyzeCoverage(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
