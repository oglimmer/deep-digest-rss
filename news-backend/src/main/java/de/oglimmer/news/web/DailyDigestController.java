/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.service.DailyDigestService;
import de.oglimmer.news.web.dto.DailyDigestDto;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
  private final ModelMapper modelMapper;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public DailyDigestController(DailyDigestService dailyDigestService, ModelMapper modelMapper) {
    this.dailyDigestService = dailyDigestService;
    this.modelMapper = modelMapper;
  }

  @GetMapping
  public ResponseEntity<DailyDigestDto> getDigest(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return dailyDigestService
        .findDigestForDate(date)
        .map(digest -> ResponseEntity.ok(modelMapper.map(digest, DailyDigestDto.class)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public String triggerDigest(
      @RequestParam(defaultValue = "24") int hours,
      @RequestParam(defaultValue = "true") boolean postToDiscord,
      @RequestParam(defaultValue = "false") boolean persist) {
    executor.submit(
        () -> {
          try {
            String result = dailyDigestService.generateDigest(hours, postToDiscord, persist);
            log.info("Daily digest result:\n{}", result);
          } catch (Exception e) {
            log.error("Daily digest generation failed", e);
          }
        });
    return "Digest generation started";
  }
}
