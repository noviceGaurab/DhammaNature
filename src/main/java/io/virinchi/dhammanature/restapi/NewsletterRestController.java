package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.service.NewsletterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsletter")
public class NewsletterRestController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscribeRequest request) {
        NewsletterService.SubscriptionResult result = newsletterService.subscribe(request.email());
        return ResponseEntity.status(result.success() ? 201 : 200)
                .body(Map.of("success", result.success(),
                        "alreadySubscribed", result.alreadySubscribed(),
                        "message", result.message()));
    }

    public record SubscribeRequest(
            @NotBlank(message = "Email is required.")
            @Email(message = "Please provide a valid email address.")
            String email) {
    }
}
