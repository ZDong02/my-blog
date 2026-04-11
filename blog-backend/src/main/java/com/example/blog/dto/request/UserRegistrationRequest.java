package com.example.blog.dto.request;

import com.example.blog.constant.UserConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求 DTO
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
public class UserRegistrationRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 个字符之间")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在 8 到 100 个字符之间")
    private String password;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    /**
     * 注册角色：USER-普通用户，ADMIN-管理员（需要管理员邀请码）
     */
    private String role = UserConstants.ROLE_USER;

    /**
     * 管理员邀请码（注册管理员时必填）
     */
    private String adminCode;

    /**
     * 图形验证码 ID
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaId;

    /**
     * 图形验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}