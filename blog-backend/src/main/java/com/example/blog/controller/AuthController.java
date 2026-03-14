package com.example.blog.controller;

import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.UserRegistrationRequest;
import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.AuthResponse;
import com.example.blog.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
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