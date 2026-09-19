package com.orderprocessing.inventory.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String productId) {
        super("Product already exists: " + productId);
    }
}