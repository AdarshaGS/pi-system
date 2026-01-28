package com.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Global error response structure")
public class ApiErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2024-01-29T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "Unique Request ID for correlation", example = "req-12345")
    private String requestId;

    @Schema(description = "HTTP Status Code", example = "400")
    private int status;

    @Schema(description = "Error Category", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Invalid input parameter")
    private String message;

    @Schema(description = "API Path where the error occurred", example = "/api/v1/user/profile")
    private String path;
}
