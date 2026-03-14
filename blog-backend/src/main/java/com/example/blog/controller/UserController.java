package com.example.blog.controller;

import com.example.blog.dto.request.UserProfileUpdateRequest;
import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.User;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        User user = userService.findById(userDetails.getId());
        // Remove sensitive information
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        User updatedUser = userService.updateProfile(userDetails.getId(), request);
        // Remove sensitive information
        updatedUser.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        // Remove sensitive information
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}