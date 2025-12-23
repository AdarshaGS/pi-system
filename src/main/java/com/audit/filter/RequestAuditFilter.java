package com.audit.filter;

import com.audit.entity.RequestAudit;
import com.audit.service.RequestAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RequestAuditFilter extends OncePerRequestFilter {

    private final RequestAuditService requestAuditService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip auditing for specific paths if needed (e.g., swagger, health checks)
        // But the user said "every request", so we will keep it broad but maybe skip
        // static assets if we had them.
        // For now, we'll audit everything handled by this filter.

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            recordAudit(request, response, duration);
        }
    }

    private void recordAudit(HttpServletRequest request, HttpServletResponse response, long duration) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                userId = authentication.getName(); // Assuming username/email is the ID, or use primary key if available
            }

            RequestAudit audit = RequestAudit.builder()
                    .userId(userId)
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .queryParams(request.getQueryString())
                    .statusCode(response.getStatus())
                    .timeTakenMs(duration)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            requestAuditService.logRequest(audit);
        } catch (Exception e) {
            // Ensure audit failures don't crash the application, though service logs it
            // too.
            // Just being extra safe here not to bubble up exceptions from finally block
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
