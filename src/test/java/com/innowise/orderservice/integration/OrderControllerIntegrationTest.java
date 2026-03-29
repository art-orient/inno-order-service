package com.innowise.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.jayway.jsonpath.JsonPath;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

  private static final WireMockServer wireMock = new WireMockServer(
          WireMockConfiguration.options().dynamicPort()
  );

  @BeforeAll
  static void startWireMock() {
    wireMock.start();
  }

  @AfterAll
  static void stopWireMock() {
    wireMock.stop();
  }

  @Container
  static final PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                  .withDatabaseName("orders")
                  .withUsername("postgres")
                  .withPassword("postgres");

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("user-service.url", () -> "http://localhost:" + wireMock.port());
  }

  @BeforeAll
  static void checkDocker() {
    Assumptions.assumeTrue(
            DockerClientFactory.instance().isDockerAvailable(),
            "Skipping integration tests because Docker is not available"
    );
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  CircuitBreakerRegistry registry;

  @Autowired
  ItemRepository itemRepository;

  @Autowired
  OrderItemRepository orderItemRepository;

  @Autowired
  OrderRepository orderRepository;

  private long itemId;

  @BeforeEach
  void beforeEach() {
    registry.circuitBreaker("userService").transitionToClosedState();
    wireMock.resetAll();
    orderItemRepository.deleteAllInBatch();
    orderRepository.deleteAllInBatch();
    itemRepository.deleteAllInBatch();
    Item item = new Item();
    item.setName("Test item");
    item.setPrice(BigDecimal.TEN);
    itemId = itemRepository.save(item).getId();
  }

  private String createOrderJson() {
    return """
            {
              "userEmail": "test@mail.com",
              "items": [
                {"itemId": %d, "quantity": 1}
              ]
            }
            """.formatted(itemId);
  }

  private void stubGetByEmail() {
    wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/users/by-email"))
                    .withQueryParam("email", WireMock.equalTo("test@mail.com"))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                            {
                                              "id": 1,
                                              "email": "test@mail.com",
                                              "firstName": "Alex",
                                              "lastName": "Smith"
                                            }
                                            """)
                    )
    );
  }

  private void stubGetById(long id) {
    wireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/api/users/" + id))
                    .willReturn(
                            WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("""
                                            {
                                              "id": %d,
                                              "email": "test@mail.com",
                                              "firstName": "Alex",
                                              "lastName": "Smith"
                                            }
                                            """.formatted(id))
                    )
    );
  }

  private long extractId(String json) {
    return JsonPath.parse(json).read("$.id", Long.class);
  }

  @Test
  void createOrder_returns201() throws Exception {
    stubGetByEmail();
    mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createOrderJson()))
            .andExpect(status().isCreated());
  }

  @Test
  void getOrderById_returns200() throws Exception {
    stubGetByEmail();
    String response = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createOrderJson()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long orderId = extractId(response);
    stubGetById(1L);
    mockMvc.perform(get("/api/orders/" + orderId))
            .andExpect(status().isOk());
  }

  @Test
  void getOrdersWithFilter_returns200() throws Exception {
    stubGetByEmail();
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createOrderJson()));
    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createOrderJson()));
    stubGetById(1L);
    mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk());
  }

  @Test
  void updateOrder_returns200() throws Exception {
    stubGetByEmail();
    String response = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createOrderJson()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long orderId = extractId(response);
    stubGetById(1L);
    mockMvc.perform(put("/api/orders/" + orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "status": "PAID",
                              "items": [
                                {"itemId": %d, "quantity": 1}
                              ]
                            }
                            """.formatted(itemId)))
            .andExpect(status().isOk());
  }

  @Test
  void deleteOrder_returns204() throws Exception {
    stubGetByEmail();
    String response = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createOrderJson()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long orderId = extractId(response);
    stubGetById(1L);
    mockMvc.perform(delete("/api/orders/" + orderId))
            .andExpect(status().isNoContent());
  }
}