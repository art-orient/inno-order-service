package com.innowise.orderservice.model.dto;

import com.innowise.orderservice.model.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderUpdateRequestDto(
        @NotNull OrderStatus status,
        @NotEmpty List<@Valid OrderItemDto> items) {
}
