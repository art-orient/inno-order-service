package com.innowise.orderservice.dao;

import java.math.BigDecimal;

public record PaymentEventDto(
        String paymentId,
        Long orderId,
        Long userId,
        String status,
        BigDecimal amount
) {}
