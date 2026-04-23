package com.example.blog.config;

import com.example.blog.exception.BusinessException;
import com.example.blog.service.RateLimitService;
import com.example.blog.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginRateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (uri.endsWith("/login") || uri.endsWith("/admin/login")) {
            String ip = IpUtils.getClientIP(request);
            String identifier = ip;

            String username = request.getParameter("usernameOrEmail");
            if (username != null && !username.contains("@")) {
                identifier = username + ":" + ip;
            }

            if (rateLimitService.isLockedOut(identifier)) {
                long remainingTime = rateLimitService.getRemainingLockoutTime(identifier);
                throw new BusinessException(
                    "登录失败次数过多，请等待 " + remainingTime + " 秒后再试",
                    429
                );
            }

            if (rateLimitService.isGlobalRateLimited(ip)) {
                throw new BusinessException(
                    "请求过于频繁，请稍后再试",
                    429
                );
            }
        }
        return true;
    }
}