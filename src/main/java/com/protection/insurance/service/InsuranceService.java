package com.protection.insurance.service;

import java.util.List;

import com.protection.insurance.data.Insurance;
import com.protection.insurance.data.InsuranceClaim;
import com.protection.insurance.data.InsurancePremium;
import com.protection.insurance.dto.ClaimHistoryResponse;
import com.protection.insurance.dto.CoverageAnalysisResponse;
import com.protection.insurance.dto.FileClaimRequest;
import com.protection.insurance.dto.PremiumHistoryResponse;
import com.protection.insurance.dto.RecordPremiumRequest;

public interface InsuranceService {

    Insurance createInsurancePolicy(Insurance insurance);

    List<Insurance> getAllInsurancePolicies();

    List<Insurance> getInsurancePoliciesByUserId(Long userId);

    Insurance getInsurancePolicyById(Long id);

    void deleteInsurancePolicy(Long id);

    // Premium Management
    InsurancePremium recordPremium(Long insuranceId, RecordPremiumRequest request);

    PremiumHistoryResponse getPremiumHistory(Long insuranceId);

    // Claims Management
    InsuranceClaim fileClaim(Long insuranceId, FileClaimRequest request);

    ClaimHistoryResponse getClaimHistory(Long insuranceId);

    // Coverage Analysis
    CoverageAnalysisResponse analyzeCoverage(Long userId);
}
