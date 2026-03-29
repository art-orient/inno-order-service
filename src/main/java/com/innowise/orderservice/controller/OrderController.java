package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService service;

  public OrderController(OrderService service) {
    this.service = service;
  }

  @PostMapping
  public OrderResponseDto create(@Valid @RequestBody OrderCreateRequestDto dto) {
    return service.create(dto);
  }

  @GetMapping("/{id}")
  public OrderResponseDto getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @GetMapping
  public Page<OrderResponseDto> getWithFilter(
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
          @RequestParam(required = false) List<OrderStatus> statuses,
          Pageable pageable
  ) {
    return service.getWithFilter(from, to, statuses, pageable);
  }

  @GetMapping("/user/{userId}")
  public List<OrderResponseDto> getByUserId(@PathVariable Long userId) {
    return service.getByUserId(userId);
  }

  @PutMapping("/{id}")
  public OrderResponseDto update(@PathVariable Long id,
                                 @Valid @RequestBody OrderUpdateRequestDto dto) {
    return service.update(id, dto);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
