package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.service.BlogService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/blog")
public class BlogRestController {

    private final BlogService blogService;
    private final ApiAuth apiAuth;

    @GetMapping("/posts")
    public ResponseEntity<?> posts() {
        return ResponseEntity.ok(blogService.published().stream().map(ApiViews::blogPost).toList());
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<?> post(@PathVariable Integer id) {
        var blogPost = blogService.publishedById(id).orElseThrow(
                () -> ApiException.notFound("Article not found"));
        return ResponseEntity.ok(ApiViews.blogPost(blogPost));
    }

    /** Any logged-in member may submit an article for review. */
    @PostMapping("/posts")
    public ResponseEntity<?> submit(@Valid @RequestBody BlogPostRequest request, HttpSession session) {
        var user = apiAuth.requireUser(session);
        var saved = blogService.submit(request.title(), request.category(),
                request.imageUrl(), request.content(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.blogPost(saved));
    }

    public record BlogPostRequest(
            @NotBlank(message = "Please provide an article title.")
            @Size(max = 200, message = "Title must be 200 characters or fewer.")
            String title,

            @Size(max = 100, message = "Category is too long.")
            String category,

            @Size(max = 500, message = "Image URL is too long.")
            String imageUrl,

            @NotBlank(message = "Please provide the article content.")
            @Size(max = 20000, message = "Article content is too long.")
            String content) {
    }
}
