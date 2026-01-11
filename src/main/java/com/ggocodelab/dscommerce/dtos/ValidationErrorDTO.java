package com.ggocodelab.dscommerce.dtos;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorDTO extends CustomErrorDTO{
	
	private List<FieldMessageDTO> errors = new ArrayList<>();

	public ValidationErrorDTO(Instant timestamp, Integer status, String errror, String path) {
		super(timestamp, status, errror, path);
	}
	
	public List<FieldMessageDTO> getErrors() {
	       return errors;
	}
	
	public void addError(String fieldName, String message) {
    	errors.removeIf(x -> x.getFieldName().equals(fieldName));
    	errors.add(new FieldMessageDTO(fieldName, message));
    }
}
