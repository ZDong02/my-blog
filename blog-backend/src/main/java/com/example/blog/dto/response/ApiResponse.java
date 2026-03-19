package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 统一 API 响应包装类
 *
 * @param <T> 响应数据类型
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@Builder
public class ApiResponse<T> {

    /**
     * 请求是否成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @return API 响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 成功响应（带消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @return API 响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     *
     * @param message 错误消息
     * @return API 响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * 失败响应（带数据）
     *
     * @param message 错误消息
     * @param data    响应数据
     * @return API 响应
     */
    public static <T> ApiResponse<T> errorWithData(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}