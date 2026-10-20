package com.example.blog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginRateLimitInterceptor loginRateLimitInterceptor;

    /** 图片上传目录，与 application.yml 的 file.upload-dir 保持一致 */
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginRateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/uploads/**", "/static/**", "/public/**");
    }

    /**
     * 将上传目录映射为可访问的静态资源路径。
     * 上传接口返回的 /api/uploads/xxx 由此映射到磁盘文件。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + location);
    }
}
