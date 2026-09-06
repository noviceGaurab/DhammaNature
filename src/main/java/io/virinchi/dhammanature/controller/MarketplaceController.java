package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.service.MarketplaceService;
import io.virinchi.dhammanature.service.VendorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * FR-07: Marketplace, plus the field-validated "Save to Wishlist", "Track
 * Order" and "View/Write Reviews" extensions (Section 4.3 use-case diagram).
 */
@Controller
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final VendorService vendorService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/marketplace")
    public String browse(Model model) {
        model.addAttribute("products", marketplaceService.browseAvailable());
        return "marketplace";
    }

    @GetMapping("/marketplace/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("product", marketplaceService.getProduct(id));
        model.addAttribute("reviews", marketplaceService.reviewsFor(id));
        return "product-detail";
    }

    @PostMapping("/marketplace/{id}/buy")
    public String buy(@PathVariable Integer id, @RequestParam(defaultValue = "1") int quantity,
                       HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        marketplaceService.purchase(user, id, quantity);
                        redirectAttributes.addFlashAttribute("success", "Order placed! Track it from your profile.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/marketplace/" + id;
                })
                .orElse("redirect:/login");
    }

    @PostMapping("/marketplace/{id}/wishlist")
    public String wishlist(@PathVariable Integer id, HttpSession session) {
        sessionUserResolver.resolve(session).ifPresent(user -> marketplaceService.toggleWishlist(user, id));
        return "redirect:/marketplace/" + id;
    }

    @PostMapping("/marketplace/{id}/review")
    public String review(@PathVariable Integer id, @RequestParam int rating, @RequestParam String reviewText,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    marketplaceService.review(user, id, rating, reviewText);
                    return "redirect:/marketplace/" + id;
                })
                .orElse("redirect:/login");
    }

    @PostMapping("/vendor/apply")
    public String applyVendor(@RequestParam String vendorName, @RequestParam(required = false) String contactDetails,
                               @RequestParam(required = false) String address,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        vendorService.applyAsVendor(user, vendorName, contactDetails, address);
                        redirectAttributes.addFlashAttribute("success", "Vendor application submitted - pending admin verification.");
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                    return "redirect:/profile";
                })
                .orElse("redirect:/login");
    }
}
