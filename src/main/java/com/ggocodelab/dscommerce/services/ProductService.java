package com.ggocodelab.dscommerce.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ggocodelab.dscommerce.dtos.ProductDTO;
import com.ggocodelab.dscommerce.dtos.ProductMinDTO;
import com.ggocodelab.dscommerce.entities.Product;
import com.ggocodelab.dscommerce.exceptions.ResourceNotFoundException;
import com.ggocodelab.dscommerce.repositories.ProductRepository;

@Service
public class ProductService {
	
	@Autowired
	public ProductRepository repository;
	
	@Transactional(readOnly = true)
	public Page<ProductMinDTO> findAll(String name, Pageable pageable) {
		Page<Product> result = repository.findByProductName(name, pageable);
		return result.map(x -> new ProductMinDTO(x));
	}
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Product product = repository.findById(id)
				.orElseThrow (() -> new ResourceNotFoundException("Recurso não encontrado"));
		return new ProductDTO(product);		
	}
	
	

}
