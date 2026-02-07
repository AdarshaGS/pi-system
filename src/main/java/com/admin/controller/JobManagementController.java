package com.admin.controller;

import com.admin.dto.ScheduledJobDTO;
import com.admin.service.JobManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing scheduled jobs
 */
@RestController
@RequestMapping("/api/v1/admin/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "APIs for managing and executing scheduled jobs")
public class JobManagementController {

    private final JobManagementService jobManagementService;

    @GetMapping
    @Operation(summary = "Get all scheduled jobs", description = "Retrieve list of all scheduled jobs with their details")
    public ResponseEntity<List<ScheduledJobDTO>> getAllJobs() {
        return ResponseEntity.ok(jobManagementService.getAllJobs());
    }

    @PostMapping("/{jobName}/execute")
    @Operation(summary = "Execute job manually", description = "Manually trigger a scheduled job to run immediately")
    public ResponseEntity<Map<String, String>> executeJob(@PathVariable String jobName) {
        try {
            jobManagementService.executeJob(jobName);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Job " + jobName + " executed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Failed to execute job: " + e.getMessage()
            ));
        }
    }
}
