package io.virinchi.dhammanature.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.virinchi.dhammanature.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Central account for every person on the platform: general users, volunteers,
 * vendors, meditation-center admins and the system administrator (FR-01, FR-02).
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private String passwordHash;

    private String phoneNumber;

    private String address;

    /** Kept as free text, matching the original Signup form's "clarify gender/pronoun" field. */
    private String genderIdentity;

    /** Raw bytes of the user's profile photo, stored in the database and served from /users/{id}/image. */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @ToString.Exclude
    private byte[] profileImageData;

    /** MIME type of {@link #profileImageData} (e.g. image/png). */
    private String profileImageContentType;

    /** Short personal description shown to other participants in the discuss section. */
    private String bio;

    /** Tracks the last moment the user was active; powers the "AFK / the community misses you" nudges. */
    private LocalDateTime lastSeenAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Builder.Default
    private int rewardPoints = 0;

    @Builder.Default
    private boolean readGuidelines = false;

    @Builder.Default
    private boolean understoodGuidelines = false;

    @Builder.Default
    private boolean acceptedTerms = false;

    @Builder.Default
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Relationships =====

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Booking> bookings = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Donation> donations = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<RewardTransaction> rewardTransactions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<VolunteerRegistration> volunteerRegistrations = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<ProductOrder> orders = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<QuizAttempt> quizAttempts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Notification> notifications = new HashSet<>();

    /** Field-validated "Save to Wishlist" extension (Section 4.3). */
    @ManyToMany
    @JoinTable(name = "user_wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    @Builder.Default
    @ToString.Exclude
    private Set<Product> wishlist = new HashSet<>();

    /** Lets a user follow/bookmark meditation centers they're interested in. */
    @ManyToMany
    @JoinTable(name = "user_followed_centers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "center_id"))
    @Builder.Default
    @ToString.Exclude
    private Set<MeditationCenter> followedCenters = new HashSet<>();
}
