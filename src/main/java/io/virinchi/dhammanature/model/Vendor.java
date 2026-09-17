package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** FR-07: Marketplace - verified vendors sell authentic Dhamma-related products. */
@Entity
@Table(name = "vendor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user", "products", "documents"})
@EqualsAndHashCode(of = "id")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String vendorName;

    private String contactDetails;

    private String address;

    /** Administrators verify vendors before products become publicly available (Section 4.1 / Business Rule 3). */
    @Builder.Default
    private boolean verified = false;

    /** The login account this vendor operates under. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    /** Evidence uploaded with the application, reviewed by admins before verification. */
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<VendorDocument> documents = new ArrayList<>();
}
