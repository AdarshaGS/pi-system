package com.common.devtools.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaChange {
    private SchemaChangeType type;
    private String tableName;
    private String columnName;
    private String indexName;
    private String description;
    
    // For comparing old vs new
    private EntitySchema oldSchema;
    private EntitySchema newSchema;
    private ColumnSchema oldColumn;
    private ColumnSchema newColumn;
    private IndexSchema oldIndex;
    private IndexSchema newIndex;
}
