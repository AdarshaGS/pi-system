package com.common.devtools.migration;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scans JPA entities and extracts database schema information
 */
@Component
@Slf4j
public class EntityScanner {

    /**
     * Scans all entity classes and returns their schema definitions
     */
    public List<EntitySchema> scanEntities(List<Class<?>> entityClasses) {
        return entityClasses.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Entity.class))
                .map(this::extractEntitySchema)
                .collect(Collectors.toList());
    }

    /**
     * Extract schema information from a single entity class
     */
    public EntitySchema extractEntitySchema(Class<?> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        Table tableAnnotation = entityClass.getAnnotation(Table.class);

        String tableName = getTableName(entityClass, tableAnnotation);
        List<ColumnSchema> columns = extractColumns(entityClass);
        List<IndexSchema> indexes = extractIndexes(tableAnnotation);
        List<UniqueConstraintSchema> uniqueConstraints = extractUniqueConstraints(tableAnnotation);

        return EntitySchema.builder()
                .className(entityClass.getSimpleName())
                .packageName(entityClass.getPackage().getName())
                .tableName(tableName)
                .columns(columns)
                .indexes(indexes)
                .uniqueConstraints(uniqueConstraints)
                .build();
    }

    private String getTableName(Class<?> entityClass, Table tableAnnotation) {
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return camelToSnake(entityClass.getSimpleName());
    }

    private List<ColumnSchema> extractColumns(Class<?> entityClass) {
        List<ColumnSchema> columns = new ArrayList<>();
        
        log.debug("Scanning fields for entity: {}", entityClass.getSimpleName());
        for (Field field : getAllFields(entityClass)) {
            log.debug("  Field: {} (type: {})", field.getName(), field.getType().getSimpleName());
            
            if (field.isAnnotationPresent(Transient.class)) {
                log.debug("    Skipping @Transient field: {}", field.getName());
                continue;
            }

            Column columnAnnotation = field.getAnnotation(Column.class);
            Id idAnnotation = field.getAnnotation(Id.class);
            GeneratedValue generatedValueAnnotation = field.getAnnotation(GeneratedValue.class);
            Enumerated enumeratedAnnotation = field.getAnnotation(Enumerated.class);
            ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
            OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
            ManyToMany manyToManyAnnotation = field.getAnnotation(ManyToMany.class);
            OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);

            // Skip collection mappings (OneToMany, ManyToMany without join column)
            if (oneToManyAnnotation != null || 
                (manyToManyAnnotation != null && joinColumnAnnotation == null)) {
                log.debug("    Skipping relationship field: {}", field.getName());
                continue;
            }

            String columnName = getColumnName(field, columnAnnotation, joinColumnAnnotation);
            String sqlType = getSqlType(field, columnAnnotation, enumeratedAnnotation);
            boolean nullable = isNullable(columnAnnotation, idAnnotation);
            boolean primaryKey = idAnnotation != null;
            boolean autoIncrement = generatedValueAnnotation != null && 
                                   generatedValueAnnotation.strategy() == GenerationType.IDENTITY;
            String defaultValue = getDefaultValue(columnAnnotation);
            Integer length = getLength(columnAnnotation, field);
            Integer precision = getPrecision(columnAnnotation);
            Integer scale = getScale(columnAnnotation);

            columns.add(ColumnSchema.builder()
                    .fieldName(field.getName())
                    .columnName(columnName)
                    .sqlType(sqlType)
                    .nullable(nullable)
                    .primaryKey(primaryKey)
                    .autoIncrement(autoIncrement)
                    .defaultValue(defaultValue)
                    .length(length)
                    .precision(precision)
                    .scale(scale)
                    .build());
        }

        return columns;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] declaredFields = current.getDeclaredFields();
            log.info("Class: {} - Found {} declared fields", current.getSimpleName(), declaredFields.length);
            for (Field f : declaredFields) {
                log.info("  -> Field: {} (type: {})", f.getName(), f.getType().getSimpleName());
            }
            fields.addAll(Arrays.asList(declaredFields));
            current = current.getSuperclass();
        }
        log.info("Total fields for {}: {}", clazz.getSimpleName(), fields.size());
        return fields;
    }

    private String getColumnName(Field field, Column columnAnnotation, JoinColumn joinColumnAnnotation) {
        if (joinColumnAnnotation != null && !joinColumnAnnotation.name().isEmpty()) {
            return joinColumnAnnotation.name();
        }
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        return camelToSnake(field.getName());
    }

    private String getSqlType(Field field, Column columnAnnotation, Enumerated enumeratedAnnotation) {
        Class<?> type = field.getType();

        // Enum handling
        if (type.isEnum()) {
            if (enumeratedAnnotation != null && enumeratedAnnotation.value() == EnumType.ORDINAL) {
                return "INT";
            }
            return "VARCHAR(50)";
        }

        // String
        if (type == String.class) {
            int length = columnAnnotation != null ? columnAnnotation.length() : 255;
            if (length > 5000) {
                return "TEXT";
            }
            return "VARCHAR(" + length + ")";
        }

        // Numbers
        if (type == Long.class || type == long.class) {
            return "BIGINT";
        }
        if (type == Integer.class || type == int.class) {
            return "INT";
        }
        if (type == Short.class || type == short.class) {
            return "SMALLINT";
        }
        if (type == Byte.class || type == byte.class) {
            return "TINYINT";
        }

        // Decimals
        if (type == java.math.BigDecimal.class) {
            int precision = columnAnnotation != null ? columnAnnotation.precision() : 19;
            int scale = columnAnnotation != null ? columnAnnotation.scale() : 2;
            return "DECIMAL(" + precision + ", " + scale + ")";
        }
        if (type == Float.class || type == float.class) {
            return "FLOAT";
        }
        if (type == Double.class || type == double.class) {
            return "DOUBLE";
        }

        // Boolean
        if (type == Boolean.class || type == boolean.class) {
            return "BOOLEAN";
        }

        // Dates and Times
        if (type == java.time.LocalDate.class) {
            return "DATE";
        }
        if (type == java.time.LocalDateTime.class || type == java.util.Date.class) {
            return "TIMESTAMP";
        }
        if (type == java.time.LocalTime.class) {
            return "TIME";
        }

        // Default
        return "VARCHAR(255)";
    }

    private boolean isNullable(Column columnAnnotation, Id idAnnotation) {
        if (idAnnotation != null) {
            return false;
        }
        if (columnAnnotation != null) {
            return columnAnnotation.nullable();
        }
        return true;
    }

    private String getDefaultValue(Column columnAnnotation) {
        if (columnAnnotation != null && !columnAnnotation.columnDefinition().isEmpty()) {
            String def = columnAnnotation.columnDefinition().toUpperCase();
            if (def.contains("DEFAULT")) {
                return extractDefault(def);
            }
        }
        return null;
    }

    private String extractDefault(String columnDefinition) {
        int idx = columnDefinition.toUpperCase().indexOf("DEFAULT");
        if (idx >= 0) {
            String part = columnDefinition.substring(idx + 7).trim();
            if (part.contains(" ")) {
                return part.substring(0, part.indexOf(" "));
            }
            return part;
        }
        return null;
    }

    private Integer getLength(Column columnAnnotation, Field field) {
        if (field.getType() == String.class && columnAnnotation != null) {
            return columnAnnotation.length();
        }
        return null;
    }

    private Integer getPrecision(Column columnAnnotation) {
        if (columnAnnotation != null && columnAnnotation.precision() > 0) {
            return columnAnnotation.precision();
        }
        return null;
    }

    private Integer getScale(Column columnAnnotation) {
        if (columnAnnotation != null && columnAnnotation.scale() > 0) {
            return columnAnnotation.scale();
        }
        return null;
    }

    private List<IndexSchema> extractIndexes(Table tableAnnotation) {
        if (tableAnnotation == null || tableAnnotation.indexes().length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(tableAnnotation.indexes())
                .map(index -> IndexSchema.builder()
                        .name(index.name())
                        .columnList(index.columnList())
                        .unique(index.unique())
                        .build())
                .collect(Collectors.toList());
    }

    private List<UniqueConstraintSchema> extractUniqueConstraints(Table tableAnnotation) {
        if (tableAnnotation == null || tableAnnotation.uniqueConstraints().length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(tableAnnotation.uniqueConstraints())
                .map(constraint -> UniqueConstraintSchema.builder()
                        .name(constraint.name())
                        .columnNames(Arrays.asList(constraint.columnNames()))
                        .build())
                .collect(Collectors.toList());
    }

    private String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
