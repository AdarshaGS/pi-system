package com.common.devtools.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MigrationGeneratorService {

    private static final String MIGRATION_PATH = "src/main/resources/db/migration";
    private static final Pattern VERSION_PATTERN = Pattern.compile("V(\\d+)__.*\\.sql", Pattern.CASE_INSENSITIVE);

    public MigrationResponse generateMigration(MigrationRequest request) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        Path migrationDir = Paths.get(projectRoot, MIGRATION_PATH);

        if (!Files.exists(migrationDir)) {
            Files.createDirectories(migrationDir);
        }

        int nextVersion = getNextVersion(migrationDir);
        String fileName = String.format("V%d__%s.sql", nextVersion, sanitizeDescription(request.getDescription()));
        Path filePath = migrationDir.resolve(fileName);

        String formattedSql = formatSql(request.getSql());

        Files.write(filePath, formattedSql.getBytes());

        return MigrationResponse.builder()
                .fileName(fileName)
                .fullPath(filePath.toString())
                .content(formattedSql)
                .message("Migration script generated successfully.")
                .build();
    }

    private int getNextVersion(Path migrationDir) throws IOException {
        File[] files = migrationDir.toFile().listFiles((dir, name) -> name.endsWith(".sql"));
        if (files == null || files.length == 0) {
            return 1;
        }

        return Arrays.stream(files)
                .map(File::getName)
                .map(name -> {
                    Matcher matcher = VERSION_PATTERN.matcher(name);
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                    return 0;
                })
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private String sanitizeDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "New_Migration";
        }
        return description.trim().replaceAll("\\s+", "_");
    }

    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "-- New Migration\n-- TODO: Add your SQL statements here\n";
        }

        String formatted = sql.trim();

        // 1. Ensure each statement ends with a semicolon
        if (!formatted.endsWith(";")) {
            formatted += ";";
        }

        // 2. Uppercase common SQL keywords
        List<String> keywords = Arrays.asList(
                "select", "insert", "update", "delete", "create", "alter", "drop", "table", "column", "add",
                "modify", "constraint", "primary", "key", "foreign", "references", "index", "unique",
                "not", "null", "default", "auto_increment", "varchar", "int", "bigint", "decimal", "double",
                "datetime", "timestamp", "if", "exists", "into", "values", "set", "where", "from",
                "engine", "charset", "collate", "btree", "using", "ignore", "on", "to", "after", "rename");

        for (String keyword : keywords) {
            formatted = formatted.replaceAll("(?i)\\b" + keyword + "\\b", keyword.toUpperCase());
        }

        // 3. Special Rule: Suggest IF NOT EXISTS for CREATE TABLE if it's missing
        formatted = formatted.replaceAll("(?i)CREATE TABLE (?!IF NOT EXISTS)", "CREATE TABLE IF NOT EXISTS ");

        // 4. Formatting: Newlines before major keywords if after a semicolon
        formatted = formatted.replaceAll("(?i);\\s*(ALTER|CREATE|INSERT|UPDATE|DELETE|DROP|COMMENT)", ";\n\n$1");

        // 5. Formatting: Consistent spacing after commas in lists (only if not inside
        // quotes)
        // (Simplified version: just space after comma)
        formatted = formatted.replaceAll(",\\s*", ", ");

        return formatted + "\n";
    }
}
