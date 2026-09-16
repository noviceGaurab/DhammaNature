package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {
    List<ProductReview> findByProduct_IdOrderByCreatedAtDesc(Integer productId);

    long countByProduct_Id(Integer productId);
}
