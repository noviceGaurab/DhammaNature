package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.RewardTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** FR-09: Reward System - an audit trail of every point earned or spent. */
@Entity
@Table(name = "reward_transaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user", "redeemedItem"})
@EqualsAndHashCode(of = "id")
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardTransactionType type;

    /** Positive for EARNED, negative for REDEEMED. */
    @Column(nullable = false)
    private int points;

    private String reason;

    /** Set only when type = REDEEMED. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redeemed_item_id")
    private RewardCatalogItem redeemedItem;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
