package com.ggocodelab.dscommerce.config.customgrant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public class CustomPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken{
	
	private static final long serialVersionUID = 1L;
	
//	Credenciais do usuário final extraídas da requisição OAuth2
	private final String username;
	private final String password;
	private final Set<String> scopes;
	
//	Necessário para a recuperação do token na CustomPasswordAuthenticationConverter
	public CustomPasswordAuthenticationToken(
	        Authentication clientPrincipal,
	        @Nullable Set<String> scopes,
	        @Nullable Map<String, Object> additionalParameters) {

	    this(new AuthorizationGrantType("password"),
	         clientPrincipal,
	         scopes,
	         additionalParameters);
	}
	
	protected CustomPasswordAuthenticationToken(
	        AuthorizationGrantType authorizationGrantType,
	        Authentication clientPrincipal,
	        @Nullable Set<String> scopes,
	        @Nullable Map<String, Object> additionalParameters) {

	    super(authorizationGrantType, clientPrincipal, additionalParameters);
	    this.username = (String) additionalParameters.get("username");
	    this.password = (String) additionalParameters.get("password");
	    this.scopes = Collections.unmodifiableSet(
	            scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Set<String> getScopes() {
		return scopes;
	}
}

	
