package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.Comment;
import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.MeditationCenter;
import io.virinchi.dhammanature.model.QuizQuestion;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.Vendor;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.repository.*;
import io.virinchi.dhammanature.service.AdminService;
import io.virinchi.dhammanature.service.BookingService;
import io.virinchi.dhammanature.service.QuizService;
import io.virinchi.dhammanature.service.VendorService;
import io.virinchi.dhammanature.service.DiscussionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Admin dashboard - carries over admin.jsp / admin_donations.jsp / admin_comments.jsp /
 * admin_gallery.jsp / admin_page_interactions.jsp, now rendered with Thymeleaf th:each
 * instead of JSP scriptlets, plus new center/vendor verification screens.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final CommentRepository commentRepository;
    private final GalleryRepository galleryRepository;
    private final PageInteractionRepository pageInteractionRepository;
    private final MeditationCenterRepository meditationCenterRepository;
    private final VendorRepository vendorRepository;
    private final AdminService adminService;
    private final VendorService vendorService;
    private final io.virinchi.dhammanature.service.DiscussionService discussionService;
    private final io.virinchi.dhammanature.service.GalleryService galleryService;
    private final io.virinchi.dhammanature.service.BlogService blogService;
    private final io.virinchi.dhammanature.service.EventService eventService;
    private final BookingService bookingService;
    private final QuizService quizService;
    private final QuizAttemptRepository quizAttemptRepository;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping
    public String users(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page, Model model) {
        var all = userRepository.findAll();
        int pageSize = 10;
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) pageSize));
        int current = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("userData", all.stream()
                .skip((current - 1) * (long) pageSize).limit(pageSize).toList());
        model.addAttribute("totalUsers", adminService.totalUsers());
        model.addAttribute("totalCenters", adminService.totalCenters());
        model.addAttribute("totalDonations", adminService.totalDonations());
        model.addAttribute("totalBookings", adminService.totalBookings());
        model.addAttribute("totalOrders", adminService.totalOrders());
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageBase", "/admin");
        return "admin/users";
    }

    /** Summary report with imagery charts - the admin "conclusion" view of how the platform is doing. */
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalUsers", adminService.totalUsers());
        model.addAttribute("totalCenters", adminService.totalCenters());
        model.addAttribute("totalDonations", adminService.totalDonations());
        model.addAttribute("totalBookings", adminService.totalBookings());
        model.addAttribute("totalOrders", adminService.totalOrders());

        // Users registered per month (last 6 months, ascending)
        java.util.LinkedHashMap<String, Long> usersByMonth = new java.util.LinkedHashMap<>();
        java.util.LinkedHashMap<String, Long> donationsByMonth = new java.util.LinkedHashMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = java.time.YearMonth.from(today.minusMonths(i));
            usersByMonth.put(ym.toString(), 0L);
            donationsByMonth.put(ym.toString(), 0L);
        }
        userRepository.findAll().forEach(u -> {
            if (u.getCreatedAt() != null) {
                String key = java.time.YearMonth.from(u.getCreatedAt().toLocalDate()).toString();
                if (usersByMonth.containsKey(key)) usersByMonth.replace(key, usersByMonth.get(key) + 1);
            }
        });
        donationRepository.findAll().forEach(d -> {
            if (d.getDonationDate() != null) {
                String key = java.time.YearMonth.from(d.getDonationDate().toLocalDate()).toString();
                if (donationsByMonth.containsKey(key)) donationsByMonth.replace(key, donationsByMonth.get(key) + 1);
            }
        });
        model.addAttribute("userMonthLabels", new ArrayList<>(usersByMonth.keySet()));
        model.addAttribute("userMonthValues", new ArrayList<>(usersByMonth.values()));
        model.addAttribute("donationMonthLabels", new ArrayList<>(donationsByMonth.keySet()));
        model.addAttribute("donationMonthValues", new ArrayList<>(donationsByMonth.values()));

        // Role distribution (doughnut)
        java.util.LinkedHashMap<String, Long> roles = new java.util.LinkedHashMap<>();
        userRepository.findAll().forEach(u -> roles.merge(u.getRole() != null ? u.getRole().name() : "USER", 1L, Long::sum));
        model.addAttribute("roleLabels", new ArrayList<>(roles.keySet()));
        model.addAttribute("roleValues", new ArrayList<>(roles.values()));

        // Interactions by page (bar)
        java.util.LinkedHashMap<String, Long> pages = new java.util.LinkedHashMap<>();
        pageInteractionRepository.findAll().forEach(pi -> {
            String p = pi.getPageName() == null || pi.getPageName().isBlank() ? "Unknown" : pi.getPageName();
            pages.merge(p, 1L, Long::sum);
        });
        java.util.LinkedHashMap<String, Long> topPages = pages.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, java.util.LinkedHashMap::new));
        List<String> topPageLabels = new ArrayList<>();
        List<Long> topPageValues = new ArrayList<>();
        topPages.forEach((k, v) -> { topPageLabels.add(k); topPageValues.add(v); });
        model.addAttribute("pageLabels", topPageLabels);
        model.addAttribute("pageValues", topPageValues);

        // Comment activity by topic (horizontal bar) - top 8 topics
        java.util.LinkedHashMap<String, Long> topics = new java.util.LinkedHashMap<>();
        commentRepository.findAllByOrderByCreatedAtDesc().forEach(c -> {
            String t = c.getTopic() != null ? c.getTopic().getTitle() : "General";
            topics.merge(t, 1L, Long::sum);
        });
        List<String> topicLabels = new ArrayList<>();
        List<Long> topicValues = new ArrayList<>();
        topics.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .forEach(e -> { topicLabels.add(e.getKey()); topicValues.add(e.getValue()); });
        model.addAttribute("topicLabels", topicLabels);
        model.addAttribute("topicValues", topicValues);

        model.addAttribute("donationTotal",
                donationRepository.findAll().stream()
                        .map(io.virinchi.dhammanature.model.Donation::getAmount)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return "admin/reports";
    }

    @GetMapping("/donations")
    public String donations(Model model) {
        model.addAttribute("donationData", donationRepository.findAllByOrderByDonationDateDesc());
        return "admin/donations";
    }

    @GetMapping("/comments")
    public String comments(Model model) {
        var all = commentRepository.findAllByOrderByCreatedAtDesc();
        long hidden = all.stream().filter(Comment::isHidden).count();
        model.addAttribute("interactionData", all);
        model.addAttribute("totalComments", all.size());
        model.addAttribute("visibleComments", all.size() - hidden);
        model.addAttribute("hiddenComments", hidden);
        model.addAttribute("topicCount", all.stream().map(c -> c.getTopic().getId()).distinct().count());
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Integer id) {
        discussionService.delete(id);
        return "redirect:/admin/comments";
    }

    @PostMapping("/comments/{id}/unhide")
    public String unhideComment(@PathVariable Integer id) {
        discussionService.unhide(id);
        return "redirect:/admin/comments";
    }

    @PostMapping("/comments/{id}/reply")
    public String replyComment(@PathVariable Integer id,
                               @RequestParam String content,
                               HttpSession session) {
        User admin = sessionUserResolver.require(session);
        discussionService.reply(id, admin, content);
        return "redirect:/admin/comments";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("galleryData", galleryRepository.findAll());
        return "admin/gallery";
    }

    @PostMapping("/gallery/add")
    public String addGalleryImage(@org.springframework.web.bind.annotation.RequestParam String title,
                                   @org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file,
                                   @org.springframework.web.bind.annotation.RequestParam(required = false) String description) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return "redirect:/admin/gallery?error=invalidimage";
        }
        galleryService.add(title, file.getBytes(), file.getContentType(), description);
        return "redirect:/admin/gallery";
    }

    @PostMapping("/gallery/{id}/delete")
    public String deleteGalleryImage(@PathVariable Integer id) {
        galleryService.delete(id);
        return "redirect:/admin/gallery";
    }

    @GetMapping("/interactions")
    public String interactions(Model model) {
        model.addAttribute("interactionData", pageInteractionRepository.findAllByOrderByInteractionTimeDesc());
        return "admin/interactions";
    }

    @GetMapping("/centers")
    public String centers(Model model) {
        model.addAttribute("centers", meditationCenterRepository.findAll());
        return "admin/centers";
    }

    @PostMapping("/centers/{id}/verify")
    public String verifyCenter(@PathVariable Integer id) {
        adminService.verifyCenter(id);
        return "redirect:/admin/centers";
    }

    @GetMapping("/vendors")
    public String vendors(Model model) {
        var all = vendorRepository.findAll();
        model.addAttribute("vendors", all);
        model.addAttribute("pendingCount", all.stream().filter(v -> !v.isVerified()).count());
        model.addAttribute("verifiedCount", all.stream().filter(Vendor::isVerified).count());
        return "admin/vendors";
    }

    @PostMapping("/vendors/{id}/verify")
    public String verifyVendor(@PathVariable Integer id) {
        vendorService.verify(id);
        return "redirect:/admin/vendors";
    }

    @PostMapping("/vendors/{id}/reject")
    public String rejectVendor(@PathVariable Integer id) {
        vendorService.reject(id);
        return "redirect:/admin/vendors";
    }

    @GetMapping("/blog")
    public String blogPosts(Model model) {
        model.addAttribute("blogPosts", blogService.forAdmin());
        model.addAttribute("pendingCount", blogService.pendingCount());
        return "admin/blog-posts";
    }

    @PostMapping("/blog/{id}/approve")
    public String approveBlogPost(@PathVariable Integer id) {
        blogService.approve(id);
        return "redirect:/admin/blog";
    }

    @PostMapping("/blog/{id}/reject")
    public String rejectBlogPost(@PathVariable Integer id) {
        blogService.reject(id);
        return "redirect:/admin/blog";
    }

    @GetMapping("/events")
    public String events(Model model) {
        var events = eventService.all();
        java.util.Map<Integer, Long> bookingCounts = new java.util.HashMap<>();
        events.forEach(e -> bookingCounts.put(e.getId(), (long) bookingService.forEvent(e.getId()).size()));
        model.addAttribute("events", events);
        model.addAttribute("bookingCounts", bookingCounts);
        return "admin/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Integer id) {
        eventService.delete(id);
        return "redirect:/admin/events";
    }

    @GetMapping("/events/new")
    public String newEvent(Model model) {
        model.addAttribute("centers", meditationCenterRepository.findAll());
        model.addAttribute("modes", SessionMode.values());
        return "admin/event-add";
    }

    @PostMapping("/events/create")
    public String createEvent(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam("eventDate") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime eventDate,
                              @RequestParam(required = false) String venue,
                              @RequestParam SessionMode mode,
                              @RequestParam(required = false) String capacity,
                              @RequestParam Integer centerId) {
        MeditationCenter center = meditationCenterRepository.findById(centerId)
                .orElseThrow(() -> new NoSuchElementException("Meditation center not found"));
        Integer cap = (capacity == null || capacity.isBlank()) ? null : Integer.valueOf(capacity);
        Event event = Event.builder()
                .title(title)
                .description(description)
                .eventDate(eventDate)
                .venue(venue)
                .mode(mode)
                .capacity(cap)
                .build();
        eventService.publish(center, event);
        return "redirect:/admin/events";
    }

    @GetMapping("/quizzes")
    public String quizzes(Model model) {
        var quizzes = quizService.all();
        java.util.Map<Integer, Long> attemptCounts = new java.util.HashMap<>();
        quizzes.forEach(q -> attemptCounts.put(q.getId(), quizAttemptRepository.countByQuiz_Id(q.getId())));
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("attemptCounts", attemptCounts);
        return "admin/quizzes";
    }

    @GetMapping("/quizzes/new")
    public String newQuiz() {
        return "admin/quiz-add";
    }

    @PostMapping("/quizzes/create")
    public String createQuiz(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "10") int rewardPoints,
                             @RequestParam Map<String, String> params) {
        List<QuizQuestion> questions = new ArrayList<>();
        List<String> options = List.of("A", "B", "C", "D");
        for (int i = 1; i <= 5; i++) {
            String text = params.get("q" + i + "text");
            if (text != null && !text.isBlank()) {
                String correct = params.get("q" + i + "correct");
                if (correct == null || !options.contains(correct)) {
                    correct = "A";
                }
                questions.add(QuizQuestion.builder()
                        .questionText(text.trim())
                        .optionA(params.getOrDefault("q" + i + "a", ""))
                        .optionB(params.getOrDefault("q" + i + "b", ""))
                        .optionC(params.getOrDefault("q" + i + "c", ""))
                        .optionD(params.getOrDefault("q" + i + "d", ""))
                        .correctOption(correct)
                        .build());
            }
        }
        if (questions.isEmpty()) {
            return "redirect:/admin/quizzes/new?error=1";
        }
        quizService.createQuiz(title, description, rewardPoints, questions);
        return "redirect:/admin/quizzes";
    }

    @PostMapping("/quizzes/{id}/delete")
    public String deleteQuiz(@PathVariable Integer id) {
        quizService.delete(id);
        return "redirect:/admin/quizzes";
    }
}
