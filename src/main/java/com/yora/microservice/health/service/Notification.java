package com.yora.microservice.health.service;

import java.util.List;

import com.yora.microservice.health.dto.ServiceHealthUrl;

public interface Notification {

	void publish(List<ServiceHealthUrl> newList, List<ServiceHealthUrl> previous);

}
