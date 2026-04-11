package com.innowise.orderservice.repository;

import com.innowise.orderservice.dao.ItemDao;
import com.innowise.orderservice.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemDao {
}
