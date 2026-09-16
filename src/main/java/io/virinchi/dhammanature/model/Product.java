package io.virinchi.dhammanature.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/** Prayer beads, incense, books, Buddha statues, meditation accessories, handicrafts. */
@Entity
@Table(name = "product")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"vendor", "reviews", "imageData"})
@EqualsAndHashCode(of = "id")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String productName;

    @Column(length = 2000)
    private String description;

    /** Optional genre/topic (e.g. "Guided Meditation", "Sutta Commentary") for books & audio. */
    private String genre;

    @Column(nullable = false)
    private BigDecimal price;

    @Builder.Default
    private int stockQuantity = 0;

    private String imageUrl;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] imageData;

    @JsonIgnore
    private String imageContentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductCategory category = ProductCategory.HANDICRAFTS;

    /** Marketplace products must belong to verified vendors (Business Rule 6). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    /** Field-validated "View/Write Reviews" extension. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductReview> reviews = new HashSet<>();
}
