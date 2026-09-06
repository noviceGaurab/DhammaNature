package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.*;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.OrderStatus;
import io.virinchi.dhammanature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

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

    public List<Product> browseAvailable() {
        return productRepository.findByVendor_VerifiedTrue();
    }

    public Product getProduct(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    @Transactional
    public Product addProduct(Integer vendorId, String name, String description, BigDecimal price,
                               int stock, String imageUrl) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found"));
        if (!vendor.isVerified()) {
            throw new IllegalStateException("Your vendor account must be verified by an administrator before you can list products.");
        }
        return productRepository.save(Product.builder()
                .vendor(vendor).productName(name).description(description)
                .price(price).stockQuantity(stock).imageUrl(imageUrl).build());
    }

    @Transactional
    public ProductOrder purchase(User user, Integer productId, int quantity) {
        Product product = getProduct(productId);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Not enough stock for \"" + product.getProductName() + "\".");
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        ProductOrder order = productOrderRepository.save(ProductOrder.builder()
                .user(user).product(product).quantity(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .status(OrderStatus.PLACED).build());

        rewardService.awardPoints(user, Math.max(1, quantity), "Purchased " + product.getProductName());
        notificationService.notifyUser(user, "Order placed: " + product.getProductName(),
                "Your order is being prepared. Track its status from your profile.", NotificationType.ORDER);
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
