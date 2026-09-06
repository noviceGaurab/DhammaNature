package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByVendor_VerifiedTrue();
    List<Product> findByProductNameContainingIgnoreCase(String name);
}
