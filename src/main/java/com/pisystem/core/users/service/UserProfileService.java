package com.pisystem.core.users.service;

import com.pisystem.core.users.data.UserProfileRequest;
import com.pisystem.core.users.data.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getProfile(Long userId);

    UserProfileResponse createOrUpdateProfile(Long userId, UserProfileRequest request);

    boolean isProfileComplete(Long userId);
}
