package com.main;

import org.flywaydb.core.api.FlywayException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            log.info("Initiating Premium Flyway Validation Pre-check...");
            try {
                flyway.validate();
                log.info("Flyway validation successful. All migrations are consistent.");
            } catch (FlywayException e) {
                String fullMessage = e.getMessage() != null ? e.getMessage() : "Unknown Flyway validation error";

                // Flyway often joins multiple errors with newlines or semicolons
                List<String> errorLines = Arrays.stream(fullMessage.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());

                StringBuilder sb = new StringBuilder();
                sb.append("\n\n");
                sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                       FLYWAY MIGRATION VALIDATION ERRORS                     ║\n");
                sb.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
                sb.append(String.format("║ Validation Mismatches Detected: %-46d ║\n", errorLines.size()));
                sb.append("║                                                                              ║\n");

                for (String error : errorLines) {
                    if (error.startsWith("Migration Checksum Mismatch") || error.contains("mismatch")
                            || error.contains("not found")) {
                        sb.append("╟──────────────────────────────────────────────────────────────────────────────╢\n");
                    } else if (errorLines.indexOf(error) == 0) {
                        sb.append("╟──────────────────────────────────────────────────────────────────────────────╢\n");
                    }

                    // Wrap long error lines to fit in the box
                    String cleanError = error.replace("Validate failed: ", "");
                    int start = 0;
                    while (start < cleanError.length()) {
                        int end = Math.min(start + 74, cleanError.length());
                        sb.append(String.format("║ %-76s ║\n", cleanError.substring(start, end)));
                        start = end;
                    }

                    // Generate Repair Script for Checksum Mismatches
                    if (cleanError.contains("Checksum Mismatch")) {
                        try {
                            String version = null;
                            if (cleanError.contains("Migration Checksum Mismatch: ")) {
                                int versionStart = cleanError.indexOf(": ") + 2;
                                int versionEnd = cleanError.indexOf("__");
                                if (versionEnd == -1)
                                    versionEnd = cleanError.indexOf(" ", versionStart);
                                if (versionEnd != -1) {
                                    version = cleanError.substring(versionStart, versionEnd).replace("V", "");
                                }
                            }

                            if (version != null && cleanError.contains("Actual: ")) {
                                String actualChecksum = cleanError.substring(cleanError.indexOf("Actual: ") + 8,
                                        cleanError.indexOf(")"));
                                String sql = String.format(
                                        "UPDATE flyway_schema_history SET checksum = %s WHERE version = '%s';",
                                        actualChecksum, version);
                                sb.append(String.format("║ %-76s ║\n", "👉 RUN THIS TO FIX:"));
                                sb.append(String.format("║ %-76s ║\n", sql));
                            }
                        } catch (Exception ex) {
                            // Skip script generation on parsing error
                        }
                    }
                }

                sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n");

                System.err.println(sb.toString());

                throw new RuntimeException(
                        "Flyway validation failed. Multiple mismatches detected. Please fix the migration scripts.");
            }

            flyway.migrate();
        };
    }
}
