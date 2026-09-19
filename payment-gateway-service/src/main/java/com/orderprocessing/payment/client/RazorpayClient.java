package com.orderprocessing.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RazorpayClient {

    private final RestClient restClient;

    public RazorpayClient(RestClient razorpayRestClient) {
        this.restClient = razorpayRestClient;
    }

    public RazorpayOrderResponse createOrder(
            String orderId,
            BigDecimal amount,
            String currency
    ) {

        long amountInPaise = amount
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        RazorpayOrderRequest request = new RazorpayOrderRequest(
                amountInPaise,
                currency,
                orderId
        );

        return restClient.post()
                .uri("/orders")
                .body(request)
                .retrieve()
                .body(RazorpayOrderResponse.class);
    }

    public record RazorpayOrderRequest(
            long amount,
            String currency,
            String receipt
    ) {
    }

    public record RazorpayOrderResponse(
            String id,
            String entity,
            long amount,
            String currency,
            String receipt,
            String status
    ) {
    }
}