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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HealthCheckService {

	private static final String EXCEPTION_STATUS = "Exception";

	private static final String EMPTY_BODY_STATUS = "Empty";

	@Autowired
	private UrlConfig config;

	@Autowired
	private RestTemplate template;

	@Value("${default.timezone:Asia/Colombo}")
	String zoneId = "Asia/Colombo";

	@Value("${default.status-tag-name:status}")
	String statusKeyName = "status";

	private List<ServiceHealthUrl> previous;

	public void invokeHealthCheck() {
		
		notifyIfChanged(config.newServiceUrlList().parallelStream().map(e -> checkHealthStatus(e))
				.sorted((x, y) -> x.getName().compareTo(y.getName())).collect(Collectors.toList()));
		

	}

	private void notifyIfChanged(List<ServiceHealthUrl> newList) {
		if (previous == null || !newList.containsAll(previous)) {

			newList.stream().map(e -> e.toString()).forEach(log::error);
		}
		this.previous = newList;
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
			log.error(e.getMessage());
			status.setTrace(e.getMessage());
		}
		return status;
	}

}
