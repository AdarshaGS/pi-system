package com.common.exception.data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "critical_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriticalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "status")
    private int status;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "path")
    private String path;

    @Column(name = "method")
    private String method;

    @Lob
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;
}
