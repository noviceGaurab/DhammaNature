package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Integer> {
    List<Vendor> findByVerifiedTrue();
    Optional<Vendor> findByUser_Id(Integer userId);
}
