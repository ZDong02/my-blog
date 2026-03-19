package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.mapper.CategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryMapper.findAllWithPostCount();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Category category = categoryMapper.selectWithPostCount(id);
        if (category == null) {
            return ResponseEntity.ok(ApiResponse.error("Category not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        // Check if category name already exists
        Category existing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, category.getName()));
        if (existing != null) {
            return ResponseEntity.ok(ApiResponse.error("Category name already exists"));
        }
        categoryMapper.insert(category);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.error("Category not found"));
        }

        // Check if new name already exists (excluding current category)
        if (!existing.getName().equals(category.getName())) {
            Category duplicate = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getName, category.getName()));
            if (duplicate != null) {
                return ResponseEntity.ok(ApiResponse.error("Category name already exists"));
            }
        }

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        categoryMapper.updateById(existing);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCategory(@PathVariable Long id) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.ok(ApiResponse.error("Category not found"));
        }

        // Check if category has posts using PostMapper
        com.example.blog.mapper.PostMapper postMapper = getPostMapper();
        long postCount = 0;
        if (postMapper != null) {
            postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                    .eq(Post::getCategoryId, id));
        }

        if (postCount > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("hasPosts", true);
            data.put("postCount", postCount);
            return ResponseEntity.ok(ApiResponse.errorWithData("Cannot delete category with existing posts", data));
        }

        categoryMapper.deleteById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", data));
    }

    // Helper method to get PostMapper
    @Autowired(required = false)
    private com.example.blog.mapper.PostMapper postMapper;

    private com.example.blog.mapper.PostMapper getPostMapper() {
        return postMapper;
    }
}