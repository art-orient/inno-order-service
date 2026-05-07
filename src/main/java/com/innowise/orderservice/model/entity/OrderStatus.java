package com.innowise.orderservice.model.entity;

public enum OrderStatus {
  CREATED,
  PAID,
  PROCESSING,
  FAILED_PAYMENT,
  COMPLETED,
  CANCELLED
}
