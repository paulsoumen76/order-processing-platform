package com.orderprocessing.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderprocessing.inventory.dto.CreateProductRequest;
import com.orderprocessing.inventory.dto.ProductCreatedSseEvent;
import com.orderprocessing.inventory.dto.ProductResponse;
import com.orderprocessing.inventory.dto.UpdateProductRequest;
import com.orderprocessing.inventory.event.ProductSavedEvent;
import com.orderprocessing.inventory.exception.ProductAlreadyExistsException;
import com.orderprocessing.inventory.exception.ProductDeletionNotAllowedException;
import com.orderprocessing.inventory.exception.ProductNotFoundException;
import com.orderprocessing.inventory.model.*;
import com.orderprocessing.inventory.repository.OutboxEventRepository;
import com.orderprocessing.inventory.repository.ProductRepository;
import com.orderprocessing.inventory.repository.ProductUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductTransactionService {

    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ProductHistoryService productHistoryService;
    private final ProductUnitRepository productUnitRepository;
    private final InventorySseService inventorySseService;

    public ProductTransactionService(
            ProductRepository productRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper, ProductHistoryService productHistoryService, ProductUnitRepository productUnitRepository, InventorySseService inventorySseService) {

        this.productRepository = productRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.productHistoryService = productHistoryService;
        this.productUnitRepository = productUnitRepository;
        this.inventorySseService = inventorySseService;
    }

    @Transactional
    public ProductResponse saveProduct(
            CreateProductRequest request,
            String imageKey) {

        // Re-check inside the transaction.
        // This protects against two concurrent create requests.
        if (productRepository.existsByProductId(request.productId())) {
            throw new ProductAlreadyExistsException(
                    request.productId()
            );
        }

        int quantity = request.trackingType() == TrackingType.SERIALIZED
                ? 0
                : request.quantity();

        Product product = new Product();

        product.setProductId(request.productId());
        product.setProductName(request.productName());
        product.setPrice(request.price());
        product.setQuantity(quantity);
        product.setImageKey(imageKey);
        product.setTrackingType(request.trackingType());
        product.setNotificationEmail(request.notificationEmail());
        product.setNotificationMobile(request.notificationMobile());
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        ProductSavedEvent event = new ProductSavedEvent(
                UUID.randomUUID().toString(),
                savedProduct.getProductId(),
                savedProduct.getProductName(),
                savedProduct.getPrice(),
                savedProduct.getQuantity(),
                savedProduct.getNotificationEmail(),
                savedProduct.getNotificationMobile(),
                savedProduct.getCreatedAt()
        );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(
                    "Failed to serialize product saved event",
                    exception
            );
        }

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventType("ProductSavedEvent");
        outboxEvent.setAggregateId(
                savedProduct.getProductId()
        );
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(
                OutboxEventStatus.PENDING
        );
        outboxEvent.setCreatedAt(
                LocalDateTime.now()
        );

        outboxEventRepository.save(outboxEvent);
        productHistoryService.record(
                savedProduct.getProductId(),
                ProductHistoryAction.PRODUCT_CREATED,
                null,
                null,
                "Product created"
        );
        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                inventorySseService.publish(
                                        "PRODUCT_CREATED",
                                        new ProductCreatedSseEvent(
                                                product.getProductId(),
                                                product.getProductName()
                                        )
                                );
                            }
                        }
                );
        return new ProductResponse(
                savedProduct.getProductId(),
                savedProduct.getProductName(),
                savedProduct.getPrice(),
                savedProduct.getQuantity(),
                savedProduct.getNotificationEmail(),
                savedProduct.getNotificationMobile(),
                null,
                savedProduct.getCreatedAt(),
                null
        );
    }

    @Transactional
    public Product updateProduct(
            String productId,
            UpdateProductRequest request,
            String newImageKey) {

        Product product = productRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        String oldImageKey = product.getImageKey();

        product.setProductName(request.productName());
        product.setPrice(request.price());
        product.setNotificationEmail(
                request.notificationEmail()
        );
        product.setNotificationMobile(
                request.notificationMobile()
        );

        if (newImageKey != null) {
            product.setImageKey(newImageKey);
        }

        Product savedProduct = productRepository.save(product);

        productHistoryService.record(
                productId,
                ProductHistoryAction.PRODUCT_UPDATED,
                "product",
                null,
                "Product details updated"
        );

        if (newImageKey != null) {

            productHistoryService.record(
                    productId,
                    ProductHistoryAction.IMAGE_CHANGED,
                    "imageKey",
                    oldImageKey,
                    newImageKey
            );
        }
        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCommit() {

                                inventorySseService.publish(
                                        "PRODUCT_UPDATED",
                                        new ProductCreatedSseEvent(
                                                product.getProductId(),
                                                product.getProductName()
                                        )
                                );
                            }
                        }
                );
        return savedProduct;
    }

    @Transactional
    public void deleteProduct(String productId) {

        Product product = productRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        if (product.getTrackingType() == TrackingType.QUANTITY
                && product.getQuantity() > 0) {

            throw new ProductDeletionNotAllowedException(
                    "Product cannot be deleted while stock is available"
            );
        }

        if (product.getTrackingType() == TrackingType.SERIALIZED) {

            boolean hasUnits =
                    productUnitRepository
                            .existsByProduct_ProductId(productId);

            if (hasUnits) {
                throw new ProductDeletionNotAllowedException(
                        "Serialized product cannot be deleted while inventory units exist"
                );
            }
        }

        productHistoryService.record(
                productId,
                ProductHistoryAction.PRODUCT_DELETED,
                null,
                "ACTIVE",
                "DELETED"
        );

        productRepository.delete(product);
    }

}