package com.brevery.service;

import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class CloudinaryService {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    );

    @PostConstruct
    public void init() {
        if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
            log.warn("Cloudinary configuration is missing properties. File uploads may fail.");
            // Khởi tạo một đối tượng trống để tránh NullPointerException khi load bean
            this.cloudinary = new Cloudinary();
        } else {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            log.info("Cloudinary configured successfully.");
        }
    }

    public String uploadImage(MultipartFile file) {
        validateFile(file);

        if (cloudinary == null || cloudName.isEmpty()) {
            log.error("Cloudinary credentials are not configured.");
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Cấu hình Cloudinary không khả dụng");
        }

        try {
            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "folder", "bakery/products",
                    "use_filename", true,
                    "unique_filename", true,
                    "resource_type", "image"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Uploaded file to Cloudinary successfully. URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Lỗi xảy ra trong quá trình kết nối Cloudinary");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "File không được để trống");
        }

        // Validate size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(ErrorCode.FILE_INVALID_TYPE);
        }
    }
}
