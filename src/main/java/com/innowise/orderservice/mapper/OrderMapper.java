package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.OrderItemDto;
import com.innowise.orderservice.dto.OrderItemResponseDto;
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

  @Mapping(target = "id", source = "order.id")
  @Mapping(target = "userId", source = "order.userId")
  @Mapping(target = "status", source = "order.status")
  @Mapping(target = "totalPrice", source = "order.totalPrice")
  @Mapping(target = "createdAt", source = "order.createdAt")
  @Mapping(target = "updatedAt", source = "order.updatedAt")
  @Mapping(target = "items", source = "order.items")
  @Mapping(target = "user", source = "user")
  OrderResponseDto toOrderResponseDto(Order order, UserDto user);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "itemId", source = "item.id")
  @Mapping(target = "itemName", source = "item.name")
  @Mapping(target = "itemPrice", source = "item.price")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  OrderItemResponseDto toOrderItemResponseDto(OrderItem entity);

  List<OrderItemResponseDto> toOrderItemResponseDtos(List<OrderItem> entities);
}