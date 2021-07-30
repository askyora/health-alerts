package com.yora.microservice.health.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"name", "status", "statusCode", "url"})
@NoArgsConstructor
@Data
public class ServiceHealthUrl implements Serializable {

  private static final long serialVersionUID = 3582234802280630483L;

  public ServiceHealthUrl(String name, String url) {
    super();
    this.name = name;
    this.url = url;
  }

  public ServiceHealthUrl(String name, String url, String status, HttpStatus statusCode) {
    this(name, url);
    this.status = status;
    this.statusCode = statusCode;
  }

  private String name;
  private String url;
  private String status;
  private String trace;
  private ZonedDateTime timestamp;
  public HttpStatus statusCode;
}
