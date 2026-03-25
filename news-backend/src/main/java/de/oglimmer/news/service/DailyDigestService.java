/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.News;
import de.oglimmer.news.db.NewsRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DailyDigestService {

  private final NewsRepository newsRepository;
  private final AiSummarizationService aiSummarizationService;
  private final DiscordService discordService;

  @Scheduled(cron = "0 0 19 * * *", zone = "Europe/Berlin")
  public void generateAndPostDailyDigest() {
    generateDigest(24, true);
  }

  public String generateDigest(int hours, boolean postToDiscord) {
    log.info("Starting digest generation for the last {} hours", hours);

    Instant end = Instant.now();
    Instant start = end.minus(hours, ChronoUnit.HOURS);

    List<News> newsList = newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);

    if (newsList.isEmpty()) {
      log.info("No news found in the last {} hours, skipping digest", hours);
      return "No news found in the last " + hours + " hours.";
    }

    String newsContent =
        newsList.stream()
            .map(news -> "**" + news.getTitle() + "**\n" + news.getText())
            .collect(Collectors.joining("\n\n---\n\n"));

    log.info("Summarizing {} news articles", newsList.size());
    String summary = aiSummarizationService.summarize(newsContent);

    String message = "# Täglicher News-Digest\n\n" + summary;

    if (postToDiscord) {
      discordService.postMessage(message);
    }

    log.info("Digest completed with {} articles", newsList.size());
    return message;
  }
}
