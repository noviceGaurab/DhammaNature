package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.dto.CommentView;
import io.virinchi.dhammanature.dto.ParticipantView;
import io.virinchi.dhammanature.model.Comment;
import io.virinchi.dhammanature.model.DiscussionTopic;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.repository.CommentRepository;
import io.virinchi.dhammanature.repository.DiscussionTopicRepository;
import io.virinchi.dhammanature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * FR-06: Community Discussion Forum. Every page-level comment widget from the
 * old site (community_discuss, sermon1_discuss, ...) is now backed by a
 * DiscussionTopic looked up/created by a stable slug, so comments carry a
 * real foreign key instead of a bare page-name string.
 */
@Service
@RequiredArgsConstructor
public class DiscussionService {

    private static final long AFK_THRESHOLD_DAYS = 7;

    private final DiscussionTopicRepository topicRepository;
    private final CommentRepository commentRepository;
    private final RewardService rewardService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${app.base-url:}")
    private String baseUrl;

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

    /** Nested comment tree ready for rendering, with @mentions highlighted and replies grouped under parents. */
    public List<CommentView> commentTree(String slug) {
        return toTree(commentsFor(slug), registeredNames());
    }

    public List<Comment> recent() {
        return commentRepository.findAllByOrderByCreatedAtDesc();
    }

    public long totalPosts() {
        return commentRepository.countByHiddenFalse();
    }

    public long topicCount() {
        return topicRepository.count();
    }

    public long participants() {
        return commentRepository.countDistinctParticipants();
    }

    /** Registered members + named guests who took part in any discussion (for the participants modal). */
    public List<ParticipantView> participantList() {
        LocalDateTime now = LocalDateTime.now();
        List<ParticipantView> views = new ArrayList<>();
        commentRepository.findDistinctRegisteredParticipants().forEach(u ->
                views.add(new ParticipantView(u.getId(), u.getFullName(), u.getBio(),
                        imageUrl(u.getProfileImage()), u.getRole().name(), false,
                        u.getLastSeenAt() == null || u.getLastSeenAt().isBefore(now.minusDays(AFK_THRESHOLD_DAYS)),
                        daysAway(u.getLastSeenAt(), now), u.getLastSeenAt(), initial(u.getFullName()))));
        commentRepository.findDistinctGuestNames().forEach(name ->
                views.add(new ParticipantView(null, name, null, null, "GUEST", true, false, 0, null, initial(name))));
        return views;
    }

    @Transactional
    public Comment post(String slug, String topicTitle, User user, String guestName, String guestEmail,
                        String title, String content, Integer parentId) {
        DiscussionTopic topic = topicFor(slug, topicTitle);
        Comment parent = parentId == null ? null
                : commentRepository.findById(parentId).orElse(null);
        Comment comment = commentRepository.save(Comment.builder()
                .topic(topic)
                .user(user)
                .name(user != null ? user.getFullName() : guestName)
                .email(user != null ? user.getEmail() : guestEmail)
                .title(title == null || title.isBlank() ? null : title.trim())
                .content(content)
                .parent(parent)
                .build());
        if (user != null) {
            rewardService.awardPoints(user, 2, "Joined the discussion on \"" + topic.getTitle() + "\"");
            notifyMentions(comment.getContent(), user);
        }
        if (parent != null) {
            notifyReplyDelivered(parent, comment, user != null ? user.getFullName() : guestName);
        }
        return comment;
    }

    /** Admin reply: posts a comment under the same topic as the target parent comment, visibly threaded as a reply. */
    @Transactional
    public Comment reply(Integer commentId, User admin, String content) {
        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        Comment reply = commentRepository.save(Comment.builder()
                .topic(parent.getTopic())
                .user(admin)
                .name(admin.getFullName())
                .email(admin.getEmail())
                .content(content)
                .parent(parent)
                .build());
        notifyReplyDelivered(parent, reply, admin.getFullName());
        return reply;
    }

    /** Hard-deletes a comment. */
    @Transactional
    public void delete(Integer commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NoSuchElementException("Comment not found");
        }
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public void hide(Integer commentId) {
        commentRepository.findById(commentId).ifPresent(c -> {
            c.setHidden(true);
            commentRepository.save(c);
        });
    }

    @Transactional
    public void unhide(Integer commentId) {
        commentRepository.findById(commentId).ifPresent(c -> {
            c.setHidden(false);
            commentRepository.save(c);
        });
    }

