package com.github.bwork0.order_service.repository;

import com.github.bwork0.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}