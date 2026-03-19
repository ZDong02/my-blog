package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求 DTO
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
public class LoginRequest {

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(min = 3, max = 50, message = "用户名或邮箱长度必须在 3 到 50 个字符之间")
    private String usernameOrEmail;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在 8 到 100 个字符之间")
    private String password;
}