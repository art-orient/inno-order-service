package com.innowise.orderservice.dto;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName) {
}
