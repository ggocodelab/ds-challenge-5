package com.ggocodelab.dscommerce.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ggocodelab.dscommerce.dtos.UserDTO;
import com.ggocodelab.dscommerce.entities.Role;
import com.ggocodelab.dscommerce.entities.User;
import com.ggocodelab.dscommerce.projections.UserDetailsProjection;
import com.ggocodelab.dscommerce.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService{
	
	@Autowired
	private UserRepository repository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
		if(result.size() == 0) {
			throw new UsernameNotFoundException("Usuário não encontrado.");
		}
		User user = new User();
		user.setEmail(username);
		user.setPassword(result.get(0).getPassword());
		
		for (UserDetailsProjection projection : result) {
			user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
		}
		return user;
	}
	
	protected User authenticated() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
			String username = jwtPrincipal.getClaim("username");
			return repository.findByEmail(username).get();
		}
		catch (Exception e) {
			throw new UsernameNotFoundException("Invalid user");
		}
	}
	
	@Transactional(readOnly = true)
	public UserDTO getMe() {
		User entity = authenticated();
		return new UserDTO(entity);
	}

}
