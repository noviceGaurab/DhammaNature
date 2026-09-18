package io.virinchi.dhammanature.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Central file storage for user-uploaded content (profile photos, etc.).
 * Files live under {@code app.upload.dir} (default "uploads") and are served statically at /uploads/**.
 */
@Component
public class StorageConfig {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    private final Path uploadRoot;

    public StorageConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void ensureDirectory() throws IOException {
        Files.createDirectories(uploadRoot);
    }

    /** Persists an image and returns its stored filename. Throws on disallowed type or IO failure. */
    public String saveImage(MultipartFile file) {
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF, WEBP and BMP images are allowed.");
        }
        String ext = extensionFor(contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadRoot.resolve(filename);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store uploaded image.", e);
        }
        return filename;
    }

    public Path root() {
        return uploadRoot;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".jpg";
        };
    }
}