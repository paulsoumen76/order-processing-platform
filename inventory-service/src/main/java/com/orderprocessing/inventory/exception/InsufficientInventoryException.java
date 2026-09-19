package com.orderprocessing.inventory.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(
            String productId,
            int requestedQuantity) {

        super(
                "Insufficient inventory for product: "
                        + productId
                        + ". Requested: "
                        + requestedQuantity
        );
    }
}