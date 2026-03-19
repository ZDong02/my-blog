package com.example.blog.service;

import com.example.blog.dto.request.UserProfileUpdateRequest;
import com.example.blog.dto.request.UserRegistrationRequest;
import com.example.blog.dto.request.PasswordChangeRequest;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationRequest request) {
        // Check if username exists
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("Username already exists");
        }

        // Check if email exists
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException("Email already exists");
        }

        // Check if this is the first user (will become admin)
        boolean isFirstUser = userMapper.countTotalUsers() == 0;

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setAvatar("/images/default-avatar.svg");
        user.setRole(isFirstUser ? "ADMIN" : "USER");
        user.setStatus(1);
        user.setIsFirstUser(isFirstUser ? 1 : 0);

        userMapper.insert(user);

        // If this was the first user, mark that we've handled the first user setup
        if (isFirstUser) {
            userMapper.markAsNotFirstUser(user.getId());
        }

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
}