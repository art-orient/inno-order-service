package com.innowise.orderservice.service;

import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;

  // CREATE
  public Order create(Order order) {
    order.getItems().forEach(oi -> oi.setOrder(order));
    return orderRepository.save(order);
  }

  public Order getById(Long id) {
    return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
  }

  public Page<Order> getAll(LocalDateTime from,
                            LocalDateTime to,
                            List<OrderStatus> statusList,
                            Pageable pageable) {

    Specification<Order> spec = Specification.allOf(
            OrderSpecification.createdBetween(from, to),
            OrderSpecification.hasStatus(statusList));

    return orderRepository.findAll(spec, pageable);
  }

  public List<Order> getByUserId(Long userId) {
    return orderRepository.findByUserId(userId);
  }

  // UPDATE
  public Order update(Long id, Order updated) {
    Order existing = getById(id);

    existing.setStatus(updated.getStatus());
    existing.setUserId(updated.getUserId());

    existing.getItems().clear();
    updated.getItems().forEach(oi -> {
      oi.setOrder(existing);
      existing.getItems().add(oi);
    });

    return orderRepository.save(existing);
  }

  public void delete(Long id) {
    orderRepository.deleteById(id);
  }
}