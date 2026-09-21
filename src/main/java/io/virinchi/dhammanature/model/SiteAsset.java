package io.virinchi.dhammanature.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * One platform-wide static asset (site logo, hero banners, decorative photos)
 * stored as a BLOB in the database instead of a physical file under
 * static/assets/images. The name mirrors the old filename without its extension,
 * e.g. "logo.png" becomes name = "logo".
 */
@Entity
@Table(name = "site_asset")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "imageData")
@EqualsAndHashCode(of = "id")
public class SiteAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** e.g. "logo", "home-hero" - matches the old filename without extension. */
    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] imageData;

    @JsonIgnore
    private String contentType;
}