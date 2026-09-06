package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.RewardCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardCatalogItemRepository extends JpaRepository<RewardCatalogItem, Integer> {
    List<RewardCatalogItem> findByActiveTrue();
}
