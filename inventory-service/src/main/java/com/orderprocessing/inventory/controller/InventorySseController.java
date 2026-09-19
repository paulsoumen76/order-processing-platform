package com.orderprocessing.inventory.controller;

import com.orderprocessing.inventory.service.InventorySseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/inventory")
public class InventorySseController {

    private final InventorySseService inventorySseService;

    public InventorySseController(
            InventorySseService inventorySseService) {

        this.inventorySseService = inventorySseService;
    }

    @GetMapping(
            value = "/events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribe() {

        return inventorySseService.subscribe();
    }
}