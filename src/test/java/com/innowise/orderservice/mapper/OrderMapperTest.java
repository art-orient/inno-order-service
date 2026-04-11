package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderItemDto;
import com.innowise.orderservice.model.dto.OrderItemResponseDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

  private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

  // ---------------------------------------------------------
  // 1. toOrderItem (DTO → Entity)
  // ---------------------------------------------------------
  @Test
  void toOrderItem_success() {
    OrderItemDto dto = new OrderItemDto(5L, 3);

    OrderItem entity = mapper.toOrderItem(dto);

    assertNull(entity.getId());
    assertNull(entity.getOrder());
    assertNotNull(entity.getItem());
    assertEquals(5L, entity.getItem().getId());
    assertEquals(3, entity.getQuantity());
  }

  // ---------------------------------------------------------
  // 2. toOrderItems (List<DTO> → List<Entity>)
  // ---------------------------------------------------------
  @Test
  void toOrderItems_success() {
    OrderItemDto dto = new OrderItemDto(5L, 3);

    List<OrderItem> items = mapper.toOrderItems(List.of(dto));

    assertEquals(1, items.size());
    OrderItem oi = items.get(0);
    assertEquals(5L, oi.getItem().getId());
    assertEquals(3, oi.getQuantity());
  }

  // ---------------------------------------------------------
  // 3. toOrderItemResponseDto (Entity → DTO)
  // ---------------------------------------------------------
  @Test
  void toOrderItemResponseDto_success() {
    Item item = new Item();
    item.setId(5L);
    item.setName("Test item");
    item.setPrice(BigDecimal.valueOf(100));

    OrderItem orderItem = new OrderItem();
    orderItem.setId(7L);
    orderItem.setItem(item);
    orderItem.setQuantity(2);

    OrderItemResponseDto dto = mapper.toOrderItemResponseDto(orderItem);

    assertEquals(7L, dto.id());
    assertEquals(5L, dto.itemId());
    assertEquals("Test item", dto.itemName());
    assertEquals(BigDecimal.valueOf(100), dto.itemPrice());
    assertEquals(2, dto.quantity());
  }

  // ---------------------------------------------------------
  // 4. toOrderItemResponseDtos (List<Entity> → List<DTO>)
  // ---------------------------------------------------------
  @Test
  void toOrderItemResponseDtos_success() {
    Item item = new Item();
    item.setId(5L);
    item.setName("Item");
    item.setPrice(BigDecimal.TEN);

    OrderItem oi = new OrderItem();
    oi.setId(1L);
    oi.setItem(item);
    oi.setQuantity(4);

    List<OrderItemResponseDto> list = mapper.toOrderItemResponseDtos(List.of(oi));

    assertEquals(1, list.size());
    assertEquals(1L, list.get(0).id());
    assertEquals(5L, list.get(0).itemId());
    assertEquals(4, list.get(0).quantity());
  }

  // ---------------------------------------------------------
  // 5. toOrderResponseDto (Order + User → DTO)
  // ---------------------------------------------------------
  @Test
  void toOrderResponseDto_success() {
    UserDto user = new UserDto(10L, "mail@mail.com", "John", "Doe");

    Item item = new Item();
    item.setId(5L);
    item.setName("Test item");
    item.setPrice(BigDecimal.valueOf(100));

    OrderItem orderItem = new OrderItem();
    orderItem.setId(7L);
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
    assertEquals(user, dto.user());

    assertEquals(1, dto.items().size());
    OrderItemResponseDto itemDto = dto.items().get(0);

    assertEquals(7L, itemDto.id());
    assertEquals(5L, itemDto.itemId());
    assertEquals("Test item", itemDto.itemName());
    assertEquals(BigDecimal.valueOf(100), itemDto.itemPrice());
    assertEquals(2, itemDto.quantity());
  }
}