package com.innowise.orderservice.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemResponseDto(
        Long id,
        Long itemId,
        String itemName,
        BigDecimal itemPrice,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
