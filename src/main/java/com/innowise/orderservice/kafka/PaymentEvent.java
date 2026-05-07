package com.innowise.orderservice.kafka;

import com.innowise.orderservice.model.entity.PaymentStatus;

import java.time.Instant;

public record PaymentEvent(
        Long orderId,
        String paymentId,
        PaymentStatus status,
        Instant timestamp
) {}