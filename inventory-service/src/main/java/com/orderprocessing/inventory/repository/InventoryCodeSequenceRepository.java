package com.orderprocessing.inventory.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryCodeSequenceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Long getNextInventoryNumber() {

        Number result = (Number) entityManager
                .createNativeQuery(
                        "SELECT nextval('inventory_code_seq')"
                )
                .getSingleResult();

        return result.longValue();
    }
}