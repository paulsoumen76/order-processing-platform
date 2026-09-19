package com.orderprocessing.payment.repository;

import com.orderprocessing.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);
}