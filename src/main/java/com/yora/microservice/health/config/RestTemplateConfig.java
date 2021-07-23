package com.yora.microservice.health.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import lombok.Data;

@Data
@Configuration
public class RestTemplateConfig {

	@Value("${rest.read-timeout:3600}")
	private int readTimeout;

	@Value("${rest.connect-timeout:3600}")
	private int connectTimeout;

	@Bean
	public RestTemplate initTemplate() {
		return new RestTemplateBuilder().setReadTimeout(Duration.ofMillis(readTimeout))
				.setConnectTimeout(Duration.ofMillis(connectTimeout))
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
	}

}
