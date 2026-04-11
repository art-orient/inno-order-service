package com.innowise.orderservice.model.dto;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName) {
}
