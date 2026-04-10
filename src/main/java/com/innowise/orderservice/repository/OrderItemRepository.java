package com.innowise.orderservice.repository;

import com.innowise.orderservice.dao.OrderItemDao;
import com.innowise.orderservice.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemDao {
}
