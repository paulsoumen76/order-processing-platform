package com.orderprocessing.notification.service;

import com.orderprocessing.notification.event.InventoryReservedEvent;
import com.orderprocessing.notification.event.OrderCreatedEvent;
import com.orderprocessing.notification.event.ProductSavedEvent;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailNotificationService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine) {

        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendProductSavedEmail(ProductSavedEvent event) {

        try {
            Context context = new Context();

            context.setVariable("productName", event.productName());
            context.setVariable("productId", event.productId());
            context.setVariable("quantity", event.quantity());
            context.setVariable("price", event.price());

            String htmlContent =
                    templateEngine.process(
                            "product-saved-email",
                            context
                    );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.notificationEmail());

            helper.setSubject(
                    "Product Added Successfully - "
                            + event.productName()
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);

            System.out.println(
                    "HTML email notification sent to: "
                            + event.notificationEmail()
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send product notification email",
                    e
            );
        }
    }

    public void sendOrderCreatedEmail(OrderCreatedEvent event) {

        try {
            Context context = new Context();

            context.setVariable("orderId", event.orderId());
            context.setVariable("productId", event.productId());
            context.setVariable("quantity", event.quantity());

            String htmlContent =
                    templateEngine.process(
                            "order-created-email",
                            context
                    );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");


            helper.setTo(event.notificationEmail());

            helper.setSubject(
                    "Order Created Successfully - "
                            + event.orderId()
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);

            System.out.println(
                    "Order created email sent for order: "
                            + event.orderId()
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send order created email",
                    e
            );
        }
    }

    public void sendInventoryReservedEmail(
            InventoryReservedEvent event) {

        try {
            Context context = new Context();

            context.setVariable("orderId", event.orderId());
            context.setVariable("productId", event.productId());
            context.setVariable("quantity", event.quantity());
            context.setVariable("reserved", event.reserved());

            String htmlContent =
                    templateEngine.process(
                            "inventory-reserved-email.html",
                            context
                    );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            // Temporary recipient for testing.
            helper.setTo(event.notificationEmail());

            String subject = event.reserved()
                    ? "Inventory Reserved - " + event.orderId()
                    : "Inventory Reservation Failed - " + event.orderId();

            helper.setSubject(subject);

            helper.setText(htmlContent, true);

            mailSender.send(message);

            System.out.println(
                    "Inventory reservation email sent for order: "
                            + event.orderId()
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send inventory reservation email",
                    e
            );
        }
    }
}