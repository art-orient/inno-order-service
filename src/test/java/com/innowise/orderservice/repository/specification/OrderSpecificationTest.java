package com.innowise.orderservice.repository.specification;

import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderSpecificationTest {

  @Test
  void createdBetween_generatesPredicate() {
    LocalDateTime from = LocalDateTime.now().minusDays(1);
    LocalDateTime to = LocalDateTime.now().plusDays(1);
    Specification<Order> spec = OrderSpecification.createdBetween(from, to);
    assertNotNull(spec);
  }

  @Test
  void hasStatus_generatesPredicate() {
    Specification<Order> spec = OrderSpecification.hasStatus(List.of(OrderStatus.CREATED));
    assertNotNull(spec);
  }
}
