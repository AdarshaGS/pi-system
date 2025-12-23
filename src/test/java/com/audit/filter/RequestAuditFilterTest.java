package com.audit.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.audit.entity.RequestAudit;
import com.audit.service.RequestAuditService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RequestAuditFilterTest {

    @Mock
    private RequestAuditService requestAuditService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RequestAuditFilter requestAuditFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_ShouldLogRequest() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");
        when(authentication.getPrincipal()).thenReturn("testUserPrincipal");

        // Act
        // Accessing the protected method via a slight hack or assuming test is in same
        // package?
        // No, test is in same package structure but likely different
        // classloader/folder.
        // Actually protected methods are visible to classes in invalid packages if
        // mapped, but here:
        // doFilterInternal is protected in OncePerRequestFilter.
        // But we can invoke the public `doFilter`.
        // However, `OncePerRequestFilter`'s doFilter calls doFilterInternal.
        // Let's call the public doFilter if possible, or assume reflection access.
        // Standard way is to use the public API filter.doFilter(req, res, chain)

        requestAuditFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);

        ArgumentCaptor<RequestAudit> captor = ArgumentCaptor.forClass(RequestAudit.class);
        verify(requestAuditService, times(1)).logRequest(captor.capture());

        RequestAudit capturedAudit = captor.getValue();
        assertEquals("/api/test", capturedAudit.getUri());
        assertEquals("GET", capturedAudit.getMethod());
        assertEquals("testUser", capturedAudit.getUserId());
        assertEquals(200, capturedAudit.getStatusCode());
        assertNotNull(capturedAudit.getTimestamp());
    }

    @Test
    void doFilterInternal_ShouldUseXForwardedForIP() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("POST");
        when(response.getStatus()).thenReturn(201);

        // Act
        requestAuditFilter.doFilter(request, response, filterChain);

        // Assert
        ArgumentCaptor<RequestAudit> captor = ArgumentCaptor.forClass(RequestAudit.class);
        verify(requestAuditService, times(1)).logRequest(captor.capture());

        assertEquals("10.0.0.1", captor.getValue().getIpAddress());
    }
}
