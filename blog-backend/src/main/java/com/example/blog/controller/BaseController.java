package com.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.common.PageUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础控制器示例
 * 演示分页参数处理和响应格式
 */
@RestController
public class BaseController {

    /**
     * 分页参数处理示例
     *
     * @param page 页码，默认为 1
     * @param size 每页大小，默认为 10
     * @param sort 排序字段，可选
     * @param order 排序方式：asc/desc，可选
     * @return 分页参数验证结果
     */
    @GetMapping("/api/test/page")
    public Map<String, Object> testPageParams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "desc") String order) {

        // 验证分页参数
        int[] validatedParams = PageUtils.validatePageParams(page, size);
        int validatedPage = validatedParams[0];
        int validatedSize = validatedParams[1];

        Map<String, Object> result = new HashMap<>();
        result.put("originalPage", page);
        result.put("originalSize", size);
        result.put("validatedPage", validatedPage);
        result.put("validatedSize", validatedSize);
        result.put("sort", sort);
        result.put("order", order);
        result.put("maxPageSize", PageUtils.MAX_PAGE_SIZE);
        result.put("defaultPageSize", PageUtils.DEFAULT_PAGE_SIZE);

        return result;
    }

    /**
     * 标准分页响应格式
     *
     * @param page 页码
     * @param size 每页大小
     * @return 模拟的分页响应
     */
    @GetMapping("/api/test/page-response")
    public Map<String, Object> testPageResponse(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 模拟分页数据
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", new Object[]{}); // 实际数据列表
        pageData.put("total", 100L); // 总记录数
        pageData.put("size", size); // 每页大小
        pageData.put("current", page); // 当前页码
        pageData.put("pages", 10L); // 总页数

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", pageData);

        return result;
    }
}