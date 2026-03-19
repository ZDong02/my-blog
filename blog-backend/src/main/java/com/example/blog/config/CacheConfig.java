package com.example.blog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for Caffeine
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
                "posts",           // 文章缓存
                "categories",      // 分类缓存
                "tags",            // 标签缓存
                "users",           // 用户缓存
                "comments",        // 评论缓存
                "hotPosts",        // 热门榜单缓存
                "archiveStats"     // 归档统计缓存
        ));
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(3600, TimeUnit.SECONDS)
                .expireAfterWrite(3600, TimeUnit.SECONDS)
                .recordStats());
        return cacheManager;
    }
}