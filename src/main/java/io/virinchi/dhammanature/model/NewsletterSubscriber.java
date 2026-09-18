package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** A newsletter subscription captured from the home / marketplace subscribe forms. */
@Entity
@Table(name = "newsletter_subscriber")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    private Boolean active = true;

    @Column(updatable = false)
    private LocalDateTime subscribedAt;

    @PrePersist
    void onCreate() {
        this.subscribedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }
}