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
public class MigrationResponse {
    private String fileName;
    private String fullPath;
    private String content;
    private String message;
    private List<String> schemaChanges;
}
