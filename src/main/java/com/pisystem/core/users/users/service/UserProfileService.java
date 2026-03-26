package com.users.service;

import com.users.data.UserProfileRequest;
import com.users.data.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getProfile(Long userId);

    UserProfileResponse createOrUpdateProfile(Long userId, UserProfileRequest request);

    boolean isProfileComplete(Long userId);
}
