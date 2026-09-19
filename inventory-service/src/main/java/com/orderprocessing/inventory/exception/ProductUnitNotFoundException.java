package com.orderprocessing.inventory.exception;

import java.lang.management.RuntimeMXBean;

public class ProductUnitNotFoundException extends RuntimeException {
    public ProductUnitNotFoundException(String inventoryCode){
        super("Product unit not found: " + inventoryCode);
    }
}
