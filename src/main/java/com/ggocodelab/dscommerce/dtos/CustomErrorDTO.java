package com.ggocodelab.dscommerce.dtos;

import java.time.Instant;

public class CustomErrorDTO {
	
	private Instant timestamp;	
	private Integer status;
	private String errror;
	private String path;
	
	public CustomErrorDTO(Instant timestamp, Integer status, String errror, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.errror = errror;
		this.path = path;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public Integer getStatus() {
		return status;
	}

	public String getErrror() {
		return errror;
	}

	public String getPath() {
		return path;
	}
}
