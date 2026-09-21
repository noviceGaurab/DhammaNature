package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.SiteAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteAssetRepository extends JpaRepository<SiteAsset, Integer> {
    Optional<SiteAsset> findByName(String name);
}