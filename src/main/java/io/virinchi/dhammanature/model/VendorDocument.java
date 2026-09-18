package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.VendorDocType;
import jakarta.persistence.*;
import lombok.*;

/** A piece of evidence a vendor uploads with their application (government ID, tax docs, shop proof, past work...). */
@Entity
@Table(name = "vendor_document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VendorDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorDocType docType;

    @Column(nullable = false)
    private String originalName;

    private String contentType;

    /** Optional short note explaining what the file proves. */
    @Column(length = 1000)
    private String note;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @ToString.Exclude
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    @ToString.Exclude
    private Vendor vendor;
}