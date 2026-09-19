package com.orderprocessing.inventory.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_units",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_unit_serial",
                        columnNames = "serial_number"
                ),
                @UniqueConstraint(
                        name = "uk_product_unit_inventory_code",
                        columnNames = "inventory_code"
                )
        }
)
public class ProductUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "inventory_code", nullable = false)
    private String inventoryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryUnitStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProductUnit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getInventoryCode() {
        return inventoryCode;
    }

    public void setInventoryCode(String inventoryCode) {
        this.inventoryCode = inventoryCode;
    }

    public InventoryUnitStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryUnitStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}