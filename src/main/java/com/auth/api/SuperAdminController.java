package com.auth.api;

import com.auth.data.Role;
import com.auth.repo.RoleRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/super-admin")
@Tag(name = "Admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;

    @PostMapping("/update-role/{userId}")
    @Operation(summary = "Change a user's role", description = "Only SUPER_ADMIN can execute this")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String roleName = request.get("role");
        if (roleName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role name is required"));
        }

        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User ID is required"));
        }
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // Prompt requirement: ONE role per user
        user.setRoles(Collections.singleton(role));
        usersRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Role updated successfully to " + roleName));
    }
}
