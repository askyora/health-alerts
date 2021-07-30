package com.yora.microservice.health.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.yora.microservice.health.service.HealthCheckService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulingConfig {

  @Autowired private HealthCheckService service;

  @Scheduled(cron = "${scheduling.cron-expression}")
  public void scheduleFixedRateWithInitialDelayTask() {
    long now = System.currentTimeMillis();
    log.info(" invokeHealthCheck at : {}", now);
    service.invokeHealthCheck();
  }
}
