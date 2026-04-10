package com.innowise.orderservice.dao;

import com.innowise.orderservice.model.entity.Item;
import java.util.Optional;

/**
 * Data Access Object for {@link Item}.
 * Provides access to item-related persistence operations.
 */
public interface ItemDao {

  /**
   * Retrieves an item by its identifier.
   *
   * @param id the item identifier
   * @return optional containing the item if found
   */
  Optional<Item> findById(Long id);
}
