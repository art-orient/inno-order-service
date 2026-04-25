package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.dao.ItemDao;
import com.innowise.orderservice.dao.OrderDao;
import com.innowise.orderservice.dao.PaymentEventDto;
import com.innowise.orderservice.exception.ExternalServiceUnavailableException;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.model.dto.OrderCreateRequestDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateRequestDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.specification.OrderSpecification;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
class OrderServiceImpl implements OrderService {

  public static final String ORDER_NOT_FOUND = "Order not found: ";
  public static final String ITEM_NOT_FOUND = "Item not found: ";
  public static final String USER_SERVICE_UNAVAILABLE = "User Service unavailable";
  private final OrderDao orderDao;
  private final ItemDao itemDao;
  private final OrderMapper orderMapper;
  private final UserClient userClient;

  @Override
  @Transactional
  @CircuitBreaker(name = "userService", fallbackMethod = "createFallback")
  public OrderResponseDto create(OrderCreateRequestDto dto) {
    UserDto user = userClient.getByEmail(dto.userEmail());
    Order order = new Order();
    order.setUserId(user.id());
    order.setStatus(OrderStatus.CREATED);
    order.setDeleted(false);
    List<OrderItem> items = orderMapper.toOrderItems(dto.items());
    resolveOrderItems(items, order);
    BigDecimal total = calculateTotalPrice(items);
    order.setTotalPrice(total);
    order.setItems(items);
    Order saved = orderDao.save(order);
    return orderMapper.toOrderResponseDto(saved, user);
  }

  public OrderResponseDto createFallback(OrderCreateRequestDto dto, Throwable ex) {
    throw new ExternalServiceUnavailableException(USER_SERVICE_UNAVAILABLE, ex);
  }

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "getByIdFallback")
  public OrderResponseDto getById(Long id) {
    Order order = orderDao.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND + id));
    UserDto user = userClient.getById(order.getUserId());
    return orderMapper.toOrderResponseDto(order, user);
  }

  public OrderResponseDto getByIdFallback(Long id, Throwable ex) {
    throw new ExternalServiceUnavailableException(USER_SERVICE_UNAVAILABLE, ex);
  }

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "getWithFilterFallback")
  public Page<OrderResponseDto> getWithFilter(LocalDateTime from,
                                              LocalDateTime to,
                                              List<OrderStatus> statuses,
                                              Pageable pageable) {
    Specification<Order> spec = Specification.allOf(
            OrderSpecification.createdBetween(from, to),
            OrderSpecification.hasStatus(statuses)
    );
    Page<Order> page = orderDao.findAll(spec, pageable);
    return page.map(order -> {
      UserDto user = userClient.getById(order.getUserId());
      return orderMapper.toOrderResponseDto(order, user);
    });
  }

  public Page<OrderResponseDto> getWithFilterFallback(
          LocalDateTime from,
          LocalDateTime to,
          List<OrderStatus> statuses,
          Pageable pageable,
          Throwable ex
  ) {
    throw new ExternalServiceUnavailableException(USER_SERVICE_UNAVAILABLE, ex);
  }

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "getByUserIdFallback")
  public List<OrderResponseDto> getByUserId(Long userId) {
    List<Order> orders = orderDao.findByUserId(userId);
    UserDto user = userClient.getById(userId);
    return orders.stream()
            .map(order -> orderMapper.toOrderResponseDto(order, user))
            .toList();
  }

  public List<OrderResponseDto> getByUserIdFallback(Long userId, Throwable ex) {
    throw new ExternalServiceUnavailableException(USER_SERVICE_UNAVAILABLE, ex);
  }

  @Override
  @Transactional
  @CircuitBreaker(name = "userService", fallbackMethod = "updateFallback")
  public OrderResponseDto update(Long id, OrderUpdateRequestDto dto) {
    Order order = orderDao.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND + id));
    order.setStatus(dto.status());
    order.getItems().clear();
    List<OrderItem> newItems = orderMapper.toOrderItems(dto.items());
    resolveOrderItems(newItems, order);
    order.getItems().addAll(newItems);
    BigDecimal total = calculateTotalPrice(order.getItems());
    order.setTotalPrice(total);
    UserDto user = userClient.getById(order.getUserId());
    return orderMapper.toOrderResponseDto(order, user);
  }

  public OrderResponseDto updateFallback(Long id, OrderUpdateRequestDto dto, Throwable ex) {
    throw new ExternalServiceUnavailableException(USER_SERVICE_UNAVAILABLE, ex);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Order order = orderDao.findById(id)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND + id));
    orderDao.delete(order);
  }

  public void updateOrderStatus(PaymentEventDto event) {
    Order order = orderDao.findById(event.orderId())
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND + event.orderId()));
    if ("SUCCESS".equals(event.status())) {
      order.setStatus(OrderStatus.PAID);
    } else {
      order.setStatus(OrderStatus.FAILED_PAYMENT);
    }
    orderDao.save(order);
  }

  private BigDecimal calculateTotalPrice(List<OrderItem> items) {
    return items.stream()
            .map(oi -> oi.getItem().getPrice()
                    .multiply(BigDecimal.valueOf(oi.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<OrderItem> resolveOrderItems(List<OrderItem> items, Order order) {
    items.forEach(oi -> {
      Long itemId = oi.getItem().getId();
      Item item = itemDao.findById(itemId)
              .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));
      oi.setItem(item);
      oi.setOrder(order);
    });
    return items;
  }
}
