package com.orderprocesssing.ai.controller;


import com.orderprocesssing.ai.model.AiChatResponse;
import com.orderprocesssing.ai.service.AiAssistService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    private final AiAssistService aiAssistService;

    public AiAssistantController(AiAssistService aiAssistService) {
        this.aiAssistService = aiAssistService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody String message) {
        return aiAssistService.chat(message);
    }
}