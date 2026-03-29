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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
    OrderCreateRequestDto dto = new OrderCreateRequestDto(
            "test@mail.com",
            List.of(new OrderItemDto(1L, 2))
    );
    UserDto user = new UserDto(10L, "test@mail.com", "John", "Doe");
    when(userClient.getByEmail("test@mail.com")).thenReturn(user);
    Item item = new Item();
    item.setId(1L);
    item.setName("Test item");
    item.setPrice(BigDecimal.valueOf(100));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
    Order savedOrder = new Order();
    savedOrder.setId(99L);
    savedOrder.setUserId(10L);
    savedOrder.setStatus(OrderStatus.CREATED);
    savedOrder.setItems(List.of());
    savedOrder.setTotalPrice(BigDecimal.valueOf(200));
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
    OrderItemResponseDto itemResponse = new OrderItemResponseDto(
            1L,
            1L,
            "Test item",
            BigDecimal.valueOf(100),
            2,
            LocalDateTime.now(),
            LocalDateTime.now()
    );
    OrderResponseDto response = new OrderResponseDto(
            99L,
            10L,
            OrderStatus.CREATED,
            BigDecimal.valueOf(200),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of(itemResponse),
            user
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
  void getWithFilter_success() {
    LocalDateTime from = LocalDateTime.now().minusDays(1);
    LocalDateTime to = LocalDateTime.now();
    List<OrderStatus> statuses = List.of(OrderStatus.CREATED);
    Pageable pageable = mock(Pageable.class);
    Order order1 = new Order();
    order1.setId(1L);
    order1.setUserId(10L);
    order1.setStatus(OrderStatus.CREATED);
    Order order2 = new Order();
    order2.setId(2L);
    order2.setUserId(20L);
    order2.setStatus(OrderStatus.CREATED);

    Page<Order> page = new PageImpl<>(List.of(order1, order2));
    when(orderRepository.findAll(
            ArgumentMatchers.<Specification<Order>>any(),
            eq(pageable)
    )).thenReturn(page);
    UserDto user10 = new UserDto(10L, "u10@mail", "John", "Doe");
    UserDto user20 = new UserDto(20L, "u20@mail", "Jane", "Smith");
    when(userClient.getById(10L)).thenReturn(user10);
    when(userClient.getById(20L)).thenReturn(user20);

    OrderResponseDto dto1 = mock(OrderResponseDto.class);
    OrderResponseDto dto2 = mock(OrderResponseDto.class);
    when(orderMapper.toOrderResponseDto(order1, user10)).thenReturn(dto1);
    when(orderMapper.toOrderResponseDto(order2, user20)).thenReturn(dto2);
    Page<OrderResponseDto> result = orderService.getWithFilter(from, to, statuses, pageable);
    assertEquals(2, result.getContent().size());
    assertTrue(result.getContent().contains(dto1));
    assertTrue(result.getContent().contains(dto2));

    verify(orderRepository).findAll(ArgumentMatchers.<Specification<Order>>any(), eq(pageable));
    verify(userClient).getById(10L);
    verify(userClient).getById(20L);
    verify(orderMapper).toOrderResponseDto(order1, user10);
    verify(orderMapper).toOrderResponseDto(order2, user20);
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
