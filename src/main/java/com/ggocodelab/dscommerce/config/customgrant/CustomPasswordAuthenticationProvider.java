package com.ggocodelab.dscommerce.config.customgrant;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

public class CustomPasswordAuthenticationProvider implements AuthenticationProvider{
	
//	URI de referência RFC para erros OAuth2
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
	
//	Serviços centrais do Authorization Server
	private final OAuth2AuthorizationService authorizationService;
	private final UserDetailsService userDetailsService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
	private final PasswordEncoder passwordEncoder;
	
//	Dados temporários da autenticação do usuário final
	private String username = "";
	private String password = "";
	private Set<String> authorizedScopes = new HashSet<>();
	
//	Construtor responsável por injetar as dependências necessárias para validação de credenciais e geração do token
	public CustomPasswordAuthenticationProvider(
			OAuth2AuthorizationService authorizationService,
			OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator, 
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		
//		Garante que todas as dependências obrigatórias estejam presentes
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "TokenGenerator cannot be null");
		Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
		Assert.notNull(passwordEncoder, "PasswordEncoder cannot be null");
		
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}	

	@Override
	public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {

//		Converte o Authentication genérico para o token customizado do password grant
		CustomPasswordAuthenticationToken customPasswordAuthenticationToken = (CustomPasswordAuthenticationToken) authentication;

//		Recupera e valida o client OAuth2 autenticado
		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(customPasswordAuthenticationToken);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
		
//		Extrai username e password fornecidos na requisição
		username = customPasswordAuthenticationToken.getUsername();
		password = customPasswordAuthenticationToken.getPassword();
		
//		Carrega o usuário a partir do UserDetailsService
		UserDetails user = null;
		try {
			user = userDetailsService.loadUserByUsername(username);
		} catch (UsernameNotFoundException e) {
			throw new OAuth2AuthenticationException("Invalid credentials");
		}
		
//		Valida as credenciais do usuário final
		if (!passwordEncoder.matches(password, user.getPassword()) || !user.getUsername().equals(username)) {
			throw new OAuth2AuthenticationException("Invalid credentials");
		}
		
//		Resolve os scopes autorizados com base nas authorities do usuário e nos scopes permitidos ao client OAuth2
		authorizedScopes = user.getAuthorities().stream()
				.map(scope -> scope.getAuthority())
				.filter(scope -> registeredClient.getScopes().contains(scope))
				.collect(Collectors.toSet());
		
		//-----------Create a new Security Context Holder Context----------
		
//		Atualiza o SecurityContext com informações do usuário autenticado
		OAuth2ClientAuthenticationToken clientAuthentication = 
				(OAuth2ClientAuthenticationToken)SecurityContextHolder.getContext().getAuthentication();
				
		 CustomUserAuthorities customPasswordUser = new CustomUserAuthorities(username, user.getAuthorities());
	        clientAuthentication.setDetails(customPasswordUser);		
		
        var newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(clientAuthentication);
        SecurityContextHolder.setContext(newContext);
        
        //-----------TOKEN BUILDERS----------
        
//      Builder do contexto de geração de tokens
        DefaultOAuth2TokenContext.Builder tokenContextBuilder =
                DefaultOAuth2TokenContext.builder()
                        .registeredClient(registeredClient)
                        .principal(clientPrincipal)
                        .authorizationServerContext(
                                AuthorizationServerContextHolder.getContext())
                        .authorizedScopes(authorizedScopes)
                        .authorizationGrantType(new AuthorizationGrantType("password"))
                        .authorizationGrant(customPasswordAuthenticationToken);
		
//      Builder da autorização persistida
        OAuth2Authorization.Builder authorizationBuilder =
                OAuth2Authorization.withRegisteredClient(registeredClient)
                        .attribute(Principal.class.getName(), clientPrincipal)
                        .principalName(clientPrincipal.getName())
                        .authorizationGrantType(new AuthorizationGrantType("password"))
                        .authorizedScopes(authorizedScopes);
        
      //-----------ACCESS TOKEN----------
        
//      Geração do Access Token
        OAuth2TokenContext tokenContext =
                tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
		
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.",
                    ERROR_URI
            );
            throw new OAuth2AuthenticationException(error);
        }
        
//      Constrói o OAuth2AccessToken final
        OAuth2AccessToken accessToken =
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        generatedAccessToken.getTokenValue(),
                        generatedAccessToken.getIssuedAt(),
                        generatedAccessToken.getExpiresAt(),
                        tokenContext.getAuthorizedScopes()
                );
        
        if (generatedAccessToken instanceof ClaimAccessor) {
            authorizationBuilder.token(
                    accessToken,
                    metadata -> metadata.put(
                            OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                            ((ClaimAccessor) generatedAccessToken).getClaims()
                    )
            );
        } else {
            authorizationBuilder.accessToken(accessToken);
        }
        
//      Salva a autorização no AuthorizationService
        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);
        
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                accessToken
        );        
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return CustomPasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
		OAuth2ClientAuthenticationToken clientPrincipal = null;
		
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		
		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}
		
		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}	
}
