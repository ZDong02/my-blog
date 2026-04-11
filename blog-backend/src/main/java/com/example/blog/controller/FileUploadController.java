package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

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
            logger.info("File uploaded successfully: {}", file.getOriginalFilename());
            return ResponseEntity.ok(ApiResponse.success("文件上传成功", data));
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return ResponseEntity.ok(ApiResponse.error("文件上传失败，请检查文件格式和大小"));
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
            logger.info("Multiple files uploaded successfully: {} files", urls.size());
            return ResponseEntity.ok(ApiResponse.success("文件批量上传成功", data));
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return ResponseEntity.ok(ApiResponse.error("文件上传失败，请检查文件格式和大小"));
        }
    }
}
