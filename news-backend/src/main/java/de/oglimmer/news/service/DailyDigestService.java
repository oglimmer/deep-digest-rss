/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.DailyDigest;
import de.oglimmer.news.db.DailyDigestRepository;
import de.oglimmer.news.db.News;
import de.oglimmer.news.db.NewsRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
  private final DailyDigestRepository dailyDigestRepository;

  @Scheduled(cron = "0 0 19 * * *", zone = "Europe/Berlin")
  public void generateAndPostDailyDigest() {
    generateDigest(24, true, true);
  }

  public String generateDigest(int hours, boolean postToDiscord, boolean persist) {
    log.info("Starting digest generation for the last {} hours", hours);

    Instant end = Instant.now();
    Instant start = end.minus(hours, ChronoUnit.HOURS);

    List<News> newsList = newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);

    if (newsList.isEmpty()) {
      log.info("No news found in the last {} hours, skipping digest", hours);
      return "No news found in the last " + hours + " hours.";
    }

    List<String> articles =
        newsList.stream()
            .map(
                news -> {
                  StringBuilder sb = new StringBuilder();
                  sb.append("**").append(news.getTitle()).append("**\n");
                  sb.append(news.getText());
                  if (news.getTimely() != null) {
                    sb.append("\n[Aktuell: ").append(news.getTimely() ? "Ja" : "Nein").append("]");
                  }
                  if (news.getImpactScope() != null) {
                    sb.append(" [Reichweite: ").append(news.getImpactScope()).append("]");
                  }
                  return sb.toString();
                })
            .toList();

    log.info("Summarizing {} news articles", newsList.size());
    String summary = aiSummarizationService.summarize(articles);

    String message = "# Täglicher News-Digest\n\n" + summary;

    if (postToDiscord) {
      discordService.postMessage(message);
    }

    if (persist) {
      dailyDigestRepository.save(
          DailyDigest.builder().content(message).createdOn(Instant.now()).build());
    }

    log.info("Digest completed with {} articles", newsList.size());
    return message;
  }

  public Optional<DailyDigest> findDigestForDate(LocalDate date) {
    Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.of("Europe/Berlin")).toInstant();
    return dailyDigestRepository.findFirstByCreatedOnBeforeOrderByCreatedOnDesc(endOfDay);
  }
}
