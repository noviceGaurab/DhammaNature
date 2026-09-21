package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.service.BlogService;
import io.virinchi.dhammanature.service.DiscussionService;
import io.virinchi.dhammanature.service.EmailService;
import io.virinchi.dhammanature.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

/**
 * Public blog. The blog used to be a fully static page; it is now backed by
 * user-submitted articles. Anyone can read APPROVED posts; logged-in users can
 * write new articles which stay PENDING until an admin approves them. Comments
 * on each article are backed by the community discussion so replies nest and
 * the author can edit/delete their own words.
 */
@Controller
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final DiscussionService discussionService;
    private final SessionUserResolver sessionUserResolver;
    private final NotificationService notificationService;
    private final EmailService emailService;

    private String slugFor(Integer postId) {
        return "blog-" + postId;
    }

    private void populateComments(Model model, Integer postId) {
        String slug = slugFor(postId);
        discussionService.topicFor(slug, "Comments on a blog article");
        var tree = discussionService.commentTree(slug);
        model.addAttribute("commentSlug", slug);
        model.addAttribute("commentTree", tree);
        model.addAttribute("commentCount", countAll(tree));
    }

    private long countAll(java.util.List<io.virinchi.dhammanature.dto.CommentView> list) {
        long total = list.size();
        for (var c : list) {
            total += countAll(c.replies());
        }
        return total;
    }

    @GetMapping("/blog")
    public String blog(@RequestParam(name = "submitted", required = false) String submitted,
                       @RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 6;
        var paged = blogService.pagedPublished(PageRequest.of(Math.max(0, page - 1), pageSize));
        int current = coercePage(page, paged.getTotalPages());
        if (current != Math.max(1, page)) {
            paged = blogService.pagedPublished(PageRequest.of(current - 1, pageSize));
        }
        model.addAttribute("posts", paged.getContent());
        model.addAttribute("recentPosts", blogService.published().stream().limit(4).toList());
        if ("1".equals(submitted)) {
            model.addAttribute("success",
                    "Your article has been submitted and will be published after an admin approves it.");
        }
        model.addAttribute("page", current);
        model.addAttribute("totalPages", paged.getTotalPages());
        model.addAttribute("pageBase", "/blog");
        return "blog";
    }

    @GetMapping("/blog/view")
    public String blogView(@RequestParam Integer id, HttpSession session, Model model) {
        var postOpt = blogService.publishedById(id);
        if (postOpt.isEmpty()) {
            return "redirect:/blog";
        }
        model.addAttribute("post", postOpt.get());
        model.addAttribute("posts", blogService.published());
        populateComments(model, id);
        var userOpt = sessionUserResolver.resolve(session);
        model.addAttribute("liked", userOpt.isPresent()
                && hasLiked(session, userOpt.get().getId(), postOpt.get().getId()));
        return "blog-detail";
    }

    @PostMapping("/blog/like")
    public String blogLike(@RequestParam Integer id, HttpSession session, Model model) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User liker = userOpt.get();
        var postOpt = blogService.publishedById(id);
        if (postOpt.isEmpty()) {
            return "redirect:/blog";
        }
        if (!hasLiked(session, liker.getId(), postOpt.get().getId())) {
            markLiked(session, liker.getId(), postOpt.get().getId());
            blogService.likeOne(postOpt.get().getId());
            User author = postOpt.get().getAuthor();
            if (author != null && !author.getId().equals(liker.getId())) {
                notificationService.notifyUser(author, liker.getFullName() + " liked your article",
                        "\"" + postOpt.get().getTitle() + "\"", NotificationType.SYSTEM);
                emailService.sendToUser(author, "Someone liked your article",
                        "Hi " + author.getFullName() + ",\n\n"
                                + liker.getFullName() + " liked your article \"" + postOpt.get().getTitle()+ "\" on Dhamma Nature.\n\n"
                                + "With metta,\nThe Dhamma Nature team");
            }
            emailService.sendSiteAlert("Article liked",
                    liker.getFullName() + " (" + liker.getEmail() + ") liked \""
                            + postOpt.get().getTitle() + "\".");
        }
        model.addAttribute("post", postOpt.get());
        model.addAttribute("posts", blogService.published());
        populateComments(model, id);
        return "blog-detail";
    }

    @PostMapping("/blog/{id}/comment")
    public String blogComment(@PathVariable Integer id,
                              @RequestParam(required = false) String name,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String title,
                              @RequestParam(required = false) Integer parentId,
                              @RequestParam String content,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        if (!blogService.publishedById(id).isPresent()) {
            return "redirect:/blog";
        }
        if (user == null && (name == null || name.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "Please enter your name, or log in, to comment.");
            return "redirect:/blog/view?id=" + id;
        }
        discussionService.post(slugFor(id), "Comments on a blog article", user, name, email, title, content, parentId);
        redirectAttributes.addFlashAttribute("success", "Your comment was posted.");
        return "redirect:/blog/view?id=" + id;
    }

    @PostMapping("/blog/comment/{cid}/delete")
    public String blogCommentDelete(@PathVariable Integer cid,
                                    @RequestParam Integer postId,
                                    HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        try {
            discussionService.deleteOwned(cid, user);
            redirectAttributes.addFlashAttribute("success", "Your comment was deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/blog/view?id=" + postId;
    }

    @PostMapping("/blog/comment/{cid}/edit")
    public String blogCommentEdit(@PathVariable Integer cid,
                                  @RequestParam Integer postId,
                                  @RequestParam String content,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        try {
            discussionService.updateOwned(cid, content, user);
            redirectAttributes.addFlashAttribute("success", "Your comment was updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/blog/view?id=" + postId;
    }

    private boolean hasLiked(HttpSession session, Integer userId, Integer postId) {
        @SuppressWarnings("unchecked")
        Set<String> liked = (Set<String>) session.getAttribute("likedPosts");
        return liked != null && liked.contains(postId + ":" + userId);
    }

    private void markLiked(HttpSession session, Integer userId, Integer postId) {
        @SuppressWarnings("unchecked")
        Set<String> liked = (Set<String>) session.getAttribute("likedPosts");
        if (liked == null) {
            liked = new HashSet<>();
            session.setAttribute("likedPosts", liked);
        }
        liked.add(postId + ":" + userId);
    }

    @GetMapping("/blog/new")
    public String blogNew() {
        return "redirect:/blog#write";
    }

    @PostMapping("/blog/submit")
    public String blogSubmit(@RequestParam(required = false) String title,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String imageUrl,
                             @RequestParam(required = false) String content,
                             HttpSession session, Model model) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User author = userOpt.get();
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            model.addAttribute("error", "Please provide an article title and content.");
            model.addAttribute("posts", blogService.published());
            return "blog";
        }
        blogService.submit(title, category, imageUrl, content, author);
        return "redirect:/blog?submitted=1";
    }

    private int coercePage(int requested, int totalPages) {
        return Math.max(1, Math.min(requested, Math.max(1, totalPages)));
    }
}