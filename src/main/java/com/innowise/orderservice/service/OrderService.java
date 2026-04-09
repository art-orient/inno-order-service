package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.OrderCreateRequestDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateRequestDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing orders.
 * <p>
 * Provides operations for creating, retrieving, updating and deleting orders.
 * All returned responses include user information obtained from User Service.
 * Filtering operations support pagination and optional criteria.
 */
public interface OrderService {

  /**
   * Creates a new order based on the provided request data.
   *
   * @param dto request containing user email and order items
   * @return created order with user information
   */
  OrderResponseDto create(OrderCreateRequestDto dto);

  /**
   * Retrieves an order by its identifier.
   *
   * @param id order identifier
   * @return order with user information
   */
  OrderResponseDto getById(Long id);

  /**
   * Retrieves a paginated list of orders filtered by creation date range and status list.
   *
   * @param from     start of creation date range (inclusive), may be null
   * @param to       end of creation date range (inclusive), may be null
   * @param statuses list of order statuses to filter by, may be empty or null
   * @param pageable pagination parameters
   * @return paginated list of orders with user information
   */
  Page<OrderResponseDto> getWithFilter(LocalDateTime from,
                                       LocalDateTime to,
                                       List<OrderStatus> statuses,
                                       Pageable pageable);

  /**
   * Retrieves all orders belonging to a specific user.
   *
   * @param userId identifier of the user
   * @return list of orders with user information
   */
  List<OrderResponseDto> getByUserId(Long userId);

  /**
   * Updates an existing order.
   *
   * @param id  order identifier
   * @param dto request containing updated status and items
   * @return updated order with user information
   */
  OrderResponseDto update(Long id, OrderUpdateRequestDto dto);

  /**
   * Performs a soft delete of an order.
   *
   * @param id order identifier
   */
  void delete(Long id);
}