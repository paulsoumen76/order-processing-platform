package com.orderprocessing.order.client;

import com.orderprocessing.order.dto.ProductResponse;
import com.orderprocessing.order.exception.ProductNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductResponse getProduct(String productId) {

        try {
            return restClient.get()
                    .uri("/inventory/products/{productId}", productId)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) -> {
                                if (response.getStatusCode().value() == 404) {
                                    throw new ProductNotFoundException(productId);
                                }

                                throw new RuntimeException(
                                        "Inventory service returned: "
                                                + response.getStatusCode()
                                );
                            }
                    )
                    .body(ProductResponse.class);

        } catch (ProductNotFoundException exception) {
            throw exception;
        }
    }
}