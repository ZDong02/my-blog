package com.example.blog.service;

import com.example.blog.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private long maxSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/flac", "audio/aac", "audio/m4a", "audio/x-m4a"
    );

    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = Arrays.asList(
            ".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a"
    );

    // 文件头签名验证
    private static final Map<String, byte[]> IMAGE_SIGNATURES = new HashMap<>();
    static {
        // JPEG
        IMAGE_SIGNATURES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        // PNG
        IMAGE_SIGNATURES.put("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        // GIF
        IMAGE_SIGNATURES.put("gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        // WebP
        IMAGE_SIGNATURES.put("webp", new byte[]{0x52, 0x49, 0x46, 0x46});
    }

    /**
     * 上传单个文件（图片）
     */
    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 返回访问 URL（包含API上下文路径）
        return "/api/uploads/" + filename;
    }

    /**
     * 上传音频文件
     */
    public String uploadAudioFile(MultipartFile file) throws IOException {
        validateAudioFile(file);

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 返回访问 URL（包含API上下文路径）
        return "/api/uploads/" + filename;
    }

    /**
     * 上传多个文件
     */
    public List<String> uploadFiles(MultipartFile[] files) throws IOException {
        return Arrays.stream(files)
                .map(file -> {
                    try {
                        return uploadFile(file);
                    } catch (IOException e) {
                        throw new BusinessException("File upload failed: " + e.getMessage());
                    }
                })
                .toList();
    }

    /**
     * 删除文件
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            // 移除开头的 /uploads/ 或 /api/uploads/ 前缀
            String filename = filePath;
            if (filename.startsWith("/api/uploads/")) {
                filename = filename.replace("/api/uploads/", "");
            } else if (filename.startsWith("/uploads/")) {
                filename = filename.replace("/uploads/", "");
            }
            Path path = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException("File size exceeds limit. Max size: " + (maxSize / 1024 / 1024) + "MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("Invalid file type. Allowed types: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        // 验证文件内容签名
        validateFileSignature(file, extension);
    }

    /**
     * 验证音频文件
     */
    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException("File size exceeds limit. Max size: " + (maxSize / 1024 / 1024) + "MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType)) {
            throw new BusinessException("Invalid audio file type. Allowed types: " + String.join(", ", ALLOWED_AUDIO_EXTENSIONS));
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("Invalid audio file extension. Allowed extensions: " + String.join(", ", ALLOWED_AUDIO_EXTENSIONS));
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

    /**
     * 验证文件签名
     */
    private void validateFileSignature(MultipartFile file, String extension) {
        try {
            byte[] fileHeader = new byte[10];
            try (InputStream is = file.getInputStream()) {
                int bytesRead = is.read(fileHeader);
                if (bytesRead < 3) {
                    throw new BusinessException("File is too small to validate");
                }
            }

            String ext = extension.substring(1); // 移除点号
            byte[] expectedSignature = IMAGE_SIGNATURES.get(ext);

            if (expectedSignature != null) {
                boolean isValid = true;
                for (int i = 0; i < Math.min(expectedSignature.length, fileHeader.length); i++) {
                    if (fileHeader[i] != expectedSignature[i]) {
                        isValid = false;
                        break;
                    }
                }

                if (!isValid) {
                    throw new BusinessException("File content does not match expected image format");
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to validate file: " + e.getMessage());
        }
    }
}
