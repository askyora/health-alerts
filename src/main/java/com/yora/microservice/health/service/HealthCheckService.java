package com.yora.microservice.health.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.yora.microservice.health.config.UrlConfig;
import com.yora.microservice.health.dto.ServiceHealthUrl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HealthCheckService {

  private static final String EXCEPTION_STATUS = "Exception";

  private static final String EMPTY_BODY_STATUS = "Empty";

  private UrlConfig config;

  private RestTemplate template;

  private Notification notification;

  @Autowired
  public HealthCheckService(UrlConfig config, RestTemplate template, Notification notification) {
    this.config = config;
    this.template = template;
    this.notification = notification;
  }

  @Value("${default.timezone:Asia/Colombo}")
  String zoneId = "Asia/Colombo";

  @Value("${default.status-tag-name:status}")
  String statusKeyName = "status";

  @Setter private List<ServiceHealthUrl> previous;

  public void invokeHealthCheck() {

    List<ServiceHealthUrl> serviceUrls = config.newServiceUrlList();

    if (serviceUrls == null || serviceUrls.isEmpty()) {
      log.error("Service url list is empty, please check your application.yml file.");
      return;
    }

    notifyIfChanged(
        serviceUrls
            .parallelStream()
            .map(e -> checkHealthStatus(e))
            .sorted((x, y) -> x.getName().compareTo(y.getName()))
            .collect(Collectors.toList()));
  }

  private void notifyIfChanged(List<ServiceHealthUrl> newList) {
    if (previous == null || !previous.containsAll(newList)) {
      notification.publish(newList, previous);
      this.previous = newList;
    }
  }

  private ServiceHealthUrl checkHealthStatus(ServiceHealthUrl status) {
    try {
      status.setStatus(EMPTY_BODY_STATUS);
      status.setTimestamp(ZonedDateTime.now(ZoneId.of(zoneId)));
      ResponseEntity<String> response = template.getForEntity(status.getUrl(), String.class);

      if (response.hasBody()) {
        JSONObject json = new JSONObject(response.getBody());
        status.setStatus(json.optString(statusKeyName));
      }
      status.setStatusCode(response.getStatusCode());

    } catch (RestClientException | JSONException e) {
      status.setStatus(EXCEPTION_STATUS);
      status.setTrace(e.getMessage());
    }
    return status;
  }
}
