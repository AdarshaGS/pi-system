package com.budget.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for email report request containing recipient information and report parameters
 */
@Data
public class EmailReportRequest {
    private String email;
    private String monthYear; // Format: "2026-01" or "January 2026"
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> categories;
    private String format; // "PDF" or "CSV"
    private String subject;
    private String message;
}
