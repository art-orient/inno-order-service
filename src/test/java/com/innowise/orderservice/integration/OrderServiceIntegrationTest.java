package com.innowise.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.innowise.orderservice.dto.OrderCreateRequestDto;
import com.innowise.orderservice.dto.OrderItemDto;
import com.innowise.orderservice.dto.OrderResponseDto;
import com.innowise.orderservice.dto.OrderUpdateRequestDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.OrderStatus;
import com.innowise.orderservice.exception.OrderServiceException;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

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
  private OrderService orderService;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  private long itemId;

  @BeforeEach
  void setup() {
    wireMock.resetAll();

    orderItemRepository.deleteAllInBatch();
    orderRepository.deleteAllInBatch();
    itemRepository.deleteAllInBatch();

    Item item = new Item();
    item.setName("Test item");
    item.setPrice(BigDecimal.TEN);
    itemId = itemRepository.save(item).getId();
  }

  private void stubUserByEmail() {
    wireMock.stubFor(get(urlMatching("/api/users/by-email.*"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                          "id": 1,
                          "email": "test@mail.com",
                          "firstName": "Alex",
                          "lastName": "Smith"
                        }
                        """)));
  }

  private void stubUserById(long id) {
    wireMock.stubFor(get(urlEqualTo("/api/users/1"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                          "id": 1,
                          "email": "test@mail.com",
                          "firstName": "Alex",
                          "lastName": "Smith"
                        }
                        """)));
  }

  @Test
  void create_success() {
    stubUserByEmail();

    OrderCreateRequestDto dto =
            new OrderCreateRequestDto(
                    "test@mail.com",
                    List.of(new OrderItemDto(itemId, 1))
            );

    OrderResponseDto response = orderService.create(dto);

    assertNotNull(response);
    assertEquals("test@mail.com", response.user().email());
    assertEquals(1L, response.userId());
    assertEquals(OrderStatus.CREATED, response.status());
    assertEquals(0, response.totalPrice().compareTo(BigDecimal.TEN));
    assertNotNull(response.createdAt());
    assertNotNull(response.updatedAt());
  }

  // GET BY ID
  @Test
  void getById_success() {
    stubUserByEmail();

    OrderResponseDto created =
            orderService.create(new OrderCreateRequestDto(
                    "test@mail.com",
                    List.of(new OrderItemDto(itemId, 1))
            ));

    stubUserById(1L);

    OrderResponseDto found = orderService.getById(created.id());

    assertEquals(created.id(), found.id());
    assertEquals(1L, found.userId());
  }

  // GET WITH FILTER
  @Test
  void getWithFilter_success() {
    stubUserByEmail();

    orderService.create(new OrderCreateRequestDto(
            "test@mail.com",
            List.of(new OrderItemDto(itemId, 1))
    ));

    orderService.create(new OrderCreateRequestDto(
            "test@mail.com",
            List.of(new OrderItemDto(itemId, 1))
    ));

    stubUserById(1L);

    Page<OrderResponseDto> page = orderService.getWithFilter(
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            List.of(OrderStatus.CREATED),
            Pageable.unpaged()
    );

    assertEquals(2, page.getTotalElements());
  }

  // GET BY USER ID
  @Test
  void getByUserId_success() {
    stubUserByEmail();

    OrderResponseDto created =
            orderService.create(new OrderCreateRequestDto(
                    "test@mail.com",
                    List.of(new OrderItemDto(itemId, 1))
            ));

    stubUserById(1L);

    List<OrderResponseDto> list = orderService.getByUserId(1L);

    assertFalse(list.isEmpty());
    assertEquals(created.id(), list.get(0).id());
  }

  // UPDATE
  @Test
  void update_success() {
    stubUserByEmail();

    OrderResponseDto created =
            orderService.create(new OrderCreateRequestDto(
                    "test@mail.com",
                    List.of(new OrderItemDto(itemId, 1))
            ));

    stubUserById(1L);

    OrderUpdateRequestDto updateDto =
            new OrderUpdateRequestDto(
                    OrderStatus.PAID,
                    List.of(new OrderItemDto(itemId, 1))
            );

    OrderResponseDto updated = orderService.update(created.id(), updateDto);

    assertEquals(OrderStatus.PAID, updated.status());
    assertEquals(created.id(), updated.id());
  }

  // DELETE
  @Test
  void delete_success() {
    stubUserByEmail();

    OrderResponseDto created =
            orderService.create(new OrderCreateRequestDto(
                    "test@mail.com",
                    List.of(new OrderItemDto(itemId, 1))
            ));

    orderService.delete(created.id());

    assertThrows(OrderServiceException.class,
            () -> orderService.getById(created.id()));
  }
}