    /** Notifies every registered participant whose name appears as an @mention inside a new post. */
    private void notifyMentions(String content, User sender) {
        if (content == null || content.isBlank()) {
            return;
        }
        Map<String, User> byName = new LinkedHashMap<>();
        for (User u : commentRepository.findDistinctRegisteredParticipants()) {
            byName.put(u.getFullName().toLowerCase(), u);
        }
        if (byName.isEmpty()) {
            return;
        }
        String lower = content.toLowerCase();
        byName.forEach((nameLower, target) -> {
            if (lower.contains("@" + nameLower) && !target.getId().equals(sender.getId())) {
                notificationService.notifyUser(target, sender.getFullName() + " mentioned you in a discussion post",
                        "\"" + shortText(content) + "\"",
                        NotificationType.NUDGE);
                emailService.sendToUser(target, sender.getFullName() + " mentioned you in a discussion post",
                        "Hi " + target.getFullName() + ",\n\n"
                                + sender.getFullName() + " mentioned you in the Dhamma Nature discussion:\n"
                                + "\"" + shortText(content) + "\"\n\n"
                                + "Join the discussion: " + baseUrl + "/discuss\n\n"
                                + "With metta,\nThe Dhamma Nature team");
            }
        });
    }

    /** Nudges (in-app + email) the author of the parent comment when someone replies to it, and alerts the site owner. */
    private void notifyReplyDelivered(Comment parent, Comment reply, String replierName) {
        User parentAuthor = parent.getUser();
        boolean replyingToSelf = parentAuthor != null && reply.getUser() != null
                && parentAuthor.getId().equals(reply.getUser().getId());
        if (parentAuthor != null && !replyingToSelf) {
            notificationService.notifyUser(parentAuthor, "Someone replied to your comment",
                    "\"" + shortText(reply.getContent()) + "\"", NotificationType.SYSTEM);
            emailService.sendToUser(parentAuthor, "Someone replied to your comment",
                    "Hi " + parentAuthor.getFullName() + ",\n\n"
                            + replierName + " replied to your comment in the Dhamma Nature discussion:\n"
                            + "\"" + shortText(reply.getContent()) + "\"\n\n"
                            + "Join the discussion: " + baseUrl + "/discuss\n\n"
                            + "With metta,\nThe Dhamma Nature team");
        }
        emailService.sendSiteAlert("New discussion reply",
                replierName + " replied to a comment"
                        + (parentAuthor != null ? " by " + parentAuthor.getFullName() : "")
                        + (reply.getTopic() != null ? " on \"" + reply.getTopic().getTitle() + "\"" : "") + ".");
    }

    private String shortText(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String one = s.strip().replaceAll("\\s+", " ");
        return one.length() > 80 ? one.substring(0, 80) + "..." : one;
    }

    // ===== Rendering helpers =====

    private List<String> registeredNames() {
        return commentRepository.findDistinctRegisteredParticipants().stream()
                .map(User::getFullName)
                .toList();
    }

    private List<CommentView> toTree(List<Comment> flat, List<String> names) {
        Map<Integer, Comment> byId = new LinkedHashMap<>();
        for (Comment c : flat) {
            byId.put(c.getId(), c);
        }
        List<CommentView> roots = new ArrayList<>();
        for (Comment c : flat) {
            Comment parent = (c.getParent() != null) ? byId.get(c.getParent().getId()) : null;
            if (parent == null) {
                roots.add(toView(c, byId, names));
            }
        }
        return roots;
    }

    private CommentView toView(Comment c, Map<Integer, Comment> byId, List<String> names) {
        String name = (c.getName() == null || c.getName().isBlank()) ? "Guest" : c.getName();
        User author = c.getUser();
        List<CommentView> replyViews = new ArrayList<>();
        if (c.getReplies() != null) {
            for (Comment reply : c.getReplies()) {
                if (!reply.isHidden()) {
                    replyViews.add(toView(reply, byId, names));
                }
            }
        }
        return new CommentView(c.getId(), c.getTitle(), renderMentions(c.getContent(), names),
                name, initial(name), imageUrl(author != null ? author.getProfileImage() : null),
                author != null ? author.getId() : null, c.getCreatedAt(),
                c.getParent() == null ? null : c.getParent().getId(), replyViews);
    }

    /** Escapes the raw text and wraps @participant mentions in a highlighted span. */
    private String renderMentions(String raw, List<String> names) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String escaped = HtmlUtils.htmlEscape(raw);
        if (names.isEmpty()) {
            return escaped;
        }
        List<String> sorted = names.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .toList();
        String pattern = sorted.stream().map(Pattern::quote).collect(Collectors.joining("|"));
        Pattern p = Pattern.compile("(?i)@(" + pattern + ")\\b");
        Matcher m = p.matcher(escaped);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String mention = "@" + m.group(1);
            m.appendReplacement(sb, "<span class=\"mention\">" + Matcher.quoteReplacement(mention) + "</span>");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String imageUrl(String profileImage) {
        return profileImage == null || profileImage.isBlank() ? null : "/uploads/" + profileImage;
    }

    private String initial(String name) {
        return name == null || name.isBlank() ? "?" : name.substring(0, 1).toUpperCase();
    }

    private long daysAway(LocalDateTime lastSeen, LocalDateTime now) {
        return lastSeen == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(lastSeen.toLocalDate(), now.toLocalDate());
    }
}