package com.innowise.orderservice.repository.specification;

import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecification {

  public static Specification<Order> createdBetween(LocalDateTime from, LocalDateTime to) {
    return (root, query, cb) -> {
      if (from == null || to == null) {
        return null;
      }
      return cb.between(root.get("createdAt"), from, to);
    };
  }

  public static Specification<Order> hasStatus(List<OrderStatus> statusList) {
    return (root, query, cb) -> {
      if (statusList == null || statusList.isEmpty()) {
        return null;
      }
      return root.get("status").in(statusList);
    };
  }
}
