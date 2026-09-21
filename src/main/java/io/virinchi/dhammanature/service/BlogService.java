package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.BlogPost;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.BlogStatus;
import io.virinchi.dhammanature.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Blog articles with admin moderation: users submit PENDING posts, admins
 * approve or reject them, and only APPROVED posts are shown on the public blog.
 */
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final EmailService emailService;

    public List<BlogPost> published() {
        return blogPostRepository.findByStatusOrderByCreatedAtDesc(BlogStatus.APPROVED);
    }

    public Page<BlogPost> pagedPublished(Pageable pageable) {
        return blogPostRepository.findByStatusOrderByCreatedAtDesc(BlogStatus.APPROVED, pageable);
    }

    public Optional<BlogPost> publishedById(Integer id) {
        return blogPostRepository.findById(id)
                .filter(post -> post.getStatus() == BlogStatus.APPROVED);
    }

    public List<BlogPost> forAdmin() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<BlogPost> forUser(Integer userId) {
        return blogPostRepository.findByAuthor_IdOrderByCreatedAtDesc(userId);
    }

    public long pendingCount() {
        return blogPostRepository.countByStatus(BlogStatus.PENDING);
    }

    @Transactional
    public BlogPost submit(String title, String category, String imageUrl, String content, User author) {
        BlogPost saved = blogPostRepository.save(BlogPost.builder()
                .title(title.trim())
                .category(category == null || category.isBlank() ? "general" : category.trim().toLowerCase())
                .imageUrl(imageUrl == null || imageUrl.isBlank() ? "pexels-ajaybhargavguduru-939700.jpg" : imageUrl.trim())
                .content(content.trim())
                .author(author)
                .authorName(author != null ? author.getFullName() : null)
                .status(BlogStatus.PENDING)
                .build());
        if (author != null) {
            emailService.sendToUser(author, "Your article is pending review",
                    "Hi " + author.getFullName() + ",\n\n"
                            + "Your article \"" + saved.getTitle() + "\" has been submitted and will appear "
                            + "on the blog once an administrator approves it.\n\n"
                            + "With metta,\nThe Dhamma Nature team");
        }
        emailService.sendSiteAlert("New blog article submitted",
                (author != null ? author.getFullName() + " (" + author.getEmail() + ")" : "A guest")
                        + " submitted \"" + saved.getTitle() + "\".");
        return saved;
    }

    /** Records one like on a published article and returns the updated post. */
    @Transactional
    public BlogPost likeOne(Integer id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article not found"));
        post.setLikeCount(post.getLikeCount() + 1);
        return blogPostRepository.save(post);
    }

    @Transactional
    public void approve(Integer id) {
        blogPostRepository.findById(id).ifPresent(post -> {
            post.setStatus(BlogStatus.APPROVED);
            blogPostRepository.save(post);
        });
    }

    @Transactional
    public void reject(Integer id) {
        blogPostRepository.findById(id).ifPresent(post -> {
            post.setStatus(BlogStatus.REJECTED);
            blogPostRepository.save(post);
        });
    }
}