package com.ggocodelab.dscommerce.dtos;

import java.time.Instant;

public class CustomErrorDTO {
	
	private Instant timestamp;	
	private Integer status;
	private String error;
	private String path;
	
	public CustomErrorDTO(Instant timestamp, Integer status, String errror, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = errror;
		this.path = path;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public Integer getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getPath() {
		return path;
	}
}
