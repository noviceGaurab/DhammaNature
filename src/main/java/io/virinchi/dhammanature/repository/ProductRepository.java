package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Product;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p JOIN FETCH p.vendor v WHERE v.verified = true ORDER BY p.productName")
    List<Product> findByVendor_VerifiedTrue();

    @Query("SELECT p FROM Product p JOIN FETCH p.vendor v WHERE v.verified = true AND p.category = :category ORDER BY p.productName")
    List<Product> findAvailableByCategory(@Param("category") ProductCategory category);

    List<Product> findByProductNameContainingIgnoreCase(String name);

    List<Product> findByVendor_IdOrderByProductName(Integer vendorId);

    boolean existsByProductNameIgnoreCase(String productName);
}
