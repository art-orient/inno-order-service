package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponseDto> items,
        UserDto user) {
}
