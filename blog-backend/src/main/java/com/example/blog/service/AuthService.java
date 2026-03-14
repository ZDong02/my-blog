package com.example.blog.service;

import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.UserRegistrationRequest;
import com.example.blog.dto.response.AuthResponse;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(UserRegistrationRequest request) {
        User user = userService.registerUser(request);

        String token = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateToken(user); // In production, use different refresh token logic

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(86400000L) // 24 hours
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user;

        // Check if login is by username or email
        if (request.getUsernameOrEmail().contains("@")) {
            user = userService.findByEmail(request.getUsernameOrEmail());
        } else {
            user = userService.findByUsername(request.getUsernameOrEmail());
        }

        if (user == null) {
            throw new BusinessException("Invalid username/email or password");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("User account is disabled");
        }

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String token = tokenProvider.generateToken(user);
            String refreshToken = tokenProvider.generateToken(user);

            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .expiresIn(86400000L)
                    .build();
        } else {
            throw new BusinessException("Invalid username/email or password");
        }
    }

    public AuthResponse refreshToken(String token) {
        if (tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            User user = userService.findById(userId);

            if (user != null && user.getStatus() == 1) {
                String newToken = tokenProvider.generateToken(user);
                String newRefreshToken = tokenProvider.generateToken(user);

                return AuthResponse.builder()
                        .token(newToken)
                        .refreshToken(newRefreshToken)
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .expiresIn(86400000L)
                        .build();
            }
        }

        throw new BusinessException("Invalid or expired token");
    }
}