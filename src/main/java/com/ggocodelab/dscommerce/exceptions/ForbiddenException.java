package com.ggocodelab.dscommerce.exceptions;

@SuppressWarnings("serial")
public class ForbiddenException extends RuntimeException {
	 
	public ForbiddenException(String msg) {
	        super(msg);
	}
}
