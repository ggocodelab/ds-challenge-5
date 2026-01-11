package com.ggocodelab.dscommerce.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ggocodelab.dscommerce.dtos.OrderDTO;
import com.ggocodelab.dscommerce.dtos.OrderItemDTO;
import com.ggocodelab.dscommerce.entities.Order;
import com.ggocodelab.dscommerce.entities.OrderItem;
import com.ggocodelab.dscommerce.entities.OrderStatus;
import com.ggocodelab.dscommerce.entities.Product;
import com.ggocodelab.dscommerce.entities.User;
import com.ggocodelab.dscommerce.exceptions.ResourceNotFoundException;
import com.ggocodelab.dscommerce.repositories.OrderItemRepository;
import com.ggocodelab.dscommerce.repositories.OrderRepository;
import com.ggocodelab.dscommerce.repositories.ProductRepository;

@Service
public class OrderService {
	
	@Autowired
	private OrderRepository repository;
	    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;

    @Transactional(readOnly = true)
    public OrderDTO findById(Long id) {
        Order order = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado"));
        authService.validateSelfOrAdmin(order.getClient().getId());
        return new OrderDTO(order);
    }

    @Transactional
	public OrderDTO insert(OrderDTO dto) {
		
    	Order order = new Order();
    	
    	order.setMoment(Instant.now());
    	order.setStatus(OrderStatus.WAITING_PAYMENT);
    	
    	User user = userService.authenticated();
    	order.setClient(user);
    	
    	for (OrderItemDTO itemDto : dto.getItems()) {
    		Product product = productRepository.getReferenceById(itemDto.getProductId());
    		OrderItem item = new OrderItem(order, product, itemDto.getQuantity(), product.getPrice());
    		order.getItems().add(item);
    	}
    	
    	repository.save(order);
    	orderItemRepository.saveAll(order.getItems());
    	
    	return new OrderDTO(order);
	}
}
