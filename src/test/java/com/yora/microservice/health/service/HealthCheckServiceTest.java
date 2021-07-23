package com.yora.microservice.health.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.yora.microservice.health.config.UrlConfig;
import com.yora.microservice.health.dto.ServiceHealthUrl;

@ExtendWith(MockitoExtension.class)
public class HealthCheckServiceTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private UrlConfig urlConfig;

	@InjectMocks
	private HealthCheckService service;

	@BeforeEach
	public void beforeAll() {

		when(urlConfig.newServiceUrlList()).thenReturn(List.of(new ServiceHealthUrl("B", "http://ab.com/health"),
				new ServiceHealthUrl("C", "http://abc.com/health"), new ServiceHealthUrl("A", "http://a.com/health")));

	}

	@Test
	@DisplayName("when service urls  empty rest template should not invoked")
	public void when_service_urls_empty_rest_template_should_not_invoked() {

		// given
		when(urlConfig.newServiceUrlList()).thenReturn(Collections.emptyList());

		// when
		service.invokeHealthCheck();

		// then

		verifyNoMoreInteractions(restTemplate, urlConfig);
	}
	
	@Test
	@DisplayName("when service urls  null rest template should not invoked")
	public void when_service_urls_null_rest_template_should_not_invoked() {

		// given
		when(urlConfig.newServiceUrlList()).thenReturn(null);

		// when
		service.invokeHealthCheck();

		// then

		verifyNoMoreInteractions(restTemplate, urlConfig);
	}
	
	

	@Test
	@DisplayName("when service urls not empty rest template should be invoked")
	public void when_service_urls_not_empty_rest_template_should_be_invoked() {

		// given
		when(restTemplate.getForEntity("http://a.com/health", String.class))
				.thenReturn(ResponseEntity.ok("{'status':'up'}"));
		when(restTemplate.getForEntity("http://ab.com/health", String.class))
				.thenReturn(ResponseEntity.ok("{'status':'up'}"));
		when(restTemplate.getForEntity("http://abc.com/health", String.class))
				.thenReturn(ResponseEntity.ok("{'status':'up'}"));

		// when
		service.invokeHealthCheck();

		// then
		verify(urlConfig, Mockito.times(1)).newServiceUrlList();

		verify(restTemplate, Mockito.times(1)).getForEntity("http://a.com/health", String.class);
		verify(restTemplate, Mockito.times(1)).getForEntity("http://ab.com/health", String.class);
		verify(restTemplate, Mockito.times(1)).getForEntity("http://abc.com/health", String.class);
	}

}
