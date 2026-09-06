package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.CampaignStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * NFR-05: Charity Campaign Module - organizations publish charitable campaigns
 * and allow online participation and donations. Directly requested by the
 * visitors interviewed at Ashok Stupa.
 */
@Entity
@Table(name = "charity_campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"meditationCenter", "donations"})
@EqualsAndHashCode(of = "id")
public class CharityCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private BigDecimal goalAmount;

    @Builder.Default
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.ACTIVE;

    /** Only verified organizations may create charity campaigns (Business Rule 8). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private MeditationCenter meditationCenter;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Donation> donations = new HashSet<>();

    public void addRaisedAmount(BigDecimal amount) {
        this.raisedAmount = this.raisedAmount.add(amount);
    }
}
