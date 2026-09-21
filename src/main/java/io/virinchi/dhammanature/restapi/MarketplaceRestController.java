package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.enums.PaymentMethod;
import io.virinchi.dhammanature.service.MarketplaceService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marketplace")
public class MarketplaceRestController {

    private final MarketplaceService marketplaceService;
    private final ApiAuth apiAuth;

    @GetMapping("/products")
    public ResponseEntity<?> products() {
        return ResponseEntity.ok(marketplaceService.browseAvailable().stream().map(ApiViews::product).toList());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> product(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiViews.product(marketplaceService.getProduct(id)));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> purchase(@Valid @RequestBody PurchaseRequest request, HttpSession session) {
        var user = apiAuth.requireUser(session);
        PaymentMethod method = request.paymentMethod() == null
                ? PaymentMethod.ESEWA
                : PaymentMethod.valueOf(request.paymentMethod());
        int quantity = request.quantity() == null || request.quantity() < 1 ? 1 : request.quantity();
        var order = marketplaceService.purchase(user, request.productId(), quantity, method);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.order(order));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> myOrders(HttpSession session) {
        var user = apiAuth.requireUser(session);
        return ResponseEntity.ok(marketplaceService.ordersFor(user.getId()).stream().map(ApiViews::order).toList());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> order(@PathVariable Integer id, HttpSession session) {
        var user = apiAuth.requireUser(session);
        var order = marketplaceService.getOrder(id);
        if (!order.getUser().getId().equals(user.getId())) {
            throw ApiException.forbidden("You do not have access to this order.");
        }
        return ResponseEntity.ok(ApiViews.order(order));
    }

    @PostMapping("/orders/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id, HttpSession session) {
        var user = apiAuth.requireUser(session);
        var order = marketplaceService.cancelOrder(user, id, "Cancelled via API");
        return ResponseEntity.ok(ApiViews.order(order));
    }

    @PostMapping("/products/{id}/reviews")
    public ResponseEntity<?> review(@PathVariable Integer id,
                                    @Valid @RequestBody ReviewRequest request,
                                    HttpSession session) {
        var user = apiAuth.requireUser(session);
        int rating = request.rating() == null ? 1 : request.rating();
        var review = marketplaceService.review(user, id, rating, request.reviewText());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiViews.review(review));
    }

    @PostMapping("/products/{id}/wishlist")
    public ResponseEntity<?> toggleWishlist(@PathVariable Integer id, HttpSession session) {
        var user = apiAuth.requireUser(session);
        marketplaceService.toggleWishlist(user, id);
        return ResponseEntity.ok(Map.of("message", "Wishlist updated.", "onWishlist",
                user.getWishlist().stream().anyMatch(p -> p.getId().equals(id))));
    }

    public record PurchaseRequest(
            @NotNull(message = "Please choose a product.")
            Integer productId,

            @Min(value = 1, message = "Quantity must be at least 1.")
            Integer quantity,

            @Size(max = 50, message = "Payment method is too long.")
            String paymentMethod) {
    }

    public record ReviewRequest(
            @NotNull(message = "A rating is required.")
            @Min(value = 1, message = "Rating must be between 1 and 5.")
            @Max(value = 5, message = "Rating must be between 1 and 5.")
            Integer rating,

            @Size(max = 2000, message = "Review text is too long.")
            String reviewText) {
    }
}
