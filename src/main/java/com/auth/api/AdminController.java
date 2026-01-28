package com.auth.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin")
public class AdminController {

    @Hidden
    @GetMapping("/internal/health")
    public String internalHealth() {
        return "OK";
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard", description = "Only Accessible by ROLE_ADMIN or ROLE_SUPER_ADMIN")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAdminStats() {
        return ResponseEntity.ok(Map.of(
                "status", "Healthy",
                "message", "Welcome to Admin Dashboard",
                "activeJobs", 0));
    }
}
