package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileRestController {

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final DonationService donationService;
    private final MarketplaceService marketplaceService;
    private final RewardService rewardService;
    private final NotificationService notificationService;
    private final VolunteerService volunteerService;
    private final QuizService quizService;
    private final BlogService blogService;
    private final ApiAuth apiAuth;

    @GetMapping
    public ResponseEntity<?> profile(HttpSession session) {
        User user = apiAuth.requireUser(session);
        Map<String, Object> body = ApiViews.user(user);
        body.put("bookingCount", bookingService.forUser(user.getId()).size());
        body.put("donationCount", donationService.forUser(user.getId()).size());
        body.put("orderCount", marketplaceService.ordersFor(user.getId()).size());
        body.put("rewardTransactionCount", rewardService.historyFor(user.getId()).size());
        body.put("notificationCount", notificationService.forUser(user.getId()).size());
        body.put("volunteerRegistrationCount", volunteerService.forUser(user.getId()).size());
        body.put("quizAttemptCount", quizService.historyFor(user.getId()).size());
        body.put("blogPostCount", blogService.forUser(user.getId()).size());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> bookings(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(bookingService.forUser(user.getId()).stream().map(ApiViews::booking).toList());
    }

    @GetMapping("/donations")
    public ResponseEntity<?> donations(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(donationService.forUser(user.getId()).stream().map(ApiViews::donation).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(marketplaceService.ordersFor(user.getId()).stream().map(ApiViews::order).toList());
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> rewardTransactions(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(rewardService.historyFor(user.getId()).stream().map(ApiViews::rewardTransaction).toList());
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> notifications(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(notificationService.forUser(user.getId()).stream().map(ApiViews::notification).toList());
    }

    @GetMapping("/volunteer-registrations")
    public ResponseEntity<?> volunteerRegistrations(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(volunteerService.forUser(user.getId()).stream()
                .map(ApiViews::volunteerRegistration)
                .toList());
    }

    @GetMapping("/quiz-attempts")
    public ResponseEntity<?> quizAttempts(HttpSession session) {
        User user = apiAuth.requireUser(session);
        return ResponseEntity.ok(quizService.historyFor(user.getId()).stream().map(ApiViews::quizAttempt).toList());
    }

    @PostMapping("/bio")
    public ResponseEntity<?> updateBio(@Valid @RequestBody BioRequest request, HttpSession session) {
        User user = apiAuth.requireUser(session);
        user.setBio(request.bio() == null || request.bio().isBlank() ? null : request.bio().trim());
        userRepository.save(user);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Your profile description was saved.");
        body.put("bio", user.getBio());
        return ResponseEntity.ok(body);
    }

    public record BioRequest(
            @Size(max = 1000, message = "Bio must be 1000 characters or fewer.")
            String bio) {
    }
}
