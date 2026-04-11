package com.example.blog.controller;

import com.example.blog.constant.UserConstants;
import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.UserRegistrationRequest;
import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.AuthResponse;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.service.AuthService;
import com.example.blog.service.CaptchaService;
import com.example.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        // Verify captcha first
        if (!captchaService.verifyCaptcha(request.getCaptchaId(), request.getCaptchaCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    /**
     * 管理员登录接口
     * 只允许 ADMIN 角色的用户登录
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(
            @Valid @RequestBody LoginRequest request) {
        // First do normal login
        User user;
        if (request.getUsernameOrEmail().contains("@")) {
            user = userService.findByEmail(request.getUsernameOrEmail());
        } else {
            user = userService.findByUsername(request.getUsernameOrEmail());
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!UserConstants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("您不是管理员，无法使用管理员登录");
        }

        // Use authService to login
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("管理员登录成功", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        AuthResponse response = authService.refreshToken(cleanToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }
}