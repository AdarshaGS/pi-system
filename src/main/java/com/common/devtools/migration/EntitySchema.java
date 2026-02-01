package com.common.devtools.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntitySchema {
    private String className;
    private String packageName;
    private String tableName;
    private List<ColumnSchema> columns;
    private List<IndexSchema> indexes;
    private List<UniqueConstraintSchema> uniqueConstraints;
}
