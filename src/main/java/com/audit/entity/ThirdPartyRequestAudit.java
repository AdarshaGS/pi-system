package com.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "third_party_request_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyRequestAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "LONGTEXT")
    private String requestBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    @Column(name = "time_taken_ms")
    private Long timeTakenMs;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;
}
