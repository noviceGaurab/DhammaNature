package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * FR-03: Meditation Center Directory, and NFR-03: Organization Profile
 * (history, services, instructors, gallery, location, contact info).
 * Validated strongly by both Mokshya Yoga Retreat and the Transcendental
 * Meditation Center interviews.
 */
@Entity
@Table(name = "meditation_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"programs", "events", "galleryImages", "charityCampaigns", "volunteerOpportunities"})
@EqualsAndHashCode(of = "id")
public class MeditationCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String location;

    @Column(length = 4000)
    private String description;

    private String contactNumber;

    private String contactEmail;

    /** NFR-01: Social Media Integration - Facebook generated more engagement than the website for TM Center. */
    private String website;
    private String facebookUrl;
    private String instagramUrl;

    private String coverImageUrl;

    /** NFR-02: Hybrid Meditation Sessions - Mokshya offers both online guidance and physical sessions. */
    @Builder.Default
    private boolean supportsOnlineSessions = true;

    @Builder.Default
    private boolean supportsPhysicalSessions = true;

    /** Organizations must be approved before their info/events go live (Business Rule 2). */
    @Builder.Default
    private boolean verified = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Relationships =====

    @OneToMany(mappedBy = "meditationCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Program> programs = new HashSet<>();

    @OneToMany(mappedBy = "meditationCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "meditationCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Gallery> galleryImages = new HashSet<>();

    @OneToMany(mappedBy = "meditationCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CharityCampaign> charityCampaigns = new HashSet<>();

    @OneToMany(mappedBy = "meditationCenter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VolunteerOpportunity> volunteerOpportunities = new HashSet<>();
}
