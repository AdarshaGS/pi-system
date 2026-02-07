package com.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for scheduled job information
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledJobDTO {
    private String jobName;
    private String description;
    private String schedule;
    private String category;
    private boolean canRunManually;
    private String lastRunTime;
    private String nextRunTime;
}
