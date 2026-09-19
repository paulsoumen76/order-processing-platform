package com.orderprocessing.inventory.repository;

import com.orderprocessing.inventory.model.InventoryUnitStatus;
import com.orderprocessing.inventory.model.ProductUnit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductUnitRepository
        extends JpaRepository<ProductUnit, Long> {

    Optional<ProductUnit> findByInventoryCode(String inventoryCode);

    Optional<ProductUnit> findBySerialNumber(String serialNumber);

    List<ProductUnit> findByProduct_ProductId(String productId);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByInventoryCode(String inventoryCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT pu
    FROM ProductUnit pu
    WHERE pu.product.productId = :productId
      AND pu.status = :status
    ORDER BY pu.id
""")
    List<ProductUnit> findAvailableUnitsForUpdate(
            @Param("productId") String productId,
            @Param("status") InventoryUnitStatus status
    );

    @Query("""
SELECT pu.status, COUNT(pu)
FROM ProductUnit pu
WHERE pu.product.productId = :productId
GROUP BY pu.status
""")
    List<Object[]> countUnitsByStatus(
            @Param("productId") String productId
    );

    boolean existsByProduct_ProductId(String productId);
}