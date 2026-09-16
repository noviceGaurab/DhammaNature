package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.config.StorageConfig;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

/** A single "my account" screen tying together bookings, donations, orders, rewards and notifications. */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final SessionUserResolver sessionUserResolver;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final DonationService donationService;
    private final MarketplaceService marketplaceService;
    private final RewardService rewardService;
    private final NotificationService notificationService;
    private final VolunteerService volunteerService;
    private final QuizService quizService;
    private final BlogService blogService;
    private final StorageConfig storageConfig;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        var bookings = bookingService.forUser(user.getId());
        var donations = donationService.forUser(user.getId());
        var orders = marketplaceService.ordersFor(user.getId());
        var rewardHistory = rewardService.historyFor(user.getId());
        var notifications = notificationService.forUser(user.getId());
        var volunteerRegistrations = volunteerService.forUser(user.getId());
        var quizHistory = quizService.historyFor(user.getId());
        var blogPosts = blogService.forUser(user.getId());
        var wishlist = userRepository.findByIdWithWishlist(user.getId())
                .map(User::getWishlist)
                .orElse(Collections.emptySet());
        var followedCenters = userRepository.findByIdWithFollowedCenters(user.getId())
                .map(User::getFollowedCenters)
                .orElse(Collections.emptySet());

        model.addAttribute("bookings", bookings);
        model.addAttribute("donations", donations);
        model.addAttribute("orders", orders);
        model.addAttribute("rewardHistory", rewardHistory);
        model.addAttribute("notifications", notifications);
        model.addAttribute("volunteerRegistrations", volunteerRegistrations);
        model.addAttribute("quizHistory", quizHistory);
        model.addAttribute("blogPosts", blogPosts);
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("followedCenters", followedCenters);
        model.addAttribute("bookingCount", bookings.size());
        model.addAttribute("donationCount", donations.size());
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("wishlistCount", wishlist.size());
        return "profile";
    }

    /** Uploads a new profile photo (served from /uploads/**). */
    @PostMapping("/profile/image")
    public String uploadImage(@RequestParam("image") MultipartFile image,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        if (image == null || image.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Choose an image file first.");
            return "redirect:/profile";
        }
        try {
            String filename = storageConfig.saveImage(image);
            User user = userOpt.get();
            user.setProfileImage(filename);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile photo updated. It will appear across the site.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Could not save the image. Please try another file.");
        }
        return "redirect:/profile";
    }

    /** Updates the short "about me" text shown to other discuss participants. */
    @PostMapping("/profile/bio")
    public String updateBio(@RequestParam(defaultValue = "") String bio,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        user.setBio(bio.isBlank() ? null : bio.trim());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Your profile description was saved.");
        return "redirect:/profile";
    }
}
