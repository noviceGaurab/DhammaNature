package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.DiscussionTopic;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscussionTopicRepository extends JpaRepository<DiscussionTopic, Integer> {
    Optional<DiscussionTopic> findBySlug(String slug);

    /** Newest discussion topics with comments eagerly loaded, for the community hub. */
    @EntityGraph(attributePaths = "comments")
    List<DiscussionTopic> findTop5ByOrderByCreatedAtDesc();
}
