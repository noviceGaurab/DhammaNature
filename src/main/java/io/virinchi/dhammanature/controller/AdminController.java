package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.model.enums.Role;
import io.virinchi.dhammanature.repository.*;
import io.virinchi.dhammanature.service.AdminService;
import io.virinchi.dhammanature.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping
    public String users(Model model) {
        model.addAttribute("userData", userRepository.findAll());
        model.addAttribute("totalUsers", adminService.totalUsers());
        model.addAttribute("totalCenters", adminService.totalCenters());
        model.addAttribute("totalDonations", adminService.totalDonations());
        model.addAttribute("totalBookings", adminService.totalBookings());
        model.addAttribute("totalOrders", adminService.totalOrders());
        return "admin/users";
    }

    @GetMapping("/donations")
    public String donations(Model model) {
        model.addAttribute("donationData", donationRepository.findAllByOrderByDonationDateDesc());
        return "admin/donations";
    }

    @GetMapping("/comments")
    public String comments(Model model) {
        model.addAttribute("interactionData", commentRepository.findAllByOrderByCreatedAtDesc());
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/hide")
    public String hideComment(@PathVariable Integer id) {
        discussionService.hide(id);
        return "redirect:/admin/comments";
    }

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("galleryData", galleryRepository.findAll());
        return "admin/gallery";
    }

    @PostMapping("/gallery/add")
    public String addGalleryImage(@org.springframework.web.bind.annotation.RequestParam String title,
                                   @org.springframework.web.bind.annotation.RequestParam String imageUrl,
                                   @org.springframework.web.bind.annotation.RequestParam(required = false) String description) {
        galleryService.add(title, imageUrl, description);
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
        model.addAttribute("vendors", vendorRepository.findAll());
        return "admin/vendors";
    }

    @PostMapping("/vendors/{id}/verify")
    public String verifyVendor(@PathVariable Integer id) {
        vendorService.verify(id);
        return "redirect:/admin/vendors";
    }
}
