package com.orderprocessing.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RazorpayConfig {

    @Bean
    public RestClient razorpayRestClient(RazorpayProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeaders(headers ->
                        headers.setBasicAuth(
                                properties.keyId(),
                                properties.keySecret()
                        )
                )
                .build();
    }
}