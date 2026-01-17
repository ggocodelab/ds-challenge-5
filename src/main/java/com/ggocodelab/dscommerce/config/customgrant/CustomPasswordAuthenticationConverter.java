package com.ggocodelab.dscommerce.config.customgrant;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

import jakarta.servlet.http.HttpServletRequest;

public class CustomPasswordAuthenticationConverter implements AuthenticationConverter{

	@Override
	public @Nullable Authentication convert(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
