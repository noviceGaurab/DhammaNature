package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.RewardCategory;
import jakarta.persistence.*;
import lombok.*;

/**
 * NFR-04: Reward Redemption catalog. Incense coupons, meditation discounts,
 * charity sponsorship and certificates - suggested directly by the visitors
 * interviewed at Ashok Stupa.
 */
@Entity
@Table(name = "reward_catalog_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "meditationCenter")
@EqualsAndHashCode(of = "id")
public class RewardCatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private int pointsCost;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RewardCategory category = RewardCategory.INCENSE_COUPON;

    /** Null = platform-wide reward; set = redeemable only through that meditation center. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id")
    private MeditationCenter meditationCenter;

    @Builder.Default
    private boolean active = true;
}
