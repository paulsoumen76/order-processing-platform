package com.orderprocessing.inventory.controller;

import com.orderprocessing.inventory.dto.TopProductResponse;
import com.orderprocessing.inventory.service.TopProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/top-products")
public class TopProductController {

    private final TopProductService topProductService;

    public TopProductController(TopProductService topProductService) {
        this.topProductService = topProductService;
    }

    @PostMapping("/{productId}/sale")
    public void recordSale(
            @PathVariable String productId,
            @RequestParam int quantity) {

        topProductService.recordSale(productId, quantity);
    }

    @GetMapping("/top")
    public List<TopProductResponse> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {

        return topProductService.getTopProducts(limit);
    }
}