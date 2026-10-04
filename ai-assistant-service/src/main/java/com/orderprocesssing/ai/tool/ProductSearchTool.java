package com.orderprocesssing.ai.tool;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductSearchTool {

    private final RestClient restClient;

    public ProductSearchTool(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:8080")
                .build();
    }

    public String searchProducts(String search) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventory/products")
                        .queryParam("search", search)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .retrieve()
                .body(String.class);
    }
}