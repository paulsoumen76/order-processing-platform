package com.orderprocessing.inventory.exception;

public class InsufficientSerializedInventoryException
        extends RuntimeException {

    public InsufficientSerializedInventoryException(
            String productId,
            int requestedQuantity,
            int availableQuantity) {

        super("Insufficient serialized inventory for product "
                + productId
                + ". Requested: "
                + requestedQuantity
                + ", Available: "
                + availableQuantity);
    }
}