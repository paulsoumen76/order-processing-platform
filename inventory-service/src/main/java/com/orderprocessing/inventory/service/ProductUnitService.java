package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.dto.ProductInventorySummary;
import com.orderprocessing.inventory.dto.ProductUnitRequest;
import com.orderprocessing.inventory.dto.ProductUnitResponse;
import com.orderprocessing.inventory.dto.ReceiveProductUnitsRequest;
import com.orderprocessing.inventory.exception.*;
import com.orderprocessing.inventory.model.*;
import com.orderprocessing.inventory.repository.InventoryCodeSequenceRepository;
import com.orderprocessing.inventory.repository.ProductUnitRepository;
import com.orderprocessing.inventory.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final ProductRepository productRepository;
    private final InventoryCodeSequenceRepository inventoryCodeSequenceRepository;
    private final ProductHistoryService productHistoryService;

    public ProductUnitService(
            ProductUnitRepository productUnitRepository,
            ProductRepository productRepository, InventoryCodeSequenceRepository inventoryCodeSequenceRepository, ProductHistoryService productHistoryService) {

        this.productUnitRepository = productUnitRepository;
        this.productRepository = productRepository;
        this.inventoryCodeSequenceRepository = inventoryCodeSequenceRepository;
        this.productHistoryService = productHistoryService;
    }
    public List<ProductUnitResponse> receiveProductUnits(
            String productId,
            ReceiveProductUnitsRequest request) {

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found: " + productId));

        if (product.getTrackingType() != TrackingType.SERIALIZED) {
            throw new InvalidTrackingTypeException(
                    "Product is not SERIALIZED: " + productId);
        }

        List<ProductUnit> productUnits = new ArrayList<>();
        Set<String> serialNumbers = new HashSet<>();
        for (ProductUnitRequest unitRequest : request.units()) {
            if (!serialNumbers.add(unitRequest.serialNumber())) {
                throw new DuplicateSerialNumberException(
                        unitRequest.serialNumber());
            }
            if (productUnitRepository.existsBySerialNumber(
                    unitRequest.serialNumber())) {

                throw new DuplicateSerialNumberException(
                        unitRequest.serialNumber());
            }

            Long inventoryNumber =
                    inventoryCodeSequenceRepository.getNextInventoryNumber();

            String inventoryCode = String.format(
                    "INV-%06d",
                    inventoryNumber
            );

            ProductUnit productUnit = new ProductUnit();

            productUnit.setProduct(product);
            productUnit.setSerialNumber(unitRequest.serialNumber());
            productUnit.setInventoryCode(inventoryCode);
            productUnit.setStatus(InventoryUnitStatus.AVAILABLE);
            productUnit.setCreatedAt(LocalDateTime.now());

            productUnits.add(productUnit);
        }

        List<ProductUnit> savedUnits = productUnitRepository.saveAll(productUnits);

        for (ProductUnit unit : savedUnits) {

            productHistoryService.record(
                    productId,
                    ProductHistoryAction.UNIT_ADDED,
                    "serialNumber",
                    null,
                    unit.getSerialNumber()
            );
        }

        return savedUnits.stream()
                .map(unit -> new ProductUnitResponse(
                        unit.getProduct().getProductId(),
                        unit.getProduct().getProductName(),
                        unit.getSerialNumber(),
                        unit.getInventoryCode(),
                        unit.getStatus(),
                        unit.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public List<ProductUnitResponse> reserveUnits(
            String productId,
            int requestedQuantity) {

        List<ProductUnit> availableUnits =
                productUnitRepository.findAvailableUnitsForUpdate(
                        productId,
                        InventoryUnitStatus.AVAILABLE
                );

        if (availableUnits.size() < requestedQuantity) {
            throw new InsufficientSerializedInventoryException(
                    productId,
                    requestedQuantity,
                    availableUnits.size()
            );
        }

        List<ProductUnit> unitsToReserve =
                availableUnits.subList(0, requestedQuantity);

        for (ProductUnit unit : unitsToReserve) {
            unit.setStatus(InventoryUnitStatus.RESERVED);
            productHistoryService.record(
                    productId,
                    ProductHistoryAction.UNIT_RESERVED,
                    unit.getSerialNumber(),
                    InventoryUnitStatus.AVAILABLE.name(),
                    InventoryUnitStatus.RESERVED.name()
            );
        }

        productUnitRepository.saveAll(unitsToReserve);

        return unitsToReserve.stream()
                .map(unit -> new ProductUnitResponse(
                        unit.getProduct().getProductId(),
                        unit.getProduct().getProductName(),
                        unit.getSerialNumber(),
                        unit.getInventoryCode(),
                        unit.getStatus(),
                        unit.getCreatedAt()
                ))
                .toList();
    }

    public List<ProductUnitResponse> getUnits(String productId) {

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        if (product.getTrackingType() != TrackingType.SERIALIZED) {
            throw new InvalidTrackingTypeException(productId);
        }

        return productUnitRepository
                .findByProduct_ProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductUnitResponse getUnitByInventoryCode(
            String inventoryCode) {

        ProductUnit unit =
                productUnitRepository
                        .findByInventoryCode(inventoryCode)
                        .orElseThrow(() ->
                                new ProductUnitNotFoundException(
                                        inventoryCode
                                ));

        return toResponse(unit);
    }

    private ProductUnitResponse toResponse(ProductUnit unit) {

        return new ProductUnitResponse(
                unit.getProduct().getProductId(),
                unit.getProduct().getProductName(),
                unit.getSerialNumber(),
                unit.getInventoryCode(),
                unit.getStatus(),
                unit.getCreatedAt()
        );
    }

    public ProductInventorySummary getInventorySummary(
            String productId) {

        Product product = productRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        if (product.getTrackingType() == TrackingType.QUANTITY) {

            return new ProductInventorySummary(
                    product.getQuantity(),
                    product.getQuantity(),
                    0,
                    0,
                    0
            );
        }

        List<Object[]> results =
                productUnitRepository.countUnitsByStatus(
                        productId
                );

        int available = 0;
        int reserved = 0;
        int sold = 0;
        int damaged = 0;

        for (Object[] row : results) {

            InventoryUnitStatus status =
                    (InventoryUnitStatus) row[0];

            int count =
                    ((Number) row[1]).intValue();

            switch (status) {
                case AVAILABLE -> available = count;
                case RESERVED -> reserved = count;
                case SOLD -> sold = count;
                case DAMAGED -> damaged = count;
            }
        }

        int total =
                available
                        + reserved
                        + sold
                        + damaged;

        return new ProductInventorySummary(
                total,
                available,
                reserved,
                sold,
                damaged
        );
    }

}