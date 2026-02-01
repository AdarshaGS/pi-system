package com.common.devtools.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationGeneratorService {

    private static final String MIGRATION_PATH = "src/main/resources/db/migration";
    private static final String SCHEMA_SNAPSHOT_PATH = "src/main/resources/db/schema-snapshot.json";
    private static final Pattern VERSION_PATTERN = Pattern.compile("V(\\d+)__.*\\.sql", Pattern.CASE_INSENSITIVE);

    private final EntityScanner entityScanner;
    private final SchemaDriftDetector driftDetector;
    private final MigrationScriptGenerator scriptGenerator;
    private final ObjectMapper objectMapper;

    /**
     * Generate migration from manual SQL
     */
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

    /**
     * Scan all JPA entities and generate migration from entity changes
     */
    public MigrationResponse generateMigrationFromEntities(String description) throws IOException {
        log.info("Scanning JPA entities to detect schema changes...");

        // Scan all entity classes
        List<Class<?>> entityClasses = scanEntityClasses();
        log.info("Found {} entity classes", entityClasses.size());

        // Extract current schema from entities
        List<EntitySchema> currentSchema = entityScanner.scanEntities(entityClasses);

        // Load previous snapshot
        List<EntitySchema> previousSchema = loadSchemaSnapshot();

        // Detect drift
        SchemaDrift drift = driftDetector.detectDrift(previousSchema, currentSchema);

        if (!drift.isHasChanges()) {
            return MigrationResponse.builder()
                    .message("No schema changes detected. Database is in sync with entities.")
                    .schemaChanges(Collections.emptyList())
                    .build();
        }

        log.info("Detected {} schema changes", drift.getChangeCount());

        // Generate migration SQL
        String sql = scriptGenerator.generateMigrationScript(drift, description);

        // Write migration file
        String projectRoot = System.getProperty("user.dir");
        Path migrationDir = Paths.get(projectRoot, MIGRATION_PATH);
        if (!Files.exists(migrationDir)) {
            Files.createDirectories(migrationDir);
        }

        int nextVersion = getNextVersion(migrationDir);
        String fileName = String.format("V%d__%s.sql", nextVersion, sanitizeDescription(description));
        Path filePath = migrationDir.resolve(fileName);
        Files.write(filePath, sql.getBytes());

        // Save current schema as snapshot
        saveSchemaSnapshot(currentSchema);

        return MigrationResponse.builder()
                .fileName(fileName)
                .fullPath(filePath.toString())
                .content(sql)
                .message(String.format("Migration generated with %d changes", drift.getChangeCount()))
                .schemaChanges(drift.getChanges().stream()
                        .map(SchemaChange::getDescription)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Compare current entities with database and return drift report
     */
    public SchemaDrift detectSchemaDrift() throws IOException {
        List<Class<?>> entityClasses = scanEntityClasses();
        List<EntitySchema> currentSchema = entityScanner.scanEntities(entityClasses);
        List<EntitySchema> previousSchema = loadSchemaSnapshot();
        
        return driftDetector.detectDrift(previousSchema, currentSchema);
    }

    /**
     * Get current schema from entities
     */
    public List<EntitySchema> getCurrentSchema() {
        List<Class<?>> entityClasses = scanEntityClasses();
        return entityScanner.scanEntities(entityClasses);
    }

    /**
     * Save current schema as snapshot
     */
    public void saveCurrentSnapshot() throws IOException {
        List<EntitySchema> currentSchema = getCurrentSchema();
        saveSchemaSnapshot(currentSchema);
        log.info("Schema snapshot saved with {} tables", currentSchema.size());
    }

    /**
     * Scan classpath for entity classes
     */
    private List<Class<?>> scanEntityClasses() {
        try {
            ClassPathScanningCandidateComponentProvider scanner = 
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

            Set<BeanDefinition> beans = scanner.findCandidateComponents("com");
            
            List<Class<?>> entityClasses = new ArrayList<>();
            for (BeanDefinition bean : beans) {
                try {
                    Class<?> clazz = Class.forName(bean.getBeanClassName());
                    entityClasses.add(clazz);
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load class: {}", bean.getBeanClassName());
                }
            }
            
            return entityClasses;
        } catch (Exception e) {
            log.error("Error scanning entity classes", e);
            return Collections.emptyList();
        }
    }

    /**
     * Load schema snapshot from file
     */
    private List<EntitySchema> loadSchemaSnapshot() throws IOException {
        String projectRoot = System.getProperty("user.dir");
        Path snapshotPath = Paths.get(projectRoot, SCHEMA_SNAPSHOT_PATH);
        
        if (!Files.exists(snapshotPath)) {
            log.info("No schema snapshot found, assuming fresh start");
            return Collections.emptyList();
        }

        try {
            String json = Files.readString(snapshotPath);
            return objectMapper.readValue(json, new TypeReference<List<EntitySchema>>() {});
        } catch (Exception e) {
            log.warn("Could not read schema snapshot, assuming fresh start", e);
            return Collections.emptyList();
        }
    }

    /**
     * Save schema snapshot to file
     */
    private void saveSchemaSnapshot(List<EntitySchema> schema) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        Path snapshotPath = Paths.get(projectRoot, SCHEMA_SNAPSHOT_PATH);
        
        Path parentDir = snapshotPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        Files.write(snapshotPath, json.getBytes());
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
