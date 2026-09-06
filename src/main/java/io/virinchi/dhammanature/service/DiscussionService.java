package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Comment;
import io.virinchi.dhammanature.model.DiscussionTopic;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.CommentRepository;
import io.virinchi.dhammanature.repository.DiscussionTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FR-06: Community Discussion Forum. Every page-level comment widget from the
 * old site (community_discuss, sermon1_discuss, ...) is now backed by a
 * DiscussionTopic looked up/created by a stable slug, so comments carry a
 * real foreign key instead of a bare page-name string.
 */
@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final DiscussionTopicRepository topicRepository;
    private final CommentRepository commentRepository;
    private final RewardService rewardService;

    @Transactional
    public DiscussionTopic topicFor(String slug, String titleIfCreating) {
        return topicRepository.findBySlug(slug)
                .orElseGet(() -> topicRepository.save(DiscussionTopic.builder()
                        .slug(slug).title(titleIfCreating).build()));
    }

    public List<Comment> commentsFor(String slug) {
        return topicRepository.findBySlug(slug)
                .map(t -> commentRepository.findByTopic_IdAndHiddenFalseOrderByCreatedAtAsc(t.getId()))
                .orElseGet(List::of);
    }

    public List<Comment> recent() {
        return commentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Comment post(String slug, String topicTitle, User user, String guestName, String guestEmail, String content) {
        DiscussionTopic topic = topicFor(slug, topicTitle);
        Comment comment = commentRepository.save(Comment.builder()
                .topic(topic)
                .user(user)
                .name(user != null ? user.getFullName() : guestName)
                .email(user != null ? user.getEmail() : guestEmail)
                .content(content)
                .build());
        if (user != null) {
            rewardService.awardPoints(user, 2, "Joined the discussion on \"" + topic.getTitle() + "\"");
        }
        return comment;
    }

    @Transactional
    public void hide(Integer commentId) {
        commentRepository.findById(commentId).ifPresent(c -> {
            c.setHidden(true);
            commentRepository.save(c);
        });
    }
}
