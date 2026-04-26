package com.innowise.orderservice.kafka;

import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class KafkaConsumer {

  private final OrderService orderService;

  @KafkaListener(topics = "CREATE_PAYMENT", groupId = "order-service")
  public void handlePaymentEvent(PaymentEvent event) {
    log.info("Received payment event: {}", event);
    orderService.updateOrderStatus(event);
  }
}
