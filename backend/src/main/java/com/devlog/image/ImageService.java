package com.devlog.image;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {

    private static final String UPLOAD_DIR = "uploads";
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("이미지 파일(jpg, png, gif, webp)만 업로드 가능합니다.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID() + ext;
        Path target = Paths.get(UPLOAD_DIR, filename);

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장에 실패했습니다.", e);
        }

        return "/uploads/" + filename;
    }

    private String resolveExtension(String original, String contentType) {
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        return switch (contentType) {
            case "image/png"  -> ".png";
            case "image/gif"  -> ".gif";
            case "image/webp" -> ".webp";
            default           -> ".jpg";
        };
    }
}
