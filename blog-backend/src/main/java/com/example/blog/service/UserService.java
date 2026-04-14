package com.example.blog.service;

import com.example.blog.constant.UserConstants;
import com.example.blog.dto.request.UserProfileUpdateRequest;
import com.example.blog.dto.request.UserRegistrationRequest;
import com.example.blog.dto.request.PasswordChangeRequest;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.blog.dto.response.UserStatsResponse;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.invite-code:ADMIN2026}")
    private String adminInviteCode;

    public User registerUser(UserRegistrationRequest request) {
        // Check if username exists
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // Check if email exists
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // Check if this is the first user (will become admin automatically)
        boolean isFirstUser = userMapper.countTotalUsers() == 0;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setAvatar("/images/default-avatar.svg");
        user.setStatus(1);

        // Determine role
        String requestedRole = request.getRole();
        if (isFirstUser) {
            // First user is always admin
            user.setRole(UserConstants.ROLE_ADMIN);
            user.setIsFirstUser(1);
        } else if (UserConstants.ROLE_ADMIN.equalsIgnoreCase(requestedRole)) {
            // Admin registration requires valid invite code
            if (request.getAdminCode() == null || !request.getAdminCode().equals(adminInviteCode)) {
                throw new BusinessException("管理员邀请码不正确");
            }
            user.setRole(UserConstants.ROLE_ADMIN);
            user.setIsFirstUser(0);
        } else {
            // Default to USER role
            user.setRole(UserConstants.ROLE_USER);
            user.setIsFirstUser(0);
        }

        userMapper.insert(user);

        return user;
    }

    public User updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userMapper.updateProfile(user);
        return userMapper.selectById(userId);
    }

    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public List<User> findAllUsers() {
        return userMapper.findAllUsers();
    }

    public void updateUserRole(Long userId, String role) {
        userMapper.updateUserRole(userId, role);
    }

    public void updateUserStatus(Long userId, Integer status) {
        userMapper.updateUserStatus(userId, status);
    }

    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    public void batchUpdateUserStatus(List<Long> userIds, Integer status) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        userMapper.batchUpdateStatus(userIds, status);
    }

    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        userMapper.batchDeleteUsers(userIds);
    }

    public UserStatsResponse getUserStats() {
        UserStatsResponse stats = new UserStatsResponse();
        stats.setTotalUsers(userMapper.countTotalUsers());
        stats.setTodayNewUsers(userMapper.countTodayNewUsers());
        stats.setMonthNewUsers(userMapper.countMonthNewUsers());
        stats.setActiveUsers(userMapper.countActiveUsers());
        stats.setRoleDistribution(userMapper.countRoleDistribution());

        List<Map<String, Object>> rawTrend = userMapper.getGrowthTrend(30);
        List<UserStatsResponse.GrowthData> growthTrend = rawTrend.stream()
            .map(m -> {
                UserStatsResponse.GrowthData data = new UserStatsResponse.GrowthData();
                data.setDate(m.get("date").toString());
                data.setCount(((Number) m.get("count")).intValue());
                return data;
            })
            .collect(Collectors.toList());
        stats.setGrowthTrend(growthTrend);

        return stats;
    }

    public List<User> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectUsersByIds(userIds);
    }
}