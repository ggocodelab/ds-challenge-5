package com.ggocodelab.dscommerce.config.customgrant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;


public class CustomPasswordAuthenticationConverter implements AuthenticationConverter{
	
//	Definição das constantes manualmente
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	
	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {
		
//		Obtém o grant_type da requisição e garante que seja "password"
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (!"password".equals(grantType)) {
			return null;
		}
		
//		Extrai todos os parâmetros da requisição HTTP em uma estrutura uniforme
		MultiValueMap<String, String> parameters = getParameters(request);

//		Lê o parâmetro scope (opcional) conforme o padrão OAuth2
		String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
		
//		Valida se o scope foi informado mais de uma vez (requisição inválida)
		if (StringUtils.hasText(scope) && parameters
				.get(OAuth2ParameterNames.SCOPE)
				.size() != 1) {
					throw new OAuth2AuthenticationException(
							OAuth2ErrorCodes.INVALID_REQUEST
							);
				}
//		username required
		String username = parameters.getFirst(USERNAME);
		if (!StringUtils.hasText(username) || parameters.get(USERNAME).size() != 1) {
		    throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}		
	
//		Lê e valida o parâmetro password (obrigatório no password grant)
		String password = parameters.getFirst(PASSWORD);
		if (!StringUtils.hasText(password) || parameters.get(PASSWORD).size() != 1) {
					throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}
		
//		Converte o scope em um Set<String> para uso interno pelo Authorization Server
		Set<String> requestedScopes = null;
		if (StringUtils.hasText(scope)) {
			requestedScopes = new HashSet<>(Arrays.asList(
					StringUtils.delimitedListToStringArray(scope, " "))
			);
		}
		
//		Coleta parâmetros adicionais (exceto grant_type e scope)  que serão repassados ao AuthenticationProvider
		Map<String, Object> additionalParameters = new HashMap<>();
		parameters.forEach((key, value) -> {
			
			if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
					!key.equals(OAuth2ParameterNames.SCOPE)) {
				additionalParameters.put(key, value.get(0));
			}
		});
		
//		Recupera o client autenticado do contexto de segurança e cria o token de autenticação customizado do password grant
		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();	
		return new CustomPasswordAuthenticationToken(
				clientPrincipal, 
				requestedScopes, 
				additionalParameters
				);
		}
		
		private static MultiValueMap<String, String> getParameters(HttpServletRequest request) {

//			Obtém o mapa bruto de parâmetros da requisição HTTP
			Map<String, String[]> parameterMap = request.getParameterMap();
			
//			Converte o map para MultiValueMap
			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());

//			Normaliza todos os parâmetros preservando múltiplos valores por chave
			parameterMap.forEach((key, values) -> {
				if (values.length > 0) {
					for (String value : values) {
						parameters.add(key, value);
					}
				}
			});
			
			return parameters;
		}		
		
	}

