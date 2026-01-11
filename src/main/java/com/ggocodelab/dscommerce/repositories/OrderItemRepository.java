package com.ggocodelab.dscommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ggocodelab.dscommerce.entities.OrderItem;
import com.ggocodelab.dscommerce.entities.OrderItemPK;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemPK> {

}
