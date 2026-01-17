package com.ggocodelab.dscommerce.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {
	
//	Injeta as origens permitidas para CORS a partir do application.properties
	@Value("${cors.origins}")
	private String corsOrigins;
	
	@Bean
	@Profile("dev")
	@Order(1)
	SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher(PathRequest.toH2Console()).csrf(csrf -> csrf.disable());
		http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		return http.build();
	}
	
	@Bean
	@Order(3)
	SecurityFilterChain rsSecurityFilterChain(HttpSecurity http) throws Exception {
		
//		Desabilita CSRF para APIs REST stateless
		http.csrf(csrf -> csrf.disable());
		
//		Define a política de autorização das requisições do Resource Server
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		
//		Habilita o Resource Server OAuth2 com validação de JWT
		http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		
//		Aplica a configuração de CORS para chamadas cross-origin
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		
		return http.build();		
	}
	
	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		
//		Converte a claim "authorities" do JWT em GrantedAuthority
//		Remove o prefixo padrão "SCOPE_" para compatibilidade com roles customizadas
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		grantedAuthoritiesConverter.setAuthorityPrefix("");
		
//		Associa o conversor customizado ao JwtAuthenticationConverter
//		Permitindo que o SecurityContext reconheça corretamente as permissões
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}
	
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		
//		Converte a propriedade de origens permitidas em uma lista
		String[] origins = corsOrigins.split(",");
		
//		Define a política de CORS da aplicação
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOriginPatterns(Arrays.asList(origins));
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		
//		Aplica a configuração de CORS para todas as rotas da aplicação
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);		
		return source;		
	}
	
	@Bean
	FilterRegistrationBean<CorsFilter> filterRegistrationBeanCorsFilter() {
		
//		Registra o filtro de CORS com a maior precedência
//		Garante que as regras de CORS sejam aplicadas antes do Spring Security
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
				new CorsFilter(corsConfigurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}
	
}
