package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.model.Product;
import io.virinchi.dhammanature.model.ProductReview;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import io.virinchi.dhammanature.service.MarketplaceService;
import io.virinchi.dhammanature.service.VendorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

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
    public String browse(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(required = false) ProductCategory cat,
                         Model model) {
        var allProducts = marketplaceService.browseAvailable();
        var products = cat != null
                ? allProducts.stream().filter(p -> p.getCategory() == cat).toList()
                : allProducts;
        int pageSize = 8;
        int totalPages = Math.max(1, (int) Math.ceil(products.size() / (double) pageSize));
        int current = Math.max(1, Math.min(page, totalPages));
        model.addAttribute("products", products.stream()
                .skip((current - 1) * (long) pageSize).limit(pageSize).toList());
        model.addAttribute("allProductCount", allProducts.size());
        model.addAttribute("productsByCategory", marketplaceService.browseGroupedByCategory());
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("featuredProducts", allProducts.stream().limit(4).toList());
        model.addAttribute("productCount", products.size());
        model.addAttribute("vendorCount", allProducts.stream()
                .map(p -> p.getVendor().getId())
                .distinct()
                .count());
        model.addAttribute("currentCategory", cat);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageBase", cat != null ? "/marketplace?cat=" + cat.name() : "/marketplace");
        return "marketplace";
    }

    @GetMapping("/marketplace/{id}")
    public String detail(@PathVariable Integer id, HttpSession session, Model model) {
        var product = marketplaceService.getProduct(id);
        var reviews = marketplaceService.reviewsFor(id);
        List<Product> related = marketplaceService.browseAvailable().stream()
                .filter(p -> !p.getId().equals(id))
                .filter(p -> p.getCategory() == product.getCategory())
                .limit(3)
                .toList();
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("relatedProducts", related);
        model.addAttribute("averageRating", reviews.stream()
                .mapToInt(ProductReview::getRating).average().orElse(0.0));
        model.addAttribute("inWishlist",
                sessionUserResolver.resolve(session).map(user -> user.getWishlist().stream()
                        .anyMatch(p -> p.getId().equals(id))).orElse(false));
        return "product-detail";
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleMissing(NoSuchElementException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage() + " - please choose a product from the marketplace.");
        return "redirect:/marketplace";
    }

    @PostMapping("/marketplace/{id}/buy")
    public String buy(@PathVariable Integer id, @RequestParam(defaultValue = "1") int quantity,
                       HttpSession session, RedirectAttributes redirectAttributes) {
        return sessionUserResolver.resolve(session)
                .map(user -> {
                    try {
                        var order = marketplaceService.purchase(user, id, quantity);
                        return "redirect:/marketplace/order-confirmed?orderId=" + order.getId();
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                        return "redirect:/marketplace/" + id;
                    }
                })
                .orElse("redirect:/login");
    }

    @GetMapping("/marketplace/order-confirmed")
    public String orderConfirmed(@RequestParam(required = false) Integer orderId,
                                 HttpSession session, Model model) {
        if (orderId == null) {
            return "redirect:/marketplace";
        }
        try {
            var order = marketplaceService.getOrder(orderId);
            boolean owner = sessionUserResolver.resolve(session)
                    .map(u -> u.getId().equals(order.getUser().getId()))
                    .orElse(false);
            if (!owner) {
                return "redirect:/marketplace";
            }
            model.addAttribute("order", order);
            model.addAttribute("pointsEarned", Math.max(1, order.getQuantity()));
        } catch (Exception e) {
            return "redirect:/marketplace";
        }
        return "order-confirm";
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
