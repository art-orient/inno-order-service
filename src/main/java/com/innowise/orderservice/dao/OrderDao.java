package com.innowise.orderservice.dao;

import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Order}.
 * Encapsulates all database operations related to orders.
 */
public interface OrderDao {

  /**
   * Retrieves an order by its identifier.
   *
   * @param id the order identifier
   * @return optional containing the order if found
   */
  Optional<Order> findById(Long id);

  /**
   * Retrieves all orders belonging to a specific user.
   *
   * @param userId the user identifier
   * @return list of orders for the given user
   */
  List<Order> findByUserId(Long userId);

  /**
   * Retrieves orders matching the provided specification and pagination.
   *
   * @param spec     filtering specification
   * @param pageable pagination parameters
   * @return page of orders matching the criteria
   */
  Page<Order> findAll(Specification<Order> spec, Pageable pageable);

  /**
   * Persists the given order.
   *
   * @param order the order to save
   * @return saved order instance
   */
  Order save(Order order);

  /**
   * Deletes the given order.
   *
   * @param order the order to delete
   */
  void delete(Order order);
}
