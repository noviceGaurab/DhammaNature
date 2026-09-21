package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.VolunteerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_registration")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VolunteerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    @ToString.Exclude
    private VolunteerOpportunity opportunity;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VolunteerStatus status = VolunteerStatus.REGISTERED;

    @Column(length = 100)
    private String studentIdNumber;

    /** Student ID card photo stored as a BLOB (served from /volunteer-registrations/{id}/student-id/image). */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @ToString.Exclude
    private byte[] studentIdImageData;

    private String studentIdImageContentType;

    /** College approval letter stored as a BLOB (served from /volunteer-registrations/{id}/college-approval/image). */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @ToString.Exclude
    private byte[] collegeApprovalImageData;

    private String collegeApprovalImageContentType;

    @Column(length = 150)
    private String collegeName;

    @Builder.Default
    private boolean warningsAccepted = false;

    @Column(updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}
