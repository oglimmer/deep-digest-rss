/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.service.DailyDigestService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/daily-digest")
@Slf4j
public class DailyDigestController {

  private final DailyDigestService dailyDigestService;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public DailyDigestController(DailyDigestService dailyDigestService) {
    this.dailyDigestService = dailyDigestService;
  }

  @PostMapping
  public String triggerDigest(
      @RequestParam(defaultValue = "24") int hours,
      @RequestParam(defaultValue = "true") boolean postToDiscord) {
    executor.submit(
        () -> {
          try {
            String result = dailyDigestService.generateDigest(hours, postToDiscord);
            log.info("Daily digest result:\n{}", result);
          } catch (Exception e) {
            log.error("Daily digest generation failed", e);
          }
        });
    return "Digest generation started";
  }
}
