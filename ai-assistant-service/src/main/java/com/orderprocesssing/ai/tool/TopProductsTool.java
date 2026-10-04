package com.orderprocesssing.ai.tool;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TopProductsTool {

    private final RestClient restClient;

    public TopProductsTool(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:8080")
                .build();
    }

    public String getTopOrderedProducts(int limit) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventory/top-products/top")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(String.class);
    }
}