package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Comment;
import io.virinchi.dhammanature.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByTopic_IdAndHiddenFalseOrderByCreatedAtAsc(Integer topicId);

    List<Comment> findByTopic_Id(Integer topicId);

    void deleteByTopic_Id(Integer topicId);

    @EntityGraph(attributePaths = {"topic", "user"})
    List<Comment> findAllByOrderByCreatedAtDesc();

    long countByHiddenFalse();

    @Query("select count(distinct case when c.user is not null then concat('u', c.user.id) else concat('g', c.name) end) from Comment c where c.hidden = false")
    long countDistinctParticipants();

    /** Registered members who have ever posted in the discussion area (for the participants modal). */
    @Query("select distinct c.user from Comment c where c.hidden = false and c.user is not null order by c.user.fullName")
    List<User> findDistinctRegisteredParticipants();

    /** Anonymous visitors who left a name while posting (for the participants modal). */
    @Query("select distinct c.name from Comment c where c.hidden = false and c.user is null and c.name is not null order by c.name")
    List<String> findDistinctGuestNames();
}