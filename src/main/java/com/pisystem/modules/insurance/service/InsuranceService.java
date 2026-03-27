package com.pisystem.modules.insurance.service;

import java.util.List;

import com.pisystem.modules.insurance.data.Insurance;
import com.pisystem.modules.insurance.data.InsuranceClaim;
import com.pisystem.modules.insurance.data.InsurancePremium;
import com.pisystem.modules.insurance.dto.ClaimHistoryResponse;
import com.pisystem.modules.insurance.dto.CoverageAnalysisResponse;
import com.pisystem.modules.insurance.dto.FileClaimRequest;
import com.pisystem.modules.insurance.dto.PremiumHistoryResponse;
import com.pisystem.modules.insurance.dto.RecordPremiumRequest;

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
