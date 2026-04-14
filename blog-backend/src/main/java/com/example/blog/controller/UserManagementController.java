package com.example.blog.controller;

import com.example.blog.constant.UserConstants;
import com.example.blog.dto.request.BatchDeleteRequest;
import com.example.blog.dto.request.BatchStatusUpdateRequest;
import com.example.blog.dto.request.PasswordChangeRequest;
import com.example.blog.dto.request.UserRoleUpdateRequest;
import com.example.blog.dto.request.UserStatusUpdateRequest;
import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.UserStatsResponse;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        // Remove password from all users
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateRequest request,
            @AuthenticationPrincipal JwtUserDetails currentAdmin) {

        // Prevent self-demotion
        if (currentAdmin.getId().equals(userId)) {
            throw new BusinessException("无法修改自己的角色");
        }

        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // Validate role
        String newRole = request.getRole();
        if (!UserConstants.ROLE_ADMIN.equals(newRole) && !UserConstants.ROLE_USER.equals(newRole)) {
            throw new BusinessException("无效的角色");
        }

        user.setRole(newRole);
        userService.updateUserRole(userId, newRole);

        User updatedUser = userService.findById(userId);
        updatedUser.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success("用户角色已更新", updatedUser));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal JwtUserDetails currentAdmin) {

        // Prevent self-deactivation
        if (currentAdmin.getId().equals(userId)) {
            throw new BusinessException("无法修改自己的状态");
        }

        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("用户状态已更新", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal JwtUserDetails currentAdmin) {

        // Prevent self-deletion
        if (currentAdmin.getId().equals(userId)) {
            throw new BusinessException("无法删除自己的账号");
        }

        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("用户已删除", null));
    }

    @PutMapping("/batch/status")
    public ResponseEntity<ApiResponse<Void>> batchUpdateStatus(
            @Valid @RequestBody BatchStatusUpdateRequest request,
            @AuthenticationPrincipal JwtUserDetails currentAdmin) {

        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }

        // Prevent self status change through batch
        request.getUserIds().removeIf(id -> currentAdmin.getId().equals(id));

        if (!request.getUserIds().isEmpty()) {
            userService.batchUpdateUserStatus(request.getUserIds(), request.getStatus());
        }

        return ResponseEntity.ok(ApiResponse.success("批量更新状态成功", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteUsers(
            @Valid @RequestBody BatchDeleteRequest request,
            @AuthenticationPrincipal JwtUserDetails currentAdmin) {

        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }

        // Prevent self deletion through batch
        request.getUserIds().removeIf(id -> currentAdmin.getId().equals(id));

        if (!request.getUserIds().isEmpty()) {
            userService.batchDeleteUsers(request.getUserIds());
        }

        return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        UserStatsResponse stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
