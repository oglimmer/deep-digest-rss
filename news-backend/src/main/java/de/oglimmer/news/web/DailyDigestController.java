/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.service.DailyDigestService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/daily-digest")
@AllArgsConstructor
public class DailyDigestController {

  private final DailyDigestService dailyDigestService;

  @PostMapping
  public String triggerDigest(
      @RequestParam(defaultValue = "24") int hours,
      @RequestParam(defaultValue = "true") boolean postToDiscord) {
    return dailyDigestService.generateDigest(hours, postToDiscord);
  }
}
