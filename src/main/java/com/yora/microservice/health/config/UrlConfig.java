package com.yora.microservice.health.config;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.yora.microservice.health.dto.ServiceHealthUrl;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "services")
public class UrlConfig {

  private List<ServiceHealthUrl> list;

  public List<ServiceHealthUrl> newServiceUrlList() {
    return list.stream().map(e -> SerializationUtils.clone(e)).collect(Collectors.toList());
  }
}
