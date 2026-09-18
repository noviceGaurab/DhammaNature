package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.VendorDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorDocumentRepository extends JpaRepository<VendorDocument, Integer> {
    List<VendorDocument> findByVendor_IdOrderByIdAsc(Integer vendorId);
}