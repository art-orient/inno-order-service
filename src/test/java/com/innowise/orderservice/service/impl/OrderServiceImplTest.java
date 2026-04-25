package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.dao.ItemDao;
import com.innowise.orderservice.dao.OrderDao;
import com.innowise.orderservice.dao.PaymentEventDto;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderCreateRequestDto;
import com.innowise.orderservice.model.dto.OrderItemDto;
import com.innowise.orderservice.model.dto.OrderItemResponseDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateRequestDto;
import com.innowise.orderservice.model.dto.UserDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
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
  private OrderDao orderDao;

  @Mock
  private ItemDao itemDao;

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

    OrderItem mappedItem = new OrderItem();
    Item stubItem = new Item();
    stubItem.setId(1L);
    mappedItem.setItem(stubItem);
    mappedItem.setQuantity(2);
    when(orderMapper.toOrderItems(dto.items()))
            .thenReturn(List.of(mappedItem));

    Item item = new Item();
    item.setId(1L);
    item.setName("Test item");
    item.setPrice(BigDecimal.valueOf(100));
    when(itemDao.findById(1L)).thenReturn(Optional.of(item));

    Order savedOrder = new Order();
    savedOrder.setId(99L);
    savedOrder.setUserId(10L);
    savedOrder.setStatus(OrderStatus.CREATED);
    savedOrder.setItems(List.of());
    savedOrder.setTotalPrice(BigDecimal.valueOf(200));
    when(orderDao.save(any(Order.class))).thenReturn(savedOrder);

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
    verify(orderDao).save(any(Order.class));
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
    when(itemDao.findById(5L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> orderService.create(dto));
  }

  @Test
  void getById_success() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    when(orderDao.findById(1L)).thenReturn(Optional.of(order));
    UserDto user = new UserDto(10L, "mail", "John", "Doe");
    when(userClient.getById(10L)).thenReturn(user);
    OrderResponseDto dto = mock(OrderResponseDto.class);
    when(orderMapper.toOrderResponseDto(order, user)).thenReturn(dto);
    OrderResponseDto result = orderService.getById(1L);
    assertNotNull(result);
    verify(orderDao).findById(1L);
    verify(userClient).getById(10L);
  }

  @Test
  void getById_notFound_throwsException() {
    when(orderDao.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> orderService.getById(1L));
  }

  @Test
  void getByUserId_success() {
    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    when(orderDao.findByUserId(10L)).thenReturn(List.of(order));
    UserDto user = new UserDto(10L, "mail", "John", "Doe");
    when(userClient.getById(10L)).thenReturn(user);
    OrderResponseDto dto = mock(OrderResponseDto.class);
    when(orderMapper.toOrderResponseDto(order, user)).thenReturn(dto);
    List<OrderResponseDto> result = orderService.getByUserId(10L);
    assertEquals(1, result.size());
    verify(orderDao).findByUserId(10L);
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
    when(orderDao.findAll(
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

    verify(orderDao).findAll(ArgumentMatchers.<Specification<Order>>any(), eq(pageable));
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
    when(orderDao.findById(1L)).thenReturn(Optional.of(order));
    OrderUpdateRequestDto dto = new OrderUpdateRequestDto(OrderStatus.PAID,
            List.of(new OrderItemDto(1L, 3)));
    Item item = new Item();
    item.setId(1L);
    item.setPrice(BigDecimal.valueOf(50));
    when(itemDao.findById(1L)).thenReturn(Optional.of(item));
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
    when(orderDao.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> orderService.update(1L, mock(OrderUpdateRequestDto.class)));
  }

  @Test
  void delete_success() {
    Order order = new Order();
    when(orderDao.findById(1L)).thenReturn(Optional.of(order));
    orderService.delete(1L);
    verify(orderDao).delete(order);
  }

  @Test
  void delete_notFound_throwsException() {
    when(orderDao.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> orderService.delete(1L));
  }

  @Test
  void updateOrderStatus_successToPaid() {
    PaymentEventDto event = new PaymentEventDto("p-1", 1L, 10L,
            "SUCCESS", BigDecimal.TEN);
    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.CREATED);
    when(orderDao.findById(1L)).thenReturn(Optional.of(order));
    orderService.updateOrderStatus(event);
    assertEquals(OrderStatus.PAID, order.getStatus());
    verify(orderDao).save(order);
  }

  @Test
  void updateOrderStatus_failedToFailedPayment() {
    PaymentEventDto event = new PaymentEventDto("p-1", 1L, 10L,
            "FAILED", BigDecimal.TEN);
    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.CREATED);
    when(orderDao.findById(1L)).thenReturn(Optional.of(order));
    orderService.updateOrderStatus(event);
    assertEquals(OrderStatus.FAILED_PAYMENT, order.getStatus());
    verify(orderDao).save(order);
  }

  @Test
  void updateOrderStatus_orderNotFound_throwsException() {
    PaymentEventDto event = new PaymentEventDto("p-1", 1L, 10L,
            "SUCCESS", BigDecimal.TEN);
    when(orderDao.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> orderService.updateOrderStatus(event));
  }
}
