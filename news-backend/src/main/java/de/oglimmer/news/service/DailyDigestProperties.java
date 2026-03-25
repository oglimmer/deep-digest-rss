/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "daily-digest")
@Getter
@Setter
public class DailyDigestProperties {

  private String discordWebhookUrl;
  private String model;
  private String apiKey;
  private String generationEngine;
}
