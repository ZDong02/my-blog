package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Category;
import com.example.blog.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(null);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return ResponseEntity.ok(ApiResponse.error("Category not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}