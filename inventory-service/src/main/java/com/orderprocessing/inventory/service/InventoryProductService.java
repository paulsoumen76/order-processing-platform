package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.dto.*;
import com.orderprocessing.inventory.exception.InsufficientInventoryException;
import com.orderprocessing.inventory.exception.InvalidTrackingTypeException;
import com.orderprocessing.inventory.exception.ProductAlreadyExistsException;
import com.orderprocessing.inventory.exception.ProductNotFoundException;
import com.orderprocessing.inventory.model.Product;
import com.orderprocessing.inventory.model.ProductHistoryAction;
import com.orderprocessing.inventory.model.TrackingType;
import com.orderprocessing.inventory.repository.ProductRepository;
import com.orderprocessing.inventory.storage.ImageStorageService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class InventoryProductService {

    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;
    private final ProductTransactionService productTransactionService;
    private final ProductHistoryService productHistoryService;
    private final ProductUnitService productUnitService;

    public InventoryProductService(ProductRepository productRepository, ImageStorageService imageStorageService, ProductTransactionService productTransactionService, ProductHistoryService productHistoryService, ProductUnitService productUnitService, InventorySseService inventorySseService) {
        this.productRepository = productRepository;
        this.productHistoryService = productHistoryService;
        this.imageStorageService = imageStorageService;
        this.productTransactionService = productTransactionService;
        this.productUnitService = productUnitService;
    }

    public ProductResponse createProduct(
            CreateProductRequest request,
            MultipartFile image) {

        if (productRepository.existsByProductId(request.productId())) {
            throw new ProductAlreadyExistsException(
                    request.productId()
            );
        }

        String imageKey = null;

        if (image != null && !image.isEmpty()) {

            validateImage(image);

            imageKey = generateImageKey(
                    request.productId(),
                    image
            );

            // If this fails, ProductTransactionService
            // is never called.
            imageStorageService.upload(
                    imageKey,
                    image
            );
        }

        try {

            ProductResponse response = productTransactionService.saveProduct(
                    request,
                    imageKey
            );
            return addImageUrl(response, imageKey);

        } catch (Exception exception) {

            if (imageKey != null) {

                try {
                    imageStorageService.delete(imageKey);

                } catch (Exception cleanupException) {

                    System.err.println(
                            "Failed to cleanup R2 image: "
                                    + imageKey
                    );

                    cleanupException.printStackTrace();
                }
            }

            throw exception;
        }
    }

    private ProductResponse addImageUrl(
            ProductResponse response,
            String imageKey) {

        String imageUrl = imageKey == null
                ? null
                : imageStorageService.getUrl(imageKey);

        return new ProductResponse(
                response.productId(),
                response.productName(),
                response.price(),
                response.quantity(),
                response.trackingType(),
                response.notificationEmail(),
                response.notificationMobile(),
                imageUrl,
                response.createdAt(),
                response.inventorySummary()
        );
    }

    private String generateImageKey(
            String productId,
            MultipartFile image) {

        String extension = getFileExtension(
                image.getOriginalFilename()
        );

        return "product-images/"
                + productId
                + "/"
                + UUID.randomUUID()
                + extension;
    }

    private String getFileExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return ".bin";
        }

        return filename.substring(
                filename.lastIndexOf(".")
        ).toLowerCase(Locale.ROOT);
    }

    private void validateImage(MultipartFile image) {

        String contentType = image.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png")
                        && !contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                    "Only JPG, PNG and WebP images are supported"
            );
        }

        if (image.getSize() > 5 * 1024 * 1024) {

            throw new IllegalArgumentException(
                    "Image size must not exceed 5 MB"
            );
        }
    }

    private ProductResponse toProductResponse(
            Product product) {

        String imageUrl = product.getImageKey() == null
                ? null
                : imageStorageService.getUrl(
                product.getImageKey()
        );

        ProductInventorySummary summary =
                productUnitService.getInventorySummary(
                        product.getProductId()
                );

        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                product.getQuantity(),
                product.getTrackingType(),
                product.getNotificationEmail(),
                product.getNotificationMobile(),
                imageUrl,
                product.getCreatedAt(),
                summary
        );
    }

    @Transactional
    public ProductResponse receiveQuantity(
            String productId,
            ReceiveQuantityRequest request) {

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        if (product.getTrackingType() != TrackingType.QUANTITY) {
            throw new InvalidTrackingTypeException(productId);
        }
        int oldQuantity = product.getQuantity();
        product.setQuantity(
                product.getQuantity() + request.quantity()
        );

        productRepository.increaseQuantity(
                productId,
                request.quantity()
        );
        Product updatedProduct = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));
        int newQuantity = product.getQuantity();

        productHistoryService.record(
                productId,
                ProductHistoryAction.QUANTITY_RECEIVED,
                "quantity",
                String.valueOf(oldQuantity),
                String.valueOf(newQuantity)
        );

        return toProductResponse(updatedProduct);
    }

    @Transactional
    public ProductResponse reserveQuantity(
            String productId,
            int requestedQuantity) {

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        if (product.getTrackingType() != TrackingType.QUANTITY) {
            throw new InvalidTrackingTypeException(productId);
        }

        int updatedRows = productRepository.reserveQuantity(
                productId,
                requestedQuantity
        );

        if (updatedRows == 0) {
            throw new InsufficientInventoryException(
                    productId,
                    requestedQuantity
            );
        }

        Product updatedProduct = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        return toProductResponse(updatedProduct);
    }


    public ProductResponse getProduct(String productId) {

        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        return toProductResponse(product);
    }

    public List<ProductHistoryResponse> getProductHistory(
            String productId) {

        if (!productRepository.existsByProductId(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return productHistoryService.getHistory(productId);
    }

    public ProductResponse updateProduct(
            String productId,
            UpdateProductRequest request,
            MultipartFile image) {

        Product existingProduct =
                productRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new ProductNotFoundException(productId));

        String oldImageKey =
                existingProduct.getImageKey();

        String newImageKey = null;

        // Upload new image first.
        if (image != null && !image.isEmpty()) {

            validateImage(image);

            newImageKey = generateImageKey(
                    productId,
                    image
            );

            imageStorageService.upload(
                    newImageKey,
                    image
            );
        }

        try {

            Product updatedProduct =
                    productTransactionService.updateProduct(
                            productId,
                            request,
                            newImageKey
                    );

            /*
             * DB transaction succeeded.
             *
             * Now the old image is no longer referenced.
             */
            if (newImageKey != null &&
                    oldImageKey != null) {

                try {
                    imageStorageService.delete(
                            oldImageKey
                    );

                } catch (Exception cleanupException) {

                    System.err.println(
                            "Failed to delete old product image: "
                                    + oldImageKey
                    );

                }
            }

            return toProductResponse(updatedProduct);

        } catch (Exception exception) {

            /*
             * DB failed.
             *
             * The new image isn't referenced by DB,
             * so clean it up.
             */
            if (newImageKey != null) {

                try {
                    imageStorageService.delete(
                            newImageKey
                    );

                } catch (Exception cleanupException) {

                    System.err.println(
                            "Failed to cleanup new product image: "
                                    + newImageKey
                    );

                }
            }

            throw exception;
        }
    }

    public void deleteProduct(String productId) {

        Product product = productRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        String imageKey = product.getImageKey();

        productTransactionService.deleteProduct(productId);

        if (imageKey != null) {
            try {
                imageStorageService.delete(imageKey);
            } catch (Exception exception) {

                System.err.println(
                        "Failed to delete product image: "
                                + imageKey
                );
            }
        }
    }

    public Page<ProductResponse> getProducts(
            String search,
            TrackingType trackingType,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to zero"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        String searchValue =
                search == null ? "" : search.trim();

        Page<Product> products;

        if (trackingType == null) {

            products =
                    productRepository
                            .findByProductIdContainingIgnoreCaseOrProductNameContainingIgnoreCase(
                                    searchValue,
                                    searchValue,
                                    pageable
                            );

        } else {

            products =
                    productRepository
                            .findByProductIdContainingIgnoreCaseAndTrackingTypeOrProductNameContainingIgnoreCaseAndTrackingType(
                                    searchValue,
                                    trackingType,
                                    searchValue,
                                    trackingType,
                                    pageable
                            );
        }

        return products.map(this::toProductResponse);
    }
}