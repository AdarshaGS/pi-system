package com.common.devtools.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/open/dev/migration")
@Tag(name = "Development Tools", description = "Utilities for developers")
@RequiredArgsConstructor
public class MigrationGeneratorController {

    private final MigrationGeneratorService migrationGeneratorService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new Flyway migration script with proper formatting")
    public ResponseEntity<MigrationResponse> generateMigration(@RequestBody MigrationRequest request) {
        try {
            MigrationResponse response = migrationGeneratorService.generateMigration(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    MigrationResponse.builder()
                            .message("Failed to generate migration: " + e.getMessage())
                            .build());
        }
    }
}
