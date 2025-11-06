package com.example.backend_quanlynhahanglau.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        try {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "overwrite", true,
                    "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"}
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Upload ảnh thành công: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("Lỗi khi upload ảnh lên Cloudinary: {}", e.getMessage());
            throw new IOException("Không thể upload ảnh lên Cloudinary: " + e.getMessage(), e);
        }
    }

    public boolean deleteImage(String imageUrl) {
        try {
            // Lấy public_id từ URL
            String publicId = extractPublicId(imageUrl);
            if (publicId == null) {
                log.warn("Không thể trích xuất public_id từ URL: {}", imageUrl);
                return false;
            }

            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            boolean deleted = (Boolean) result.get("result");
            if (deleted) {
                log.info("Xóa ảnh thành công: {}", publicId);
            } else {
                log.warn("Không thể xóa ảnh: {}", publicId);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Lỗi khi xóa ảnh khỏi Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    private String extractPublicId(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
                return null;
            }

            // Tìm phần sau "/upload/"
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String afterUpload = imageUrl.substring(uploadIndex + "/upload/".length());
            
            // Bỏ qua version nếu có (v1234567890/)
            if (afterUpload.matches("^v\\d+/.*")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            // Bỏ phần extension
            int lastDot = afterUpload.lastIndexOf(".");
            if (lastDot != -1) {
                afterUpload = afterUpload.substring(0, lastDot);
            }

            return afterUpload;
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất public_id: {}", e.getMessage());
            return null;
        }
    }
}

