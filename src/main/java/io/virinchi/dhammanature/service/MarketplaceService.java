package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.*;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.OrderStatus;
import io.virinchi.dhammanature.model.enums.PaymentMethod;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import io.virinchi.dhammanature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * FR-07: Marketplace, plus the field-validated extensions from the use-case
 * diagram (Section 4.3): Save to Wishlist, Track Order, View/Write Reviews.
 * Business Rule 6: products must belong to a verified vendor.
 */
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductReviewRepository productReviewRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public List<Product> browseAvailable() {
        return productRepository.findByVendor_VerifiedTrue();
    }

    /** Products grouped by category in enum display order; empty categories are omitted. */
    public Map<ProductCategory, List<Product>> browseGroupedByCategory() {
        Map<ProductCategory, List<Product>> grouped = browseAvailable().stream()
                .collect(Collectors.groupingBy(Product::getCategory));
        Map<ProductCategory, List<Product>> ordered = new LinkedHashMap<>();
        for (ProductCategory category : ProductCategory.values()) {
            List<Product> items = grouped.get(category);
            if (items != null && !items.isEmpty()) {
                ordered.put(category, items);
            }
        }
        return ordered;
    }

    public Product getProduct(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    @Transactional
    public Product addProduct(Integer vendorId, String name, String description, String genre, BigDecimal price,
                               int stock, byte[] imageData, String imageContentType, ProductCategory category) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found"));
        ProductCategory resolved = category != null ? category : ProductCategory.HANDICRAFTS;
        return productRepository.save(Product.builder()
                .vendor(vendor).productName(name).description(description).genre(genre)
                .price(price).stockQuantity(stock).imageData(imageData).imageContentType(imageContentType).category(resolved).build());
    }

    /** A vendor's own catalogue - includes drafts that are not yet publicly listed. */
    public List<Product> productsForVendor(Integer vendorId) {
        return productRepository.findByVendor_IdOrderByProductName(vendorId);
    }

    /** A product that belongs to the given vendor, so vendors can only manage their own catalogue. */
    public Product findVendorProduct(Integer vendorId, Integer productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getVendor().getId().equals(vendorId))
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    @Transactional
    public Product updateProduct(Integer vendorId, Integer productId, String name, String description,
                                 String genre, BigDecimal price, int stock,
                                 String imageUrl, ProductCategory category) {
        Product product = findVendorProduct(vendorId, productId);
        product.setProductName(name);
        product.setDescription(description);
        product.setGenre(genre);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setImageUrl(imageUrl);
        if (category != null) {
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Integer vendorId, Integer productId) {
        Product product = findVendorProduct(vendorId, productId);
        long orders = productOrderRepository.countByProduct_Id(productId);
        if (orders > 0) {
            throw new IllegalStateException(
                    "\"" + product.getProductName() + "\" has placed orders and cannot be removed - edit its details instead.");
        }
        productRepository.delete(product);
    }

    @Transactional
    public ProductOrder purchase(User user, Integer productId, int quantity, PaymentMethod paymentMethod) {
        Product product = getProduct(productId);
        if (isPurchased(productId)) {
            throw new IllegalStateException("\"" + product.getProductName()
                    + "\" has already been purchased and is no longer available.");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalStateException("No more in stock.");
        }
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        int pointsUsed = 0;
        if (paymentMethod == PaymentMethod.REDEEMED_POINTS) {
            pointsUsed = PointValue.pointsFor(totalPrice);
            rewardService.spendPoints(user, pointsUsed,
                    "Paid for \"" + product.getProductName() + "\" x " + quantity + " with reward points");
        }

        ProductOrder order = productOrderRepository.save(ProductOrder.builder()
                .user(user).product(product).quantity(quantity)
                .totalPrice(totalPrice)
                .paymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.ESEWA)
                .pointsUsed(pointsUsed)
                .status(OrderStatus.PLACED).build());

        rewardService.awardPoints(user, Math.max(1, quantity), "Purchased " + product.getProductName());
        notificationService.notifyUser(user, "Order placed: " + product.getProductName(),
                "Your order is being prepared. Track its status from your profile.", NotificationType.ORDER);
        emailService.sendToUser(user, "Order confirmed: " + product.getProductName(),
                "Hi " + user.getFullName() + ",\n\n"
                        + "Your order has been placed:\n"
                        + " - " + product.getProductName() + " x " + quantity
                        + "\nTotal: $" + order.getTotalPrice() + "\n"
                        + "Payment: " + (pointsUsed > 0 ? pointsUsed + " reward points" : paymentMethod.getDisplayName())
                        + "\nStatus: " + order.getStatus() + "\n\n"
                        + "Track its status from your profile.\n\n"
                        + "With metta,\nThe Dhamma Nature team");
        emailService.sendSiteAlert("New order placed",
                user.getFullName() + " (" + user.getEmail() + ") purchased "
                        + quantity + "x " + product.getProductName() + " (order #" + order.getId() + ").");
        return order;
    }

    /** Ids of every product the user has bought (and not cancelled) - used to tag "already purchased" in the catalog. */
    public List<Integer> purchasedProductIds(Integer userId) {
        return productOrderRepository.findPurchasedProductIdsByUser(userId);
    }

    /** Ids of every product that has been sold (any user, non-cancelled) - these are "No more in stock". */
    public List<Integer> purchasedProductIds() {
        return productOrderRepository.findAllPurchasedProductIds();
    }

    /** True once any (non-cancelled) purchase exists for this product - it is then no longer offered for sale. */
    public boolean isPurchased(Integer productId) {
        return productOrderRepository.existsByProduct_IdAndStatusNot(productId, OrderStatus.CANCELLED);
    }

    /**
     * Reserves one unit the moment "Buy now" is clicked by lowering the stock
     * count immediately, so the item reads "No more in stock" for everyone else.
     */
    @Transactional
    public void reserveStock(Integer productId) {
        Product product = getProduct(productId);
        if (product.getStockQuantity() <= 0) {
            throw new IllegalStateException("No more in stock.");
        }
        product.setStockQuantity(product.getStockQuantity() - 1);
        productRepository.save(product);
    }

    public ProductOrder getOrder(Integer id) {
        return productOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    /**
     * Cancels an order that has not been delivered yet: restores stock,
     * refunds any reward points used, and claws back the purchase points when
     * the user still has enough. The customer-selected reason is stored for
     * future review.
     */
    @Transactional
    public ProductOrder cancelOrder(User user, Integer orderId, String cancelReason) {
        ProductOrder order = getOrder(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not have permission to cancel this order.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("This order has already been cancelled.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("This order has already been delivered and cannot be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(cancelReason != null ? cancelReason.trim() : null);

        Product product = order.getProduct();
        product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
        productRepository.save(product);

        int refundPoints = order.getPointsUsed();
        if (refundPoints > 0) {
            rewardService.awardPoints(user, refundPoints,
                    "Refunded points for cancelled order: " + product.getProductName());
        }
        int clawbackPoints = Math.max(1, order.getQuantity());
        try {
            rewardService.spendPoints(user, clawbackPoints,
                    "Points removed for cancelled order: " + product.getProductName());
        } catch (IllegalStateException e) {
            // User has already spent the purchase points - nothing left to claw back.
        }

        productOrderRepository.save(order);
        notificationService.notifyUser(user, "Order cancelled: " + product.getProductName(),
                "Your order has been cancelled"
                        + (order.getCancelReason() != null ? " - " + order.getCancelReason() : "") + "."
                        + (refundPoints > 0 ? " " + refundPoints + " reward points were refunded to your account." : ""),
                NotificationType.ORDER);
        emailService.sendToUser(user, "Order cancelled: " + product.getProductName(),
                "Hi " + user.getFullName() + ",\n\n"
                        + "Your order has been cancelled:\n"
                        + " - " + product.getProductName() + " x " + order.getQuantity()
                        + "\nTotal: $" + order.getTotalPrice()
                        + (refundPoints > 0 ? "\nReward points refunded: " + refundPoints : "")
                        + (order.getCancelReason() != null && !order.getCancelReason().isBlank()
                        ? "\nReason: " + order.getCancelReason() : "")
                        + "\n\nWith metta,\nThe Dhamma Nature team");
        emailService.sendSiteAlert("Order cancelled",
                user.getFullName() + " (" + user.getEmail() + ") cancelled " + order.getQuantity()
                        + "x " + product.getProductName() + " (order #" + order.getId()
                        + "). Reason: " + (order.getCancelReason() != null ? order.getCancelReason() : "not given"));
        return order;
    }

    public List<ProductOrder> ordersFor(Integer userId) {
        return productOrderRepository.findByUser_IdOrderByOrderDateDesc(userId);
    }

    @Transactional
    public ProductReview review(User user, Integer productId, int rating, String text) {
        Product product = getProduct(productId);
        int clamped = Math.max(1, Math.min(5, rating));
        return productReviewRepository.save(ProductReview.builder()
                .product(product).user(user).rating(clamped).reviewText(text).build());
    }

    public List<ProductReview> reviewsFor(Integer productId) {
        return productReviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId);
    }

    @Transactional
    public void toggleWishlist(User user, Integer productId) {
        Product product = getProduct(productId);
        if (user.getWishlist().contains(product)) {
            user.getWishlist().remove(product);
        } else {
            user.getWishlist().add(product);
        }
        userRepository.save(user);
    }
}
