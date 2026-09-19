package com.orderprocessing.inventory.controller;

import com.orderprocessing.inventory.dto.*;
import com.orderprocessing.inventory.model.TrackingType;
import com.orderprocessing.inventory.service.InventoryProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inventory/products")
public class ProductController {

    private final InventoryProductService inventoryProductService;

    public ProductController(
            InventoryProductService inventoryProductService) {
        this.inventoryProductService = inventoryProductService;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("product") @Valid CreateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        ProductResponse response =
                inventoryProductService.createProduct(request, image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{productId}/quantity")
    public ResponseEntity<ProductResponse> receiveQuantity(
            @PathVariable String productId,
            @Valid @RequestBody ReceiveQuantityRequest request) {

        ProductResponse response =
                inventoryProductService.receiveQuantity(
                        productId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<ProductResponse> reserveQuantity(
            @PathVariable String productId,
            @Valid @RequestBody ReserveQuantityRequest request) {

        ProductResponse response =
                inventoryProductService.reserveQuantity(
                        productId,
                        request.quantity()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TrackingType trackingType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                inventoryProductService.getProducts(
                        search,
                        trackingType,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                inventoryProductService.getProduct(productId)
        );
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<ProductHistoryResponse>> getProductHistory(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                inventoryProductService.getProductHistory(productId)
        );
    }

    @PutMapping(
            value = "/{productId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestPart("product") @Valid UpdateProductRequest request,
            @RequestPart(value = "image", required = false)
            MultipartFile image) {

        ProductResponse response =
                inventoryProductService.updateProduct(
                        productId,
                        request,
                        image
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String productId) {

        inventoryProductService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}