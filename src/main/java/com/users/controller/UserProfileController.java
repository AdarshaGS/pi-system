package com.users.controller;

import com.common.security.AuthenticationHelper;
import com.users.data.UserProfileRequest;
import com.users.data.UserProfileResponse;
import com.users.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Management of user financial and personal profile")
public class UserProfileController {

    private final UserProfileService profileService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get user profile", description = "Retrieves the financial and personal profile for a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved profile")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PostMapping("/{userId}/profile")
    @Operation(summary = "Create or update user profile", description = "Sets or updates financial settings like income, dependents, and risk tolerance")
    @ApiResponse(responseCode = "200", description = "Successfully updated profile")
    public ResponseEntity<UserProfileResponse> createOrUpdateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileRequest request) {
        authenticationHelper.validateUserAccess(userId);
        return ResponseEntity.ok(profileService.createOrUpdateProfile(userId, request));
    }
}
