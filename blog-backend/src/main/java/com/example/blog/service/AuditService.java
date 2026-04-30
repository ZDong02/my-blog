package com.example.blog.service;

import com.example.blog.entity.AuditLog;
import com.example.blog.mapper.AuditLogMapper;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.util.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuditService {

    @Autowired
    private AuditLogMapper auditLogMapper;

    public void log(String action, String targetType, Long targetId, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setDetails(details);
            auditLog.setCreatedAt(LocalDateTime.now());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof JwtUserDetails) {
                    JwtUserDetails userDetails = (JwtUserDetails) principal;
                    auditLog.setUserId(userDetails.getId());
                    auditLog.setUsername(userDetails.getUsername());
                }
            }

            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(IpUtils.getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            } else {
                auditLog.setIpAddress("unknown");
            }

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // Don't let audit logging failure affect main operation
            System.err.println("Failed to write audit log: " + e.getMessage());
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public void logUserAction(Long targetUserId, String targetUsername, String action, String details) {
        log(action, "USER", targetUserId,
            "Action: " + action + " | Target: " + targetUsername + (details != null ? " | " + details : ""));
    }

    public void logPostAction(Long postId, String action, String details) {
        log(action, "POST", postId,
            "Action: " + action + (details != null ? " | " + details : ""));
    }

    public void logCategoryAction(Long categoryId, String categoryName, String action) {
        log(action, "CATEGORY", categoryId, "Category: " + categoryName);
    }

    public void logAuthAction(String action, String username, boolean success, String details) {
        String result = success ? "SUCCESS" : "FAILED";
        log(action, "AUTH", null,
            "Action: " + action + " | User: " + username + " | Result: " + result +
            (details != null ? " | " + details : ""));
    }
}