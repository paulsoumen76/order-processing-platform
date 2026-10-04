package com.orderprocesssing.ai.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient(
            @Value("${openai.api-key}") String apiKey) {

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}