package io.virinchi.dhammanature.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A DTO describing one comment for the discuss page, with @mention highlighting
 * already applied to {@code contentHtml} and nested replies pre-built into a tree.
 */
public record CommentView(
        Integer id,
        String title,
        String contentHtml,
        String rawContent,
        String authorName,
        String authorInitial,
        String profileImage,
        Integer authorId,
        LocalDateTime createdAt,
        Integer parentId,
        List<CommentView> replies
) {
}