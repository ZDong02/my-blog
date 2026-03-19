package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传单个文件
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.uploadFile(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", filePath);
            data.put("filename", file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", data));
        } catch (IOException e) {
            return ResponseEntity.ok(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    /**
     * 上传多个文件
     */
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> urls = fileUploadService.uploadFiles(files);
            Map<String, Object> data = new HashMap<>();
            data.put("urls", urls);
            data.put("count", urls.size());
            return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully", data));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}
