package com.orderprocessing.inventory.exception;

public class DuplicateSerialNumberException extends RuntimeException {

    public DuplicateSerialNumberException(String serialNumber) {
        super("Duplicate serial number: " + serialNumber);
    }
}