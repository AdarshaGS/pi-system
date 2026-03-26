package com.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.admin.data.UserAdminDTO;
import com.common.exception.data.CriticalLog;
import com.common.exception.repo.CriticalLogRepository;
import com.common.security.AuthenticationHelper;
import com.users.data.Users;
import com.users.service.UserReadService;
import com.users.service.UserWriteService;
import com.auth.data.Role;
import com.auth.repo.RoleRepository;
import com.audit.data.UserActivityLog;
import com.audit.repo.UserActivityLogRepository;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Portal", description = "Admin portal for managing users and viewing system data")
public class AdminController {

    private final AuthenticationHelper authenticationHelper;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final CriticalLogRepository criticalLogRepository;
    private final RoleRepository roleRepository;
    private final UserActivityLogRepository activityLogRepository;

    public AdminController(AuthenticationHelper authenticationHelper,
            UserReadService userReadService,
            UserWriteService userWriteService,
            CriticalLogRepository criticalLogRepository,
            RoleRepository roleRepository,
            UserActivityLogRepository activityLogRepository) {
        this.authenticationHelper = authenticationHelper;
        this.userReadService = userReadService;
        this.userWriteService = userWriteService;
        this.criticalLogRepository = criticalLogRepository;
        this.roleRepository = roleRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @Hidden
    @GetMapping("/internal/health")
    public String internalHealth() {
        return "OK";
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard", description = "Only Accessible by ROLE_ADMIN or ROLE_SUPER_ADMIN")
    public ResponseEntity<?> getAdminStats() {
        authenticationHelper.validateAdminAccess();
        return ResponseEntity.ok(Map.of(
                "status", "Healthy",
                "message", "Welcome to Admin Dashboard",
                "activeJobs", 0));
    }

    // User Management Endpoints

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users with optional search, filter, and pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all users")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        authenticationHelper.validateAdminAccess();

        List<Users> allUsers = userReadService.getAllUsers();

        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allUsers = allUsers.stream()
                    .filter(user -> user.getName().toLowerCase().contains(searchLower)
                            || user.getEmail().toLowerCase().contains(searchLower)
                            || (user.getMobileNumber() != null
                                    && user.getMobileNumber().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Apply role filter
        if (role != null && !role.trim().isEmpty()) {
            allUsers = allUsers.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(r -> r.getName().equals(role)))
                    .collect(Collectors.toList());
        }

        // Calculate pagination
        int totalElements = allUsers.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        // Get paginated sublist
        List<Users> paginatedUsers = allUsers.subList(fromIndex, toIndex);
        List<UserAdminDTO> userDTOs = paginatedUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("users", userDTOs);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user (excludes password)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserAdminDTO> getUserById(@PathVariable("userId") Long userId) {
        authenticationHelper.validateAdminAccess();

        Users user = userReadService.getUserById(userId);
        UserAdminDTO userDTO = convertToDTO(user);

        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Updates user information (name, email, mobile, roles)")
    @ApiResponse(responseCode = "200", description = "Successfully updated user")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserAdminDTO> updateUser(@PathVariable("userId") Long userId,
            @Valid @RequestBody UserAdminDTO userDTO) {
        authenticationHelper.validateAdminAccess();

        Users existingUser = userReadService.getUserById(userId);

        // Update basic fields
        existingUser.setName(userDTO.getName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setMobileNumber(userDTO.getMobileNumber());

        // Update roles if provided
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                newRoles.add(role);
            }
            existingUser.setRoles(newRoles);
        }

        userWriteService.updateUser(existingUser);

        return ResponseEntity.ok(convertToDTO(existingUser));
    }

    @PostMapping("/users/{userId}/roles/{roleName}")
    @Operation(summary = "Add role to user", description = "Adds a specific role to a user")
    @ApiResponse(responseCode = "200", description = "Successfully added role")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    @ApiResponse(responseCode = "404", description = "User or role not found")
    public ResponseEntity<UserAdminDTO> addRoleToUser(@PathVariable("userId") Long userId,
            @PathVariable("roleName") String roleName) {
        authenticationHelper.validateAdminAccess();

        Users user = userReadService.getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.getRoles().add(role);
        userWriteService.updateUser(user);

        return ResponseEntity.ok(convertToDTO(user));
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    @Operation(summary = "Remove role from user", description = "Removes a specific role from a user")
    @ApiResponse(responseCode = "200", description = "Successfully removed role")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    @ApiResponse(responseCode = "404", description = "User or role not found")
    public ResponseEntity<UserAdminDTO> removeRoleFromUser(@PathVariable("userId") Long userId,
            @PathVariable("roleName") String roleName) {
        authenticationHelper.validateAdminAccess();

        Users user = userReadService.getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.getRoles().remove(role);
        userWriteService.updateUser(user);

        return ResponseEntity.ok(convertToDTO(user));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user from the system")
    @ApiResponse(responseCode = "204", description = "Successfully deleted user")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
        authenticationHelper.validateAdminAccess();

        Users user = userReadService.getUserById(userId);
        userWriteService.deleteUser(user.getEmail());

        return ResponseEntity.noContent().build();
    }

    // Utilities Endpoints

    @GetMapping("/utilities/critical-logs")
    @Operation(summary = "Get critical logs", description = "Retrieves the latest 10 critical error logs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved critical logs")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    public ResponseEntity<List<CriticalLog>> getCriticalLogs() {
        authenticationHelper.validateAdminAccess();

        List<CriticalLog> logs = criticalLogRepository.findTop10ByOrderByTimestampDesc();
        // Limit to 10
        List<CriticalLog> limitedLogs = logs.stream().limit(10).collect(Collectors.toList());

        return ResponseEntity.ok(limitedLogs);
    }

    // Activity Logs Endpoints

    @GetMapping("/utilities/activity-logs")
    @Operation(summary = "Get user activity logs", description = "Retrieves recent user activity logs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved activity logs")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    public ResponseEntity<List<UserActivityLog>> getActivityLogs(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        authenticationHelper.validateAdminAccess();

        List<UserActivityLog> logs;
        if (userId != null) {
            logs = activityLogRepository.findByUserIdOrderByTimestampDesc(userId);
        } else {
            logs = activityLogRepository.findTop50ByOrderByTimestampDesc();
        }

        return ResponseEntity.ok(logs.stream().limit(limit).collect(Collectors.toList()));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Retrieves all available roles in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved roles")
    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
    public ResponseEntity<List<String>> getAllRoles() {
        authenticationHelper.validateAdminAccess();

        List<String> roles = roleRepository.findAll().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

    // Helper Methods

    private UserAdminDTO convertToDTO(Users user) {
        return UserAdminDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
