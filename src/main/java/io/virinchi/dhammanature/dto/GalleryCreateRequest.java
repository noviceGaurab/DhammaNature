package io.virinchi.dhammanature.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON body accepted by POST /api/gallery, matching the course spec:
 * <pre>{ "title": "...", "description": "..." }</pre>
 */
public record GalleryCreateRequest(
        @NotBlank(message = "A title is required.")
        @Size(max = 150, message = "Title must be 150 characters or fewer.")
        String title,

        @Size(max = 2000, message = "Description must be 2000 characters or fewer.")
        String description) {
}
