package com.innowise.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderUpdateRequestDto(
        @NotNull com.innowise.orderservice.entity.OrderStatus status,
        @NotEmpty List<@Valid OrderItemDto> items) {
}
