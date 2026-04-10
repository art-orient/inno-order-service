package com.innowise.orderservice.repository;

import com.innowise.orderservice.dao.OrderDao;
import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>, OrderDao {

  @EntityGraph(value = "order-with-items", type = EntityGraph.EntityGraphType.FETCH)
  Optional<Order> findById(Long id);

  @EntityGraph(value = "order-with-items", type = EntityGraph.EntityGraphType.FETCH)
  List<Order> findByUserId(Long userId);

  @Override
  @EntityGraph(value = "order-with-items", type = EntityGraph.EntityGraphType.FETCH)
  Page<Order> findAll(Specification<Order> spec, Pageable pageable);
}
