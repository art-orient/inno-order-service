package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.OrderCreateRequestDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.OrderUpdateRequestDto;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller providing CRUD operations and filtering capabilities
 * for managing orders within the system.
 *
 * <p>Supports:
 * <ul>
 *   <li>Creating new orders</li>
 *   <li>Retrieving orders by ID</li>
 *   <li>Filtering orders by date range and status</li>
 *   <li>Retrieving orders by user ID</li>
 *   <li>Updating existing orders</li>
 *   <li>Deleting orders</li>
 * </ul>
 *
 * All business logic is delegated to {@link OrderService}.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  /**
   * Creates a new order.
   *
   * @param dto the request payload containing order creation details
   * @return the created order with generated ID
   */
  @PostMapping
  public ResponseEntity<OrderResponseDto> create(@RequestBody @Valid OrderCreateRequestDto dto) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(orderService.create(dto));
  }

  /**
   * Retrieves an order by its ID.
   *
   * @param id the ID of the order to retrieve
   * @return the order details
   */
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getById(id));
  }

  /**
   * Retrieves a paginated list of orders filtered by optional parameters.
   *
   * @param from     optional start date for filtering
   * @param to       optional end date for filtering
   * @param statuses optional list of order statuses to filter by
   * @param pageable pagination and sorting information
   * @return a page of orders matching the filter criteria
   */
  @GetMapping
  public ResponseEntity<Page<OrderResponseDto>> getWithFilter(
          @RequestParam(required = false) LocalDateTime from,
          @RequestParam(required = false) LocalDateTime to,
          @RequestParam(required = false) List<OrderStatus> statuses,
          Pageable pageable
  ) {
    return ResponseEntity.ok(orderService.getWithFilter(from, to, statuses, pageable));
  }

  /**
   * Retrieves all orders belonging to a specific user.
   *
   * @param userId the ID of the user whose orders should be returned
   * @return list of orders associated with the user
   */
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<OrderResponseDto>> getByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(orderService.getByUserId(userId));
  }

  /**
   * Updates an existing order.
   *
   * @param id  the ID of the order to update
   * @param dto the request payload containing updated order details
   * @return the updated order
   */
  @PutMapping("/{id}")
  public ResponseEntity<OrderResponseDto> update(
          @PathVariable Long id,
          @RequestBody @Valid OrderUpdateRequestDto dto
  ) {
    return ResponseEntity.ok(orderService.update(id, dto));
  }

  /**
   * Deletes an order by its ID.
   *
   * @param id the ID of the order to delete
   * @return HTTP 204 No Content on successful deletion
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    orderService.delete(id);
    return ResponseEntity.noContent().build();
  }
}