package com.example.blog.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 扫描 Mapper 接口；分页由 Mapper XML 中的 LIMIT 语句实现，无需分页插件。
 */
@Configuration
@MapperScan("com.example.blog.mapper")
public class MyBatisPlusConfig {
}