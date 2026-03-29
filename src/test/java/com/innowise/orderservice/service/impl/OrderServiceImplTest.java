package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.entity.*;
import com.innowise.orderservice.exception.OrderServiceException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private OrderMapper orderMapper;

  @Mock
  private UserClient userClient;

  @InjectMocks
  private OrderServiceImpl orderService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void create_success() {
    OrderCreateRequestDto dto = new OrderCreateRequestDto("test@mail.com",
            List.of(new OrderItemDto(1L, 2)));
    UserDto user = new UserDto(10L, "test@mail.com", "John", "Doe");
    when(userClient.getByEmail("test@mail.com")).thenReturn(user);
    Item item = new Item();
    item.setId(1L);
    item.setPrice(BigDecimal.valueOf(100));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    Order savedOrder = new Order();
    savedOrder.setId(99L);
    savedOrder.setUserId(10L);
    savedOrder.setStatus(OrderStatus.CREATED);
    savedOrder.setItems(List.of());
    savedOrder.setTotalPrice(BigDecimal.valueOf(200));
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    OrderResponseDto response = new OrderResponseDto(
            99L, 10L, OrderStatus.CREATED, BigDecimal.valueOf(200),
            LocalDateTime.now(), LocalDateTime.now(),
            List.of(new OrderItemDto(1L, 2)), user
    );
    when(orderMapper.toOrderResponseDto(savedOrder, user)).thenReturn(response);
    OrderResponseDto result = orderService.create(dto);
    assertEquals(99L, result.id());
    assertEquals(10L, result.userId());
    assertEquals(BigDecimal.valueOf(200), result.totalPrice());
    verify(orderRepository).save(any(Order.class));
    verify(userClient).getByEmail("test@mail.com");
  }

  @Test
  void create_itemNotFound_throwsException() {
    OrderCreateRequestDto dto = new OrderCreateRequestDto("test@mail.com",
            List.of(new OrderItemDto(5L, 1)));
    when(userClient.getByEmail(anyString()))
            .thenReturn(new UserDto(10L, "test@mail.com", "John", "Doe"));
    OrderItem oi = new OrderItem();
    Item stubItem = new Item();
    stubItem.setId(5L);
    oi.setItem(stubItem);
    oi.setQuantity(1);
    when(orderMapper.toOrderItems(dto.items())).thenReturn(List.of(oi));
    when(itemRepository.findById(5L)).thenReturn(Optional.empty());
    assertThrows(OrderServiceException.class, () -> orderService.create(dto));
  }

  @Test
  void getById_success() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    UserDto user = new UserDto(10L, "mail", "John", "Doe");
    when(userClient.getById(10L)).thenReturn(user);
    OrderResponseDto dto = mock(OrderResponseDto.class);
    when(orderMapper.toOrderResponseDto(order, user)).thenReturn(dto);
    OrderResponseDto result = orderService.getById(1L);
    assertNotNull(result);
    verify(orderRepository).findById(1L);
    verify(userClient).getById(10L);
  }

  @Test
  void getById_notFound_throwsException() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(OrderServiceException.class, () -> orderService.getById(1L));
  }

  @Test
  void getByUserId_success() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    when(orderRepository.findByUserId(10L)).thenReturn(List.of(order));
    UserDto user = new UserDto(10L, "mail", "John", "Doe");
    when(userClient.getById(10L)).thenReturn(user);
    OrderResponseDto dto = mock(OrderResponseDto.class);
    when(orderMapper.toOrderResponseDto(order, user)).thenReturn(dto);
    List<OrderResponseDto> result = orderService.getByUserId(10L);
    assertEquals(1, result.size());
    verify(orderRepository).findByUserId(10L);
  }

  @Test
  void update_success() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    order.setItems(new ArrayList<>());
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    OrderUpdateRequestDto dto = new OrderUpdateRequestDto(OrderStatus.PAID,
            List.of(new OrderItemDto(1L, 3)));
    Item item = new Item();
    item.setId(1L);
    item.setPrice(BigDecimal.valueOf(50));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
    OrderItem mappedItem = new OrderItem();
    mappedItem.setItem(item);
    mappedItem.setQuantity(3);
    when(orderMapper.toOrderItems(dto.items())).thenReturn(List.of(mappedItem));

    UserDto user = new UserDto(10L, "mail", "John", "Doe");
    when(userClient.getById(10L)).thenReturn(user);
    OrderResponseDto response = mock(OrderResponseDto.class);
    when(orderMapper.toOrderResponseDto(order, user)).thenReturn(response);
    OrderResponseDto result = orderService.update(1L, dto);
    assertNotNull(result);
    assertEquals(OrderStatus.PAID, order.getStatus());
    assertEquals(BigDecimal.valueOf(150), order.getTotalPrice());
  }

  @Test
  void update_orderNotFound_throwsException() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(OrderServiceException.class, () -> orderService.update(1L, mock(OrderUpdateRequestDto.class)));
  }

  @Test
  void delete_success() {
    Order order = new Order();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    orderService.delete(1L);
    verify(orderRepository).delete(order);
  }

  @Test
  void delete_notFound_throwsException() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(OrderServiceException.class, () -> orderService.delete(1L));
  }
}
