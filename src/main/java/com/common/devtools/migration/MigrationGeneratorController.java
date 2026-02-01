package com.common.devtools.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/open/dev/migration")
@Tag(name = "Development Tools - Migration Generator", description = "Generate migrations from JPA entities and detect schema drift")
@RequiredArgsConstructor
public class MigrationGeneratorController {

    private final MigrationGeneratorService migrationGeneratorService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new Flyway migration script with manual SQL")
    public ResponseEntity<MigrationResponse> generateMigration(@Valid @RequestBody MigrationRequest request) {
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

    @PostMapping("/generate-from-entities")
    @Operation(summary = "Generate migration script from JPA entity changes automatically",
               description = "Scans all JPA entities, compares with previous snapshot, and generates migration SQL")
    public ResponseEntity<MigrationResponse> generateFromEntities(
            @RequestParam(required = false, defaultValue = "Auto_Generated_Migration") String description) {
        try {
            MigrationResponse response = migrationGeneratorService.generateMigrationFromEntities(description);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    MigrationResponse.builder()
                            .message("Failed to generate migration from entities: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/detect-drift")
    @Operation(summary = "Detect schema drift between entities and database",
               description = "Compares current JPA entities with last snapshot to detect changes")
    public ResponseEntity<SchemaDrift> detectDrift() {
        try {
            SchemaDrift drift = migrationGeneratorService.detectSchemaDrift();
            return ResponseEntity.ok(drift);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    SchemaDrift.builder()
                            .hasChanges(false)
                            .changeCount(0)
                            .build());
        }
    }

    @GetMapping("/schema/current")
    @Operation(summary = "Get current schema from JPA entities",
               description = "Returns the schema definition extracted from all JPA entity classes")
    public ResponseEntity<List<EntitySchema>> getCurrentSchema() {
        List<EntitySchema> schema = migrationGeneratorService.getCurrentSchema();
        return ResponseEntity.ok(schema);
    }

    @PostMapping("/schema/snapshot")
    @Operation(summary = "Save current schema as snapshot",
               description = "Saves the current JPA entity schema as a baseline for future drift detection")
    public ResponseEntity<Map<String, String>> saveSnapshot() {
        try {
            migrationGeneratorService.saveCurrentSnapshot();
            return ResponseEntity.ok(Map.of("message", "Schema snapshot saved successfully"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to save snapshot: " + e.getMessage()));
        }
    }
}
