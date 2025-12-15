package com.audit.service;

import com.audit.entity.RequestAudit;
import com.audit.repository.RequestAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestAuditServiceTest {

    @Mock
    private RequestAuditRepository requestAuditRepository;

    @InjectMocks
    private RequestAuditService requestAuditService;

    private RequestAudit requestAudit;

    @BeforeEach
    void setUp() {
        requestAudit = RequestAudit.builder()
                .userId("testUser")
                .method("GET")
                .uri("/api/test")
                .statusCode(200)
                .timeTakenMs(100L)
                .build();
    }

    @Test
    void logRequest_ShouldSaveAuditLog() {
        requestAuditService.logRequest(requestAudit);
        verify(requestAuditRepository, times(1)).save(any(RequestAudit.class));
    }

    @Test
    void logRequest_ShouldHandleExceptionGracefully() {
        // Force an exception by mocking save to throw one (if possible) or just verify
        // it doesn't crash
        // Since the service catches Exception, we verify it doesn't propagate
        org.mockito.Mockito.when(requestAuditRepository.save(any(RequestAudit.class)))
                .thenThrow(new RuntimeException("Database error"));

        requestAuditService.logRequest(requestAudit);

        verify(requestAuditRepository, times(1)).save(any(RequestAudit.class));
    }
}
