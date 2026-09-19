package com.orderprocessing.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.r2")
public record R2Properties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket
) {
}