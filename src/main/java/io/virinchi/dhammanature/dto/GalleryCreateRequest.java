package io.virinchi.dhammanature.dto;

/**
 * JSON body accepted by POST /api/gallery, matching the course spec:
 * <pre>{ "title": "...", "description": "..." }</pre>
 */
public record GalleryCreateRequest(String title, String description) {
}