package com.yora.microservice.health.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.yora.microservice.health.config.UrlConfig;
import com.yora.microservice.health.dto.ServiceHealthUrl;

@ExtendWith(MockitoExtension.class)
public class HealthCheckServiceTest {

  @Mock private RestTemplate restTemplate;

  @Mock private UrlConfig urlConfig;

  @Mock private Notification notification;

  @InjectMocks private HealthCheckService service;

  @Test
  @DisplayName("when service urls  empty rest template should not invoked")
  public void when_service_urls_empty_rest_template_should_not_invoked() {

    // given
    when(urlConfig.newServiceUrlList()).thenReturn(Collections.emptyList());

    // when
    service.invokeHealthCheck();

    // then

    verifyNoMoreInteractions(restTemplate, urlConfig, notification);
  }

  @Test
  @DisplayName("when service urls  null rest template should not invoked")
  public void when_service_urls_null_rest_template_should_not_invoked() {

    // given
    when(urlConfig.newServiceUrlList()).thenReturn(null);

    // when
    service.invokeHealthCheck();

    // then

    verifyNoMoreInteractions(restTemplate, urlConfig, notification);
  }

  @Test
  @DisplayName("when service urls not empty rest template should be invoked")
  public void when_service_urls_not_empty_rest_template_should_be_invoked() {

    // given

    List<ServiceHealthUrl> collection =
        List.of(
            new ServiceHealthUrl("B", "http://ab.com/health"),
            new ServiceHealthUrl("C", "http://abc.com/health"),
            new ServiceHealthUrl("A", "http://a.com/health"));

    when(urlConfig.newServiceUrlList()).thenReturn(collection);

    when(restTemplate.getForEntity("http://a.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'up'}"));

    when(restTemplate.getForEntity("http://ab.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'down'}"));

    when(restTemplate.getForEntity("http://abc.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'up'}"));

    Mockito.doNothing().when(notification).publish(Mockito.anyList(), Mockito.isNull());

    // when
    service.invokeHealthCheck();

    // then

    assertEquals(collection.get(0).getName(), "B");
    assertEquals(collection.get(1).getName(), "C");
    assertEquals(collection.get(2).getName(), "A");

    assertEquals(collection.get(0).getStatus(), "down");
    assertEquals(collection.get(1).getStatus(), "up");
    assertEquals(collection.get(2).getStatus(), "up");

    verify(urlConfig, Mockito.times(1)).newServiceUrlList();

    verify(notification).publish(Mockito.anyList(), Mockito.isNull());

    verify(restTemplate, Mockito.times(1)).getForEntity("http://a.com/health", String.class);
    verify(restTemplate, Mockito.times(1)).getForEntity("http://ab.com/health", String.class);
    verify(restTemplate, Mockito.times(1)).getForEntity("http://abc.com/health", String.class);
  }

  @Test
  @DisplayName("when service urls not empty rest template should be invoked")
  public void when_rest_throw_exception_() {

    // given

    List<ServiceHealthUrl> collection =
        List.of(
            new ServiceHealthUrl("B", "http://ab.com/health"),
            new ServiceHealthUrl("C", "http://abc.com/health"),
            new ServiceHealthUrl("A", "http://a.com/health"));

    when(urlConfig.newServiceUrlList()).thenReturn(collection);

    when(restTemplate.getForEntity("http://a.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'up'}"));

    when(restTemplate.getForEntity("http://ab.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'down'}"));

    when(restTemplate.getForEntity("http://abc.com/health", String.class))
        .thenThrow(RestClientException.class);

    Mockito.doNothing().when(notification).publish(Mockito.anyList(), Mockito.isNull());

    // when
    service.invokeHealthCheck();

    // then
    assertEquals(collection.get(0).getName(), "B");
    assertEquals(collection.get(1).getName(), "C");
    assertEquals(collection.get(2).getName(), "A");

    assertEquals(collection.get(0).getStatus(), "down");
    assertEquals(collection.get(1).getStatus(), "Exception");
    assertEquals(collection.get(2).getStatus(), "up");

    verify(urlConfig, Mockito.times(1)).newServiceUrlList();

    verify(notification).publish(Mockito.anyList(), Mockito.isNull());

    verify(restTemplate, Mockito.times(1)).getForEntity("http://a.com/health", String.class);
    verify(restTemplate, Mockito.times(1)).getForEntity("http://ab.com/health", String.class);
    verify(restTemplate, Mockito.times(1)).getForEntity("http://abc.com/health", String.class);
  }

  @Test
  @DisplayName("service status changed notification invoked")
  public void when_service_status_changed_notification_invoked() {

    // given

    service.setPrevious(
        List.of(
            new ServiceHealthUrl("B", "http://ab.com/health", "up", HttpStatus.OK),
            new ServiceHealthUrl("C", "http://abc.com/health", "down", HttpStatus.OK)));

    List<ServiceHealthUrl> collection =
        List.of(
            new ServiceHealthUrl("B", "http://ab.com/health"),
            new ServiceHealthUrl("C", "http://abc.com/health"),
            new ServiceHealthUrl("A", "http://a.com/health"));

    when(urlConfig.newServiceUrlList()).thenReturn(collection);

    when(restTemplate.getForEntity("http://a.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'up'}"));

    when(restTemplate.getForEntity("http://ab.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'up'}"));

    when(restTemplate.getForEntity("http://abc.com/health", String.class))
        .thenReturn(ResponseEntity.ok("{'status':'up'}"));

    Mockito.doNothing().when(notification).publish(Mockito.anyList(), Mockito.anyList());

    // when
    service.invokeHealthCheck();

    // then

    assertEquals(collection.get(0).getName(), "B");
    assertEquals(collection.get(1).getName(), "C");
    assertEquals(collection.get(2).getName(), "A");

    assertEquals(collection.get(0).getStatus(), "up");
    assertEquals(collection.get(1).getStatus(), "up");
    assertEquals(collection.get(2).getStatus(), "up");

    verify(urlConfig, Mockito.times(1)).newServiceUrlList();

    verify(notification).publish(Mockito.anyList(), Mockito.anyList());

    verify(restTemplate, Mockito.times(1)).getForEntity("http://a.com/health", String.class);
    verify(restTemplate, Mockito.times(1)).getForEntity("http://ab.com/health", String.class);
    verify(restTemplate, Mockito.times(1)).getForEntity("http://abc.com/health", String.class);
  }
}
