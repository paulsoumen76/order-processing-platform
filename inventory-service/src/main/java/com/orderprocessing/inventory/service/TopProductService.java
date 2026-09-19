package com.orderprocessing.inventory.service;

import com.orderprocessing.inventory.dto.TopProductResponse;
import com.orderprocessing.inventory.model.Product;
import com.orderprocessing.inventory.repository.ProductRepository;
import com.orderprocessing.inventory.storage.ImageStorageService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopProductService {

    private static final String TOP_PRODUCTS_KEY = "top-products";

    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository;
    private final ImageStorageService imageStorageService;

    public TopProductService(
            StringRedisTemplate redisTemplate,
            ProductRepository productRepository,
            ImageStorageService imageStorageService
            ) {

        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.imageStorageService = imageStorageService;
    }

    public void recordSale(String productId, int quantity) {

        redisTemplate.opsForZSet()
                .incrementScore(
                        TOP_PRODUCTS_KEY,
                        productId,
                        quantity
                );
    }

    public List<TopProductResponse> getTopProducts(int limit) {

        Set<String> productIds =
                redisTemplate.opsForZSet()
                        .reverseRange(
                                TOP_PRODUCTS_KEY,
                                0,
                                limit - 1
                        );

        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        List<Product> products =
                productRepository.findByProductIdIn(productIds);

        Map<String, Product> productMap =
                products.stream()
                        .collect(Collectors.toMap(
                                Product::getProductId,
                                product -> product
                        ));

        return productIds.stream()
                .map(productId -> {

                    Product product = productMap.get(productId);

                    if (product == null) {
                        return null;
                    }

                    Double score =
                            redisTemplate.opsForZSet()
                                    .score(
                                            TOP_PRODUCTS_KEY,
                                            productId
                                    );

                    return new TopProductResponse(
                            product.getProductId(),
                            product.getProductName(),
                            product.getPrice(),
                            product.getQuantity(),
                            product.getImageKey() == null
                                    ? null
                                    : imageStorageService.getUrl(
                                    product.getImageKey()
                            ),
                            score == null
                                    ? 0
                                    : score.longValue()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}