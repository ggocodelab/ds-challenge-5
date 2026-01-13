package com.ggocodelab.dscommerce.config;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {
	
	@Bean
	@Profile("dev")
	@Order(1)
	SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher(PathRequest.toH2Console()).csrf(csrf -> csrf.disable());
		http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		return http.build();
	}
}
