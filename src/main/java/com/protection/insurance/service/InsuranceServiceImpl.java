package com.protection.insurance.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.security.AuthenticationHelper;
import com.protection.insurance.data.ClaimStatus;
import com.protection.insurance.data.Insurance;
import com.protection.insurance.data.InsuranceClaim;
import com.protection.insurance.data.InsurancePremium;
import com.protection.insurance.data.InsuranceType;
import com.protection.insurance.data.PremiumPaymentStatus;
import com.protection.insurance.dto.ClaimHistoryResponse;
import com.protection.insurance.dto.CoverageAnalysisResponse;
import com.protection.insurance.dto.FileClaimRequest;
import com.protection.insurance.dto.PremiumHistoryResponse;
import com.protection.insurance.dto.RecordPremiumRequest;
import com.protection.insurance.repo.InsuranceClaimRepository;
import com.protection.insurance.repo.InsurancePremiumRepository;
import com.protection.insurance.repo.InsuranceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final InsurancePremiumRepository premiumRepository;
    private final InsuranceClaimRepository claimRepository;
    private final AuthenticationHelper authenticationHelper;

    @Override
    @Transactional
    public Insurance createInsurancePolicy(Insurance insurance) {
        authenticationHelper.validateUserAccess(insurance.getUserId());
        return insuranceRepository.save(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Insurance> getAllInsurancePolicies() {
        authenticationHelper.validateAdminAccess();
        return insuranceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Insurance> getInsurancePoliciesByUserId(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return insuranceRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Insurance getInsurancePolicyById(Long id) {
        Optional<Insurance> policy = insuranceRepository.findById(id);
        if (policy.isPresent()) {
            authenticationHelper.validateUserAccess(policy.get().getUserId());
        }
        return policy.orElse(null);
    }

    @Override
    @Transactional
    public void deleteInsurancePolicy(Long id) {
        Optional<Insurance> policy = insuranceRepository.findById(id);
        if (policy.isPresent()) {
            authenticationHelper.validateUserAccess(policy.get().getUserId());
            insuranceRepository.deleteById(id);
        }
    }

    // ==================== Premium Management ====================

    @Override
    @Transactional
    public InsurancePremium recordPremium(Long insuranceId, RecordPremiumRequest request) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new RuntimeException("Insurance policy not found"));
        
        authenticationHelper.validateUserAccess(insurance.getUserId());

        InsurancePremium premium = InsurancePremium.builder()
                .insuranceId(insuranceId)
                .paymentAmount(request.getPaymentAmount())
                .paymentDate(request.getPaymentDate())
                .dueDate(request.getDueDate())
                .paymentStatus(PremiumPaymentStatus.valueOf(request.getPaymentStatus()))
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .isAutoRenewal(request.getIsAutoRenewal())
                .notes(request.getNotes())
                .build();

        return premiumRepository.save(premium);
    }

    @Override
    @Transactional(readOnly = true)
    public PremiumHistoryResponse getPremiumHistory(Long insuranceId) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new RuntimeException("Insurance policy not found"));
        
        authenticationHelper.validateUserAccess(insurance.getUserId());

        List<InsurancePremium> premiums = premiumRepository.findByInsuranceIdOrderByPaymentDateDesc(insuranceId);

        long paidCount = premiums.stream()
                .filter(p -> p.getPaymentStatus() == PremiumPaymentStatus.PAID)
                .count();

        long missedCount = premiums.stream()
                .filter(p -> p.getPaymentStatus() == PremiumPaymentStatus.MISSED)
                .count();

        BigDecimal totalPaid = premiums.stream()
                .filter(p -> p.getPaymentStatus() == PremiumPaymentStatus.PAID)
                .map(InsurancePremium::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = premiums.stream()
                .filter(p -> p.getPaymentStatus() == PremiumPaymentStatus.PENDING || 
                             p.getPaymentStatus() == PremiumPaymentStatus.MISSED)
                .map(InsurancePremium::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PremiumHistoryResponse.PremiumSummary> premiumSummaries = premiums.stream()
                .map(p -> PremiumHistoryResponse.PremiumSummary.builder()
                        .id(p.getId())
                        .paymentAmount(p.getPaymentAmount())
                        .paymentDate(p.getPaymentDate().toString())
                        .dueDate(p.getDueDate() != null ? p.getDueDate().toString() : null)
                        .paymentStatus(p.getPaymentStatus().name())
                        .paymentMethod(p.getPaymentMethod())
                        .transactionReference(p.getTransactionReference())
                        .isAutoRenewal(p.getIsAutoRenewal())
                        .notes(p.getNotes())
                        .build())
                .collect(Collectors.toList());

        return PremiumHistoryResponse.builder()
                .insuranceId(insuranceId)
                .policyNumber(insurance.getPolicyNumber())
                .totalPremiumsPaid((int) paidCount)
                .missedPremiums((int) missedCount)
                .totalAmountPaid(totalPaid)
                .totalOutstanding(totalOutstanding)
                .premiums(premiumSummaries)
                .build();
    }

    // ==================== Claims Management ====================

    @Override
    @Transactional
    public InsuranceClaim fileClaim(Long insuranceId, FileClaimRequest request) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new RuntimeException("Insurance policy not found"));
        
        authenticationHelper.validateUserAccess(insurance.getUserId());

        InsuranceClaim claim = InsuranceClaim.builder()
                .insuranceId(insuranceId)
                .claimNumber(request.getClaimNumber())
                .claimAmount(request.getClaimAmount())
                .claimDate(request.getClaimDate())
                .incidentDate(request.getIncidentDate())
                .claimStatus(ClaimStatus.valueOf(request.getClaimStatus()))
                .claimType(request.getClaimType())
                .description(request.getDescription())
                .build();

        return claimRepository.save(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimHistoryResponse getClaimHistory(Long insuranceId) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new RuntimeException("Insurance policy not found"));
        
        authenticationHelper.validateUserAccess(insurance.getUserId());

        List<InsuranceClaim> claims = claimRepository.findByInsuranceIdOrderByClaimDateDesc(insuranceId);

        long approvedCount = claims.stream()
                .filter(c -> c.getClaimStatus() == ClaimStatus.APPROVED || 
                             c.getClaimStatus() == ClaimStatus.SETTLED)
                .count();

        long rejectedCount = claims.stream()
                .filter(c -> c.getClaimStatus() == ClaimStatus.REJECTED)
                .count();

        long pendingCount = claims.stream()
                .filter(c -> c.getClaimStatus() == ClaimStatus.SUBMITTED || 
                             c.getClaimStatus() == ClaimStatus.UNDER_REVIEW)
                .count();

        BigDecimal totalClaimAmount = claims.stream()
                .map(InsuranceClaim::getClaimAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalApprovedAmount = claims.stream()
                .filter(c -> c.getApprovedAmount() != null)
                .map(InsuranceClaim::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSettled = claimRepository.getTotalSettledAmount(insuranceId);
        if (totalSettled == null) {
            totalSettled = BigDecimal.ZERO;
        }

        List<ClaimHistoryResponse.ClaimSummary> claimSummaries = claims.stream()
                .map(c -> ClaimHistoryResponse.ClaimSummary.builder()
                        .id(c.getId())
                        .claimNumber(c.getClaimNumber())
                        .claimAmount(c.getClaimAmount())
                        .approvedAmount(c.getApprovedAmount())
                        .claimDate(c.getClaimDate().toString())
                        .incidentDate(c.getIncidentDate() != null ? c.getIncidentDate().toString() : null)
                        .settlementDate(c.getSettlementDate() != null ? c.getSettlementDate().toString() : null)
                        .claimStatus(c.getClaimStatus().name())
                        .claimType(c.getClaimType())
                        .description(c.getDescription())
                        .rejectionReason(c.getRejectionReason())
                        .build())
                .collect(Collectors.toList());

        return ClaimHistoryResponse.builder()
                .insuranceId(insuranceId)
                .policyNumber(insurance.getPolicyNumber())
                .totalClaims(claims.size())
                .approvedClaims((int) approvedCount)
                .rejectedClaims((int) rejectedCount)
                .pendingClaims((int) pendingCount)
                .totalClaimAmount(totalClaimAmount)
                .totalApprovedAmount(totalApprovedAmount)
                .totalSettledAmount(totalSettled)
                .claims(claimSummaries)
                .build();
    }

    // ==================== Coverage Analysis ====================

    @Override
    @Transactional(readOnly = true)
    public CoverageAnalysisResponse analyzeCoverage(Long userId) {
        authenticationHelper.validateUserAccess(userId);

        List<Insurance> policies = insuranceRepository.findByUserId(userId);

        // Separate life and health insurance
        List<Insurance> lifePolicies = policies.stream()
                .filter(p -> p.getType() == InsuranceType.LIFE || p.getType() == InsuranceType.TERM)
                .collect(Collectors.toList());

        List<Insurance> healthPolicies = policies.stream()
                .filter(p -> p.getType() == InsuranceType.HEALTH)
                .collect(Collectors.toList());

        // Calculate total coverage
        BigDecimal totalLifeCoverage = lifePolicies.stream()
                .map(Insurance::getCoverAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHealthCoverage = healthPolicies.stream()
                .map(Insurance::getCoverAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCoverage = policies.stream()
                .map(Insurance::getCoverAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAnnualPremium = policies.stream()
                .map(Insurance::getPremiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Life Insurance Analysis (rule of thumb: 10-15x annual income)
        BigDecimal assumedAnnualIncome = new BigDecimal("1000000"); // ₹10L assumption
        BigDecimal recommendedLifeCoverage = assumedAnnualIncome.multiply(new BigDecimal("10"));
        BigDecimal lifeCoverageGap = recommendedLifeCoverage.subtract(totalLifeCoverage);

        String lifeAdequacy = "ADEQUATE";
        String lifeRecommendation = "Your life insurance coverage is adequate.";
        if (lifeCoverageGap.compareTo(BigDecimal.ZERO) > 0) {
            lifeAdequacy = "INADEQUATE";
            lifeRecommendation = String.format("Consider increasing life coverage by ₹%.2fL", 
                    lifeCoverageGap.divide(new BigDecimal("100000")).doubleValue());
        } else if (lifeCoverageGap.compareTo(new BigDecimal("-5000000")) < 0) {
            lifeAdequacy = "OVER_INSURED";
            lifeRecommendation = "You may be over-insured. Review if all policies are necessary.";
        }

        CoverageAnalysisResponse.LifeInsuranceAnalysis lifeAnalysis = CoverageAnalysisResponse.LifeInsuranceAnalysis.builder()
                .currentCoverage(totalLifeCoverage)
                .recommendedCoverage(recommendedLifeCoverage)
                .coverageGap(lifeCoverageGap)
                .adequacyStatus(lifeAdequacy)
                .recommendation(lifeRecommendation)
                .annualIncome(assumedAnnualIncome)
                .multiplier(10)
                .outstandingLiabilities(BigDecimal.ZERO)
                .dependentsCount(0)
                .build();

        // Health Insurance Analysis (rule of thumb: ₹5L-10L per family in metro cities)
        BigDecimal recommendedHealthCoverage = new BigDecimal("500000"); // ₹5L recommendation
        BigDecimal healthCoverageGap = recommendedHealthCoverage.subtract(totalHealthCoverage);

        String healthAdequacy = "ADEQUATE";
        String healthRecommendation = "Your health insurance coverage is adequate.";
        if (healthCoverageGap.compareTo(BigDecimal.ZERO) > 0) {
            healthAdequacy = "INADEQUATE";
            healthRecommendation = String.format("Consider increasing health coverage by ₹%.2fL", 
                    healthCoverageGap.divide(new BigDecimal("100000")).doubleValue());
        } else if (totalHealthCoverage.compareTo(new BigDecimal("2000000")) > 0) {
            healthAdequacy = "OVER_INSURED";
            healthRecommendation = "Your health coverage is excellent.";
        }

        CoverageAnalysisResponse.HealthInsuranceAnalysis healthAnalysis = CoverageAnalysisResponse.HealthInsuranceAnalysis.builder()
                .currentCoverage(totalHealthCoverage)
                .recommendedCoverage(recommendedHealthCoverage)
                .coverageGap(healthCoverageGap)
                .adequacyStatus(healthAdequacy)
                .recommendation(healthRecommendation)
                .familySize(1)
                .city("Metro")
                .averageHospitalizationCost(new BigDecimal("300000"))
                .build();

        // Overall status
        String overallStatus = "ADEQUATE";
        String overallRecommendation = "Your insurance coverage is well-balanced.";
        if (lifeAdequacy.equals("INADEQUATE") || healthAdequacy.equals("INADEQUATE")) {
            overallStatus = "UNDER_INSURED";
            overallRecommendation = "You are under-insured. Review the recommendations above.";
        }

        return CoverageAnalysisResponse.builder()
                .userId(userId)
                .lifeInsurance(lifeAnalysis)
                .healthInsurance(healthAnalysis)
                .totalCoverAmount(totalCoverage)
                .totalAnnualPremium(totalAnnualPremium)
                .overallCoverageStatus(overallStatus)
                .recommendations(overallRecommendation)
                .build();
    }
}
