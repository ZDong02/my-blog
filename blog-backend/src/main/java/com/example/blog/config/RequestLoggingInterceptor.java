package com.example.blog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Request logging interceptor for monitoring and debugging
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        if (logger.isDebugEnabled()) {
            logger.debug("Incoming request: {} {} | IP: {}",
                    request.getMethod(),
                    getRequestURI(request),
                    getClientIp(request));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long duration = System.currentTimeMillis() - startTime;

        if (ex != null) {
            logger.error("Request failed: {} {} | Status: {} | Duration: {}ms | Error: {}",
                    request.getMethod(),
                    getRequestURI(request),
                    response.getStatus(),
                    duration,
                    ex.getMessage());
        } else if (response.getStatus() >= 400) {
            logger.warn("Request failed: {} {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    getRequestURI(request),
                    response.getStatus(),
                    duration);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Request completed: {} {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    getRequestURI(request),
                    response.getStatus(),
                    duration);
        }
    }

    private String getRequestURI(HttpServletRequest request) {
        String query = request.getQueryString();
        return query != null ? request.getRequestURI() + "?" + query : request.getRequestURI();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}