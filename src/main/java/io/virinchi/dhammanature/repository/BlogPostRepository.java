package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.BlogPost;
import io.virinchi.dhammanature.model.enums.BlogStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {

    List<BlogPost> findByStatusOrderByCreatedAtDesc(BlogStatus status);

    List<BlogPost> findAllByOrderByCreatedAtDesc();

    List<BlogPost> findByAuthor_IdOrderByCreatedAtDesc(Integer authorId);

    long countByStatus(BlogStatus status);
}