package com.orderprocessing.inventory.exception;

public class InvalidTrackingTypeException extends RuntimeException {

    public InvalidTrackingTypeException(String productId) {
        super("Product is not SERIALIZED: " + productId);
    }
}