package com.orderprocessing.inventory.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    @Bean
    public S3Client s3Client(R2Properties properties) {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        properties.accessKey(),
                        properties.secretKey()
                );

        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(
            R2Properties properties) {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        properties.accessKey(),
                        properties.secretKey()
                );

        return S3Presigner.builder()
                .endpointOverride(
                        URI.create(properties.endpoint())
                )
                .region(Region.of("auto"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }
}