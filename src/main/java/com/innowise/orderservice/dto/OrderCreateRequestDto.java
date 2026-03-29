package com.innowise.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequestDto(
        @NotBlank String userEmail,
        @NotEmpty List<@Valid OrderItemDto> items) {
}
