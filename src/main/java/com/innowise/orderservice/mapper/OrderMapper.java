package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.UserDto;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "order", ignore = true)
  @Mapping(target = "item.id", source = "itemId")
  OrderItem toOrderItem(OrderItemDto dto);

  List<OrderItem> toOrderItems(List<OrderItemDto> dtos);

  OrderResponseDto toOrderResponseDto(Order order, UserDto user);

  @ObjectFactory
  default OrderResponseDto createOrderResponseDto(Order order, UserDto user) {
    return new OrderResponseDto(
            order.getId(),
            order.getUserId(),
            order.getStatus(),
            order.getTotalPrice(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            toOrderItemDtos(order.getItems()),
            user
    );
  }

  @Mapping(target = "itemId", source = "item.id")
  OrderItemDto toOrderItemDto(OrderItem entity);

  List<OrderItemDto> toOrderItemDtos(List<OrderItem> entities);
}