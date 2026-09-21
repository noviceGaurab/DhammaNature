package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Vendor;
import io.virinchi.dhammanature.model.enums.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Integer> {
    List<Vendor> findByStatus(VendorStatus status);
    Optional<Vendor> findByUser_Id(Integer userId);

    /** One-off migration for databases created while Vendor only had a boolean `verified` column. */
    @Modifying
    @Query(value = "UPDATE vendor SET status = CASE WHEN verified = TRUE THEN 'VERIFIED' ELSE 'PENDING' END WHERE status IS NULL", nativeQuery = true)
    void backfillLegacyVerifiedColumn();
}