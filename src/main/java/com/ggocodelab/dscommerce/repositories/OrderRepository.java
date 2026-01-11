package com.ggocodelab.dscommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ggocodelab.dscommerce.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}
