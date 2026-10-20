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

/**
 * 图片上传服务：本地磁盘存储。
 * 校验分三层——大小、Content-Type、扩展名、文件头魔数，防止伪装上传。
 */
@Service
public class FileUploadService {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    /** 各图片格式的文件头特征，用于校验真实类型 */
    private static final Map<String, byte[]> IMAGE_SIGNATURES = new HashMap<>();
    static {
        IMAGE_SIGNATURES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        IMAGE_SIGNATURES.put("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        IMAGE_SIGNATURES.put("gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        IMAGE_SIGNATURES.put("webp", new byte[]{0x52, 0x49, 0x46, 0x46});
    }

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}")
    private long maxSize;

    /**
     * 上传单个图片，返回可访问的相对路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        String filename = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        return saveToLocal(file, filename);
    }

    /**
     * 批量上传图片
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

    private String saveToLocal(MultipartFile file, String filename) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }

        return "/api/uploads/" + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException("File size exceeds limit. Max size: " + (maxSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("Invalid file type. Allowed types: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        validateFileSignature(file, extension);
    }

    /** 校验文件头魔数，防止改扩展名伪装上传 */
    private void validateFileSignature(MultipartFile file, String extension) {
        try {
            byte[] header = new byte[10];
            try (InputStream is = file.getInputStream()) {
                if (is.read(header) < 3) {
                    throw new BusinessException("File is too small to validate");
                }
            }

            byte[] expected = IMAGE_SIGNATURES.get(extension.substring(1));
            if (expected == null) {
                return;
            }
            for (int i = 0; i < Math.min(expected.length, header.length); i++) {
                if (header[i] != expected[i]) {
                    throw new BusinessException("File content does not match expected image format");
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to validate file: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }
}
