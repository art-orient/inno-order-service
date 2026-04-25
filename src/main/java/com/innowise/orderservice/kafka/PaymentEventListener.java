package com.innowise.orderservice.kafka;

import com.innowise.orderservice.dao.PaymentEventDto;
import com.innowise.orderservice.service.OrderService;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class PaymentEventListener {

  private final OrderService orderService;

  public PaymentEventListener(OrderService orderService) {
    this.orderService = orderService;
  }

  @KafkaListener(topics = "payment-events", groupId = "order-service")
  public void handlePaymentEvent(PaymentEventDto event) {
    orderService.updateOrderStatus(event);
  }
}
