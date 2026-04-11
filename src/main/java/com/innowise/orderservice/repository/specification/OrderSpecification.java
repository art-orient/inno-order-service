package com.innowise.orderservice.repository.specification;

import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSpecification {

  private OrderSpecification() {
  }

  public static Specification<Order> createdBetween(LocalDateTime from, LocalDateTime to) {
    return (root, query, cb) -> {
      Expression<LocalDateTime> field = root.get("createdAt");
      Predicate predicate = null;
      if (from != null && to != null) {
        predicate = cb.between(field, from, to);
      } else if (from != null) {
        predicate = cb.greaterThanOrEqualTo(field, from);
      } else if (to != null) {
        predicate = cb.lessThanOrEqualTo(field, to);
      }
      return predicate;
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
