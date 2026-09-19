package com.orderprocessing.inventory.model;

public enum ProductHistoryAction {

    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    IMAGE_CHANGED,
    QUANTITY_RECEIVED,
    PRODUCT_DELETED,

    UNIT_ADDED,
    UNIT_RESERVED,
    UNIT_STATUS_CHANGED
}