package io.virinchi.dhammanature.config;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * Shared rules for uploaded images. Images are stored as BLOBs in the database and
 * served back through dedicated controllers (e.g. /users/{id}/image), never written
 * to the filesystem (spec Section 2.5).
 */
public final class ImageRules {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    private ImageRules() {
    }

    /** Validates and returns the lower-cased MIME type, or throws with a user-friendly message. */
    public static String requireContentType(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload the " + label + ".");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "The " + label + " must be a JPG, PNG, GIF, WEBP or BMP image.");
        }
        return contentType;
    }

    /** Reads the uploaded bytes, wrapping any IO failure in a runtime exception. */
    public static byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the uploaded image.", e);
        }
    }
}
