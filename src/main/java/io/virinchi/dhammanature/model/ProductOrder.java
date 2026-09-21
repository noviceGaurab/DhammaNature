package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.OrderStatus;
import io.virinchi.dhammanature.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Field-validated "Track Order" extension for the marketplace module. */
@Entity
@Table(name = "product_order")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user", "product"})
@EqualsAndHashCode(of = "id")
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PLACED;

    /** How the customer chose to pay when placing this order. */
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    /** Reward points spent on this order (0 = paid in money/eSewa/card/COD). */
    @Builder.Default
    private int pointsUsed = 0;

    /** Why the customer cancelled the order - kept for future review. */
    @Column(length = 500)
    private String cancelReason;

    private LocalDateTime cancelledAt;

    @Column(updatable = false)
    private LocalDateTime orderDate;

    @PrePersist
    void onCreate() {
        this.orderDate = LocalDateTime.now();
    }
}
