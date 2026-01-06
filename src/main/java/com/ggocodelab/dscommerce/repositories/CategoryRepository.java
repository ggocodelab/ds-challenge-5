package com.ggocodelab.dscommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ggocodelab.dscommerce.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{

}
