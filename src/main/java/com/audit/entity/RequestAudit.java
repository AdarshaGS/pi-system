package com.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, length = 2048)
    private String uri;

    @Column(name = "query_params", length = 2048)
    private String queryParams;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "time_taken_ms", nullable = false)
    private long timeTakenMs;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;
}
