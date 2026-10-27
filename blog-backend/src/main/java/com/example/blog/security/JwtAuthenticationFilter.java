package com.example.blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip JWT authentication for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        // 没有 token 时保持匿名，是否放行交给 SecurityConfig 的授权规则判断。
        // 注意：不能因为「路径看起来是公开接口」就跳过后面的 token 解析 ——
        // 否则 GET /posts/my-posts 这类既公开前缀、又需要当前用户身份的接口会拿不到 principal。
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            // 公开接口带着失效用户的历史 token 时不应阻断浏览，按匿名放行
            if (!isPublicRequest(request)) {
                sendUnauthorized(response, "Invalid authentication token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        return path.startsWith("/auth/")
                || path.startsWith("/captcha/")
                || path.startsWith("/uploads/")
                || path.startsWith("/api/uploads/")
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/posts") && !path.startsWith("/posts/admin/"))
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/tags"))
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/categories"))
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/comments/post/"));
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
