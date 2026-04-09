package com.innowise.orderservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(
        name = "order-with-items",
        attributeNodes = {
                @NamedAttributeNode(value = "items", subgraph = "items-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "items-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("item")
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Order extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(name = "total_price", nullable = false)
  private BigDecimal totalPrice;

  @Column(nullable = false)
  private boolean deleted = false;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();
}
