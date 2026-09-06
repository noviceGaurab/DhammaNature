package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {
    List<ProductOrder> findByUser_IdOrderByOrderDateDesc(Integer userId);
}
