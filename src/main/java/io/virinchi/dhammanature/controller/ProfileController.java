package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** A single "my account" screen tying together bookings, donations, orders, rewards and notifications. */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final SessionUserResolver sessionUserResolver;
    private final BookingService bookingService;
    private final DonationService donationService;
    private final MarketplaceService marketplaceService;
    private final RewardService rewardService;
    private final NotificationService notificationService;
    private final VolunteerService volunteerService;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        var userOpt = sessionUserResolver.resolve(session);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        var user = userOpt.get();
        model.addAttribute("bookings", bookingService.forUser(user.getId()));
        model.addAttribute("donations", donationService.forUser(user.getId()));
        model.addAttribute("orders", marketplaceService.ordersFor(user.getId()));
        model.addAttribute("rewardHistory", rewardService.historyFor(user.getId()));
        model.addAttribute("notifications", notificationService.forUser(user.getId()));
        model.addAttribute("volunteerRegistrations", volunteerService.forUser(user.getId()));
        return "profile";
    }
}
