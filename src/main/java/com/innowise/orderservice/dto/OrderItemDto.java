package com.innowise.orderservice.dto;

import jakarta.validation.constraints.*;

public record OrderItemDto(
        @NotNull Long itemId,
        @NotNull @Positive Integer quantity) {
}