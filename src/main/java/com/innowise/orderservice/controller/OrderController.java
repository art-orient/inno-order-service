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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderResponseDto> create(@RequestBody @Valid OrderCreateRequestDto dto) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(orderService.create(dto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getById(id));
  }

  @GetMapping
  public ResponseEntity<Page<OrderResponseDto>> getWithFilter(
          @RequestParam(required = false) LocalDateTime from,
          @RequestParam(required = false) LocalDateTime to,
          @RequestParam(required = false) List<OrderStatus> statuses,
          Pageable pageable
  ) {
    return ResponseEntity.ok(orderService.getWithFilter(from, to, statuses, pageable));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<OrderResponseDto>> getByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(orderService.getByUserId(userId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderResponseDto> update(
          @PathVariable Long id,
          @RequestBody @Valid OrderUpdateRequestDto dto
  ) {
    return ResponseEntity.ok(orderService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    orderService.delete(id);
    return ResponseEntity.noContent().build();
  }
}