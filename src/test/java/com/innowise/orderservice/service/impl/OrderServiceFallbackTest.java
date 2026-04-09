package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.model.dto.OrderCreateRequestDto;
import com.innowise.orderservice.model.dto.OrderUpdateRequestDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.innowise.orderservice.service.impl.OrderServiceImpl.USER_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceFallbackTest {

  private final OrderServiceImpl service =
          new OrderServiceImpl(null, null, null, null);
  private final Throwable cause = new RuntimeException("boom");

  @Test
  void createFallback_throwsException() {
    OrderCreateRequestDto dto = new OrderCreateRequestDto("mail@mail.com", List.of());
    OrderServiceException ex = assertThrows(
            OrderServiceException.class,
            () -> service.createFallback(dto, cause)
    );
    assertEquals(USER_SERVICE_UNAVAILABLE, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void getByIdFallback_throwsException() {
    Long id = 1L;
    OrderServiceException ex = assertThrows(
            OrderServiceException.class,
            () -> service.getByIdFallback(id, cause)
    );
    assertEquals(USER_SERVICE_UNAVAILABLE, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void getWithFilterFallback_throwsException() {
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime to = LocalDateTime.now();
    List<OrderStatus> statuses = List.of(OrderStatus.CREATED);
    Pageable pageable = Pageable.unpaged();
    OrderServiceException ex = assertThrows(
            OrderServiceException.class,
            () -> service.getWithFilterFallback(from, to, statuses, pageable, cause)
    );
    assertEquals(USER_SERVICE_UNAVAILABLE, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void getByUserIdFallback_throwsException() {
    Long userId = 10L;
    OrderServiceException ex = assertThrows(
            OrderServiceException.class,
            () -> service.getByUserIdFallback(userId, cause)
    );
    assertEquals(USER_SERVICE_UNAVAILABLE, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void updateFallback_throwsException() {
    Long id = 1L;
    OrderUpdateRequestDto dto =
            new OrderUpdateRequestDto(OrderStatus.PAID, List.of());
    OrderServiceException ex = assertThrows(
            OrderServiceException.class,
            () -> service.updateFallback(id, dto, cause)
    );
    assertEquals(USER_SERVICE_UNAVAILABLE, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }
}