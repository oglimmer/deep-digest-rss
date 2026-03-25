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
    log.info("Starting daily digest generation");

    Instant end = Instant.now();
    Instant start = end.minus(24, ChronoUnit.HOURS);

    List<News> newsList = newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);

    if (newsList.isEmpty()) {
      log.info("No news found in the last 24 hours, skipping digest");
      return;
    }

    String newsContent =
        newsList.stream()
            .map(news -> "**" + news.getTitle() + "**\n" + news.getText())
            .collect(Collectors.joining("\n\n---\n\n"));

    log.info("Summarizing {} news articles", newsList.size());
    String summary = aiSummarizationService.summarize(newsContent);

    String message = "**Daily News Digest**\n\n" + summary;
    discordService.postMessage(message);

    log.info("Daily digest completed with {} articles", newsList.size());
  }
}
