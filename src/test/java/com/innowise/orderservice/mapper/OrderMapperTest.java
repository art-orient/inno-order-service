package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.UserDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

  private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

  @Test
  void toOrderItems_success() {
    OrderItemDto dto = new OrderItemDto(5L, 3);
    List<OrderItem> items = mapper.toOrderItems(List.of(dto));
    assertEquals(1, items.size());
    OrderItem oi = items.get(0);
    assertNotNull(oi.getItem());
    assertEquals(5L, oi.getItem().getId());
    assertEquals(3, oi.getQuantity());
  }

  @Test
  void toOrderResponseDto_success() {
    UserDto user = new UserDto(10L, "mail@mail.com", "John", "Doe");
    Item item = new Item();
    item.setId(5L);
    item.setPrice(BigDecimal.valueOf(100));
    OrderItem orderItem = new OrderItem();
    orderItem.setItem(item);
    orderItem.setQuantity(2);

    Order order = new Order();
    order.setId(99L);
    order.setUserId(10L);
    order.setStatus(OrderStatus.CREATED);
    order.setTotalPrice(BigDecimal.valueOf(200));
    order.setItems(List.of(orderItem));

    OrderResponseDto dto = mapper.toOrderResponseDto(order, user);
    assertEquals(99L, dto.id());
    assertEquals(10L, dto.userId());
    assertEquals(OrderStatus.CREATED, dto.status());
    assertEquals(BigDecimal.valueOf(200), dto.totalPrice());
    assertEquals(1, dto.items().size());
    assertEquals(5L, dto.items().get(0).itemId());
    assertEquals(2, dto.items().get(0).quantity());
    assertEquals(user, dto.user());
  }
}
