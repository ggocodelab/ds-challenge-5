package com.ggocodelab.dscommerce.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;

import com.ggocodelab.dscommerce.config.customgrant.CustomPasswordAuthenticationConverter;
import com.ggocodelab.dscommerce.config.customgrant.CustomPasswordAuthenticationProvider;
import com.ggocodelab.dscommerce.config.customgrant.CustomUserAuthorities;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {
	
	@Value("${security.client-id}")
	private String clientId;

	@Value("${security.client-secret}")
	private String clientSecret;
	
	@Value("${security.jwt.duration}")
	private Integer jwtDurationSeconds;
	
	@Bean
	@Order(2)
	SecurityFilterChain asSecurityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
		
		// Cria explicitamente o configurer do Authorization Server
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
	            new OAuth2AuthorizationServerConfigurer();
		
		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher());
		
		http.with(authorizationServerConfigurer, Customizer.withDefaults());
		
		// Customização do endpoint /oauth2/token
	    authorizationServerConfigurer
	        .tokenEndpoint(tokenEndpoint -> tokenEndpoint
	        	.accessTokenRequestConverter(new CustomPasswordAuthenticationConverter())
	        	.authenticationProvider(new CustomPasswordAuthenticationProvider(
	            		 authorizationService(),
	                     tokenGenerator(),
	                     userDetailsService,
	                     passwordEncoder())	             
	            		 )
	         );
	    
	    http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

	    return http.build();
		
	  }
	
	//9 Persiste as autorizacoes emitidas
	@Bean
	OAuth2AuthorizationService authorizationService() {
		return new InMemoryOAuth2AuthorizationService();
	}
	
	// 10 Consentimentos (opcional no password grant)
	@Bean
	OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService() {
		return new InMemoryOAuth2AuthorizationConsentService();
	}
	
	// 6.Faz a encriptacao da senha
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	//7. Define clientId, secret, scopes e grant
	@Bean
	RegisteredClientRepository registeredClientRepository() {
		
		RegisteredClient registeredClient = RegisteredClient
			.withId(UUID.randomUUID().toString())
			.clientId(clientId)
			.clientSecret(passwordEncoder().encode(clientSecret))
			.scope("read")
			.scope("write")
			.authorizationGrantType(new AuthorizationGrantType("password"))
			.tokenSettings(tokenSettings())
			.clientSettings(clientSettings())
			.build();

		return new InMemoryRegisteredClientRepository(registeredClient);
	}
	
	// 5. Define duracao e formato do token
	@Bean
	TokenSettings tokenSettings() {
		return TokenSettings.builder()
			.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
			.accessTokenTimeToLive(Duration.ofSeconds(jwtDurationSeconds))
			.build();
	}
	
	// 7. Complementa o registeredClientRepository()
	@Bean
	ClientSettings clientSettings() {
		return ClientSettings.builder().build();
	}
	
	//8. endpoints padrão do AS
	@Bean
	AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}
	
	//11 Orquestra a geracao do access token
	@Bean
	OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
		NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource());
		JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
		jwtGenerator.setJwtCustomizer(tokenCustomizer());
		OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
		return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator);
	}
	// 12 Add roles dos usuarios ao JWT
	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			OAuth2ClientAuthenticationToken principal = context.getPrincipal();
			CustomUserAuthorities user = (CustomUserAuthorities) principal.getDetails();
			List<String> authorities = user.getAuthorities().stream().map(x -> x.getAuthority()).toList();
			if (context.getTokenType().getValue().equals("access_token")) {
	
				context.getClaims()
					.claim("authorities", authorities)
					.claim("username", user.getUsername());

			}
		};
	}
	
	//4.Permite validacao dos tokens emitidos
	@Bean
	JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}
	
	//3. Expoe as chaves para o Authorization Server
	@Bean
	JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}
	
	// 2. Encapsula as chaves em um RSAKey
	private static RSAKey generateRsa() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
	}
	
	// 1. Gera o par de chaves RSA
	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}	

}
