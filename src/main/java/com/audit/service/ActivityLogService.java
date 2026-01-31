package com.audit.service;

import com.audit.data.UserActivityLog;
import com.audit.repo.UserActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    private final UserActivityLogRepository activityLogRepository;

    public ActivityLogService(UserActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(Long userId, String username, String userEmail, String action, String description) {
        logActivity(userId, username, userEmail, action, null, null, description, "SUCCESS", null);
    }

    public void logActivity(Long userId, String username, String userEmail, String action, String resourceType,
            String resourceId, String description, String status, String errorMessage) {

        HttpServletRequest request = getCurrentRequest();
        String ipAddress = null;
        String userAgent = null;

        if (request != null) {
            ipAddress = getClientIpAddress(request);
            userAgent = request.getHeader("User-Agent");
        }

        UserActivityLog log = UserActivityLog.builder()
                .userId(userId)
                .username(username)
                .userEmail(userEmail)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorMessage(errorMessage)
                .build();

        activityLogRepository.save(log);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA",
                "REMOTE_ADDR" };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0];
            }
        }

        return request.getRemoteAddr();
    }
}
