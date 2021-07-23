package com.yora.microservice.health.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = { "name", "status", "statusCode", "url" })
public class ServiceHealthUrl implements Serializable {

	private static final long serialVersionUID = 3582234802280630483L;
	private String name;
	private String url;
	private String status;
	private String trace;
	private ZonedDateTime timestamp;
	public HttpStatus statusCode;

}
