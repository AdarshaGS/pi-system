package com.pisystem.core.users.service;

import com.pisystem.core.users.data.*;
import com.pisystem.core.users.repo.UserProfileRepository;
import com.pisystem.core.users.repo.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        // Find or create a default blank profile if it doesn't exist
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).build());

        return mapToResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse createOrUpdateProfile(Long userId, UserProfileRequest request) {
        // Validate user exists
        if (!usersRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().userId(userId).build());

        // Update fields
        if (request.getDateOfBirth() != null)
            profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getDependents() != null)
            profile.setDependents(request.getDependents());
        if (request.getCity() != null)
            profile.setCity(request.getCity());
        if (request.getCityTier() != null)
            profile.setCityTier(request.getCityTier());
        if (request.getAnnualIncome() != null)
            profile.setAnnualIncome(request.getAnnualIncome());
        if (request.getMonthlyIncome() != null)
            profile.setMonthlyIncome(request.getMonthlyIncome());
        if (request.getEmploymentType() != null)
            profile.setEmploymentType(request.getEmploymentType());
        if (request.getEmergencyFundMonths() != null)
            profile.setEmergencyFundMonths(request.getEmergencyFundMonths());
        if (request.getRiskTolerance() != null)
            profile.setRiskTolerance(request.getRiskTolerance());
        if (request.getRetirementAge() != null)
            profile.setRetirementAge(request.getRetirementAge());
        if (request.getLifeExpectancy() != null)
            profile.setLifeExpectancy(request.getLifeExpectancy());

        // Check if profile is now complete (minimal required fields)
        boolean isComplete = profile.getAnnualIncome() != null &&
                profile.getDateOfBirth() != null &&
                profile.getEmploymentType() != null;
        profile.setIsProfileComplete(isComplete);

        profile = profileRepository.save(profile);
        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProfileComplete(Long userId) {
        return profileRepository.findByUserId(userId)
                .map(UserProfile::getIsProfileComplete)
                .orElse(false);
    }

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .dateOfBirth(profile.getDateOfBirth())
                .age(profile.getAge())
                .dependents(profile.getDependents())
                .city(profile.getCity())
                .cityTier(profile.getCityTier())
                .annualIncome(profile.getAnnualIncome())
                .monthlyIncome(profile.getMonthlyIncome())
                .effectiveMonthlyIncome(profile.getEffectiveMonthlyIncome())
                .employmentType(profile.getEmploymentType())
                .emergencyFundMonths(profile.getEmergencyFundMonths())
                .riskTolerance(profile.getRiskTolerance())
                .retirementAge(profile.getRetirementAge())
                .lifeExpectancy(profile.getLifeExpectancy())
                .isProfileComplete(profile.getIsProfileComplete())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
