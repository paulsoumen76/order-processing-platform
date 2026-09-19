package com.orderprocessing.inventory.repository;

import com.orderprocessing.inventory.model.Product;
import com.orderprocessing.inventory.model.TrackingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Optional<Product> findByProductId(String productId);

    boolean existsByProductId(String productId);

    @Modifying
    @Query("""
    UPDATE Product p
    SET p.quantity = p.quantity + :quantity
    WHERE p.productId = :productId
""")
    void increaseQuantity(
            @Param("productId") String productId,
            @Param("quantity") int quantity
    );

    @Modifying
    @Query("""
    UPDATE Product p
    SET p.quantity = p.quantity - :quantity
    WHERE p.productId = :productId
      AND p.quantity >= :quantity
""")
    int reserveQuantity(
            @Param("productId") String productId,
            @Param("quantity") int quantity
    );

    void delete(Product product);

    Page<Product> findByProductIdContainingIgnoreCaseOrProductNameContainingIgnoreCase(
            String productId,
            String productName,
            Pageable pageable
    );

    Page<Product>
    findByProductIdContainingIgnoreCaseAndTrackingTypeOrProductNameContainingIgnoreCaseAndTrackingType(
            String productId,
            TrackingType trackingType1,
            String productName,
            TrackingType trackingType2,
            Pageable pageable
    );

    List<Product> findByProductIdIn(Collection<String> productIds);
}