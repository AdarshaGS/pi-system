package com.common.devtools.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationResponse {
    private String fileName;
    private String fullPath;
    private String content;
    private String message;
}
