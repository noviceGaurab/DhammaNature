package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.ProductOrder;
import io.virinchi.dhammanature.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

    @Query("SELECT o FROM ProductOrder o JOIN FETCH o.product WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<ProductOrder> findByUser_IdOrderByOrderDateDesc(@Param("userId") Integer userId);

    long countByProduct_Id(Integer productId);

    @Query("SELECT DISTINCT o.product.id FROM ProductOrder o WHERE o.user.id = :userId AND o.status <> 'CANCELLED'")
    List<Integer> findPurchasedProductIdsByUser(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT o.product.id FROM ProductOrder o WHERE o.status <> 'CANCELLED'")
    List<Integer> findAllPurchasedProductIds();

    /** True when any non-cancelled order exists for this product. */
    boolean existsByProduct_IdAndStatusNot(Integer productId, OrderStatus status);
}
