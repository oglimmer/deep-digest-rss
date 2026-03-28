/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@AllArgsConstructor
@Slf4j
public class DiscordService {

  private static final int MAX_MESSAGE_LENGTH = 2000;

  private final DailyDigestProperties properties;

  public void postMessage(String message) {
    List<String> webhookUrls =
        properties.getDiscordWebhookUrls().stream()
            .filter(url -> url != null && !url.isBlank())
            .toList();
    if (webhookUrls.isEmpty()) {
      log.warn("No Discord webhook URLs configured, skipping message");
      return;
    }

    RestClient restClient = RestClient.create();
    List<String> chunks = splitMessage(message);
    for (String webhookUrl : webhookUrls) {
      try {
        for (String chunk : chunks) {
          restClient
              .post()
              .uri(webhookUrl)
              .contentType(MediaType.APPLICATION_JSON)
              .body(Map.of("content", chunk))
              .retrieve()
              .toBodilessEntity();
        }
        log.info("Daily digest posted to Discord webhook");
      } catch (Exception e) {
        log.error("Failed to post to Discord webhook: {}", webhookUrl, e);
      }
    }
  }

  private List<String> splitMessage(String message) {
    List<String> chunks = new ArrayList<>();
    while (message.length() > MAX_MESSAGE_LENGTH) {
      int splitAt = message.lastIndexOf('\n', MAX_MESSAGE_LENGTH);
      if (splitAt <= 0) {
        splitAt = MAX_MESSAGE_LENGTH;
      }
      chunks.add(message.substring(0, splitAt));
      message = message.substring(splitAt).stripLeading();
    }
    if (!message.isEmpty()) {
      chunks.add(message);
    }
    return chunks;
  }
}
