package com.example.blog.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件和其他拦截器
 */
@Configuration
@MapperScan("com.example.blog.mapper")
public class MyBatisPlusConfig {

    // 分页配置已移除，使用编程方式实现分页功能
    // 这样可以避免 MyBatis-Plus 3.5.3.1 版本的兼容性问题
}