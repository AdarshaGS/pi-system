package com.common.devtools.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnSchema {
    private String fieldName;
    private String columnName;
    private String sqlType;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;
    private String defaultValue;
    private Integer length;
    private Integer precision;
    private Integer scale;
}
