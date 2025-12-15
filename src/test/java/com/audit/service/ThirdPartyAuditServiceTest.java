package com.audit.service;

import com.audit.entity.ThirdPartyRequestAudit;
import com.audit.repository.ThirdPartyRequestAuditRepository;
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
class ThirdPartyAuditServiceTest {

    @Mock
    private ThirdPartyRequestAuditRepository repository;

    @InjectMocks
    private ThirdPartyAuditService service;

    private ThirdPartyRequestAudit audit;

    @BeforeEach
    void setUp() {
        audit = ThirdPartyRequestAudit.builder()
                .providerName("TEST_PROVIDER")
                .url("http://test-url.com")
                .method("GET")
                .responseStatus(200)
                .timeTakenMs(50L)
                .build();
    }

    @Test
    void logOnly_ShouldSaveAuditLog() {
        service.logOnly(audit);
        verify(repository, times(1)).save(any(ThirdPartyRequestAudit.class));
    }

    @Test
    void logOnly_ShouldHandleExceptionGracefully() {
        org.mockito.Mockito.when(repository.save(any(ThirdPartyRequestAudit.class)))
                .thenThrow(new RuntimeException("DB Error"));

        service.logOnly(audit);

        verify(repository, times(1)).save(any(ThirdPartyRequestAudit.class));
    }
}
