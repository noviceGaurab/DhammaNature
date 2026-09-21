package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Product;
import io.virinchi.dhammanature.model.enums.ProductCategory;
import io.virinchi.dhammanature.model.enums.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p JOIN FETCH p.vendor v WHERE v.status = :status ORDER BY p.productName")
    List<Product> findByVendor_Status(@Param("status") VendorStatus status);

    Page<Product> findByVendor_Status(VendorStatus status, Pageable pageable);

    long countByVendor_Status(VendorStatus status);

    Page<Product> findByVendor_StatusAndCategory(VendorStatus status, ProductCategory category, Pageable pageable);

    long countByVendor_StatusAndCategory(VendorStatus status, ProductCategory category);

    @Query("SELECT p FROM Product p JOIN FETCH p.vendor v WHERE v.status = :status AND p.category = :category ORDER BY p.productName")
    List<Product> findAvailableByCategory(@Param("status") VendorStatus status,
                                          @Param("category") ProductCategory category);

    List<Product> findByProductNameContainingIgnoreCase(String name);

    List<Product> findByVendor_IdOrderByProductName(Integer vendorId);

    boolean existsByProductNameIgnoreCase(String productName);
}