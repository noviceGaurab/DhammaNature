package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.DonationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FR-05: Donation Management. Donation transactions cannot be modified once
 * completed (Business Rule 5), so this entity has no setter-driven "edit" flow
 * exposed anywhere in the controllers - only insert + read.
 */
@Entity
@Table(name = "donation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "campaign"})
@EqualsAndHashCode(of = "id")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nullable - guest donations are allowed, matching the original donation form. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Nullable - a general donation not tied to a specific charity campaign. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private CharityCampaign campaign;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    private String email;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DonationType donationType = DonationType.GENERAL;

    private String paymentMethod;

    @Column(unique = true)
    private String receiptNumber;

    @Column(updatable = false)
    private LocalDateTime donationDate;

    @PrePersist
    void onCreate() {
        this.donationDate = LocalDateTime.now();
        if (this.receiptNumber == null) {
            this.receiptNumber = "DN-" + System.currentTimeMillis();
        }
    }
}
