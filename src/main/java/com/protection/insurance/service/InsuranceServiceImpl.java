package com.protection.insurance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.security.AuthenticationHelper;
import com.protection.insurance.data.Insurance;
import com.protection.insurance.repo.InsuranceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;
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
}
