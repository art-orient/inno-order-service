package com.innowise.orderservice.dao;

import com.innowise.orderservice.model.entity.OrderItem;

/**
 * Data Access Object for {@link OrderItem}.
 * Handles persistence operations for order items.
 */
public interface OrderItemDao {

  /**
   * Persists the given order item.
   *
   * @param item the order item to save
   * @return saved order item instance
   */
  OrderItem save(OrderItem item);
}
