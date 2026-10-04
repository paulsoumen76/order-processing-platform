package com.orderprocesssing.ai.model;

public record AiChatResponse(
        String message,
        AiUiResponse ui
) {
}