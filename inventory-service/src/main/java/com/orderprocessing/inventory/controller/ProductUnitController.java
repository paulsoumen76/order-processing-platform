package com.orderprocessing.inventory.controller;

import com.orderprocessing.inventory.dto.ProductUnitResponse;
import com.orderprocessing.inventory.dto.ReceiveProductUnitsRequest;
import com.orderprocessing.inventory.service.ProductUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/products")
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    public ProductUnitController(ProductUnitService productUnitService) {
        this.productUnitService = productUnitService;
    }

    @PostMapping("/{productId}/units")
    public ResponseEntity<List<ProductUnitResponse>> receiveProductUnits(
            @PathVariable String productId,
            @Valid  @RequestBody ReceiveProductUnitsRequest request) {

        List<ProductUnitResponse> productUnits =
                productUnitService.receiveProductUnits(
                        productId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productUnits);
    }

    @GetMapping("/units/{inventoryCode}")
    public ResponseEntity<ProductUnitResponse> getByInventoryCode(
            @PathVariable String inventoryCode) {

        return ResponseEntity.ok(
                productUnitService.getUnitByInventoryCode(inventoryCode)
        );
    }

    @PostMapping("/{productId}/units/reserve")
    public ResponseEntity<List<ProductUnitResponse>> reserveUnits(
            @PathVariable String productId,
            @RequestParam int quantity) {

        List<ProductUnitResponse> reservedUnits =
                productUnitService.reserveUnits(productId, quantity);

        return ResponseEntity.ok(reservedUnits);
    }

    @GetMapping("/{productId}/units")
    public ResponseEntity<List<ProductUnitResponse>> getProductUnits(
            @PathVariable String productId) {

        return ResponseEntity.ok(
                productUnitService.getUnits(productId)
        );
    }

}