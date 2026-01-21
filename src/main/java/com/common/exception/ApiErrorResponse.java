package com.common.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private String requestId;
    private int status;
    private String error;
    private String message;
    private String path;
}
