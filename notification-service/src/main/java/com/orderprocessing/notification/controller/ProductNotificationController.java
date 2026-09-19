package com.orderprocessing.notification.controller;

import com.orderprocessing.notification.dto.ProductNotificationResponse;
import com.orderprocessing.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications/products")
public class ProductNotificationController {

    private final NotificationService
            notificationService;

    public ProductNotificationController(
            NotificationService notificationService) {

        this.notificationService =
                notificationService;
    }

    @GetMapping("/{productId}")
    public List<ProductNotificationResponse> getNotifications(
            @PathVariable String productId) {

        return notificationService
                .getNotifications(productId);
    }
}