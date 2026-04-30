package com.example.blog.service;

import com.example.blog.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}")
    private long maxSize;

    @Value("${minio.enabled:false}")
    private boolean minioEnabled;

    @Value("${minio.bucket:blog}")
    private String minioBucket;

    @Value("${minio.public-url:}")
    private String minioPublicUrl;

    @Autowired(required = false)
    private MinioClient minioClient;

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

    private static final Map<String, byte[]> IMAGE_SIGNATURES = new HashMap<>();
    static {
        IMAGE_SIGNATURES.put("jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        IMAGE_SIGNATURES.put("png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        IMAGE_SIGNATURES.put("gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        IMAGE_SIGNATURES.put("webp", new byte[]{0x52, 0x49, 0x46, 0x46});
    }

    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;

        if (minioEnabled && minioClient != null) {
            return uploadToMinio(file, filename);
        } else {
            return uploadToLocal(file, filename);
        }
    }

    public String uploadAudioFile(MultipartFile file) throws IOException {
        validateAudioFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;

        if (minioEnabled && minioClient != null) {
            return uploadAudioToMinio(file, filename);
        } else {
            return uploadAudioToLocal(file, filename);
        }
    }

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

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        if (minioEnabled && minioClient != null && filePath.contains(minioBucket)) {
            deleteFromMinio(filePath);
        } else {
            deleteFromLocal(filePath);
        }
    }

    private String uploadToMinio(MultipartFile file, String filename) throws IOException {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Return path that will be served by nginx proxy to MinIO
            return "/api/minio/" + filename;
        } catch (Exception e) {
            logger.error("MinIO error: {}", e.getMessage());
            throw new BusinessException("Failed to upload to MinIO: " + e.getMessage());
        }
    }

    private String uploadAudioToMinio(MultipartFile file, String filename) throws IOException {
        return uploadToMinio(file, filename);
    }

    private String uploadToLocal(MultipartFile file, String filename) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/api/uploads/" + filename;
    }

    private String uploadAudioToLocal(MultipartFile file, String filename) throws IOException {
        return uploadToLocal(file, filename);
    }

    private void deleteFromMinio(String filePath) {
        try {
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(filename)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Failed to delete from MinIO: {}", e.getMessage());
        }
    }

    private void deleteFromLocal(String filePath) {
        try {
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

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        validateFileSignature(file, extension);
    }

    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException("File size exceeds limit. Max size: " + (maxSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType)) {
            throw new BusinessException("Invalid audio file type. Allowed types: " + String.join(", ", ALLOWED_AUDIO_EXTENSIONS));
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("Invalid audio file extension. Allowed extensions: " + String.join(", ", ALLOWED_AUDIO_EXTENSIONS));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

    private void validateFileSignature(MultipartFile file, String extension) {
        try {
            byte[] fileHeader = new byte[10];
            try (InputStream is = file.getInputStream()) {
                int bytesRead = is.read(fileHeader);
                if (bytesRead < 3) {
                    throw new BusinessException("File is too small to validate");
                }
            }

            String ext = extension.substring(1);
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