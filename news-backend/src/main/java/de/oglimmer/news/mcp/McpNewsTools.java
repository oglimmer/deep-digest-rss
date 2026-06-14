/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.mcp;

import de.oglimmer.news.db.Feed;
import de.oglimmer.news.db.News;
import de.oglimmer.news.service.FeedService;
import de.oglimmer.news.service.NewsService;
import de.oglimmer.news.service.TagGroupService;
import de.oglimmer.news.web.dto.NewsDto;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Read-only MCP tools over the news data, exposed at {@code /mcp}. Each tool reuses the existing
 * service layer; the acting user is taken from the OAuth access token's subject so per-user state
 * (the {@code voted} flag) is honoured.
 */
@Service
@AllArgsConstructor
public class McpNewsTools {

  private final NewsService newsService;
  private final FeedService feedService;
  private final TagGroupService tagGroupService;
  private final ModelMapper modelMapper;

  /** A feed the platform aggregates. */
  public record FeedSummary(Long id, String title, String url) {}

  @McpTool(
      name = "list_news",
      description =
          "List news items for a given day. Returns title, summary text, url, tags and the"
              + " current user's vote state.")
  public List<NewsDto> listNews(
      @ToolParam(
              description = "Day to list, ISO-8601 (YYYY-MM-DD). Defaults to today when omitted.",
              required = false)
          String date,
      @ToolParam(
              description = "Optional comma-separated feed ids to restrict the results.",
              required = false)
          String feedIds) {
    LocalDate day = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
    return newsService.getNewsDto(parseFeedIds(feedIds), day, currentUserName());
  }

  @McpTool(
      name = "search_recent_news",
      description =
          "Search news from the last 24 hours, optionally filtering by tags to include or"
              + " exclude.")
  public List<NewsDto> searchRecentNews(
      @ToolParam(description = "Comma-separated tags to include.", required = false)
          String includeTags,
      @ToolParam(description = "Comma-separated tags to exclude.", required = false)
          String excludeTags,
      @ToolParam(
              description = "Optional comma-separated feed ids to restrict the results.",
              required = false)
          String feedIds) {
    List<News> news =
        newsService.getNewsRollingWindow(
            parseFeedIds(feedIds), parseCsv(includeTags), parseCsv(excludeTags));
    return news.stream().map(n -> modelMapper.map(n, NewsDto.class)).toList();
  }

  @McpTool(name = "list_feeds", description = "List all RSS feeds the platform aggregates.")
  public List<FeedSummary> listFeeds() {
    return feedService.Feeds().stream()
        .map(this::toFeedSummary)
        .sorted((a, b) -> a.id().compareTo(b.id()))
        .toList();
  }

  @McpTool(
      name = "list_tag_groups",
      description =
          "List the curated tag groups for a given day, mapping each group title to its tags.")
  public Map<String, String[]> listTagGroups(
      @ToolParam(
              description = "Day in UTC, ISO-8601 (YYYY-MM-DD). Defaults to today when omitted.",
              required = false)
          String date) {
    LocalDate day = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
    return tagGroupService.getTags(day);
  }

  private FeedSummary toFeedSummary(Feed feed) {
    return new FeedSummary(feed.getId(), feed.getTitle(), feed.getUrl());
  }

  private static List<Long> parseFeedIds(String csv) {
    if (csv == null || csv.isBlank()) {
      return Collections.emptyList();
    }
    return Stream.of(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::parseLong)
        .toList();
  }

  private static List<String> parseCsv(String csv) {
    if (csv == null || csv.isBlank()) {
      return Collections.emptyList();
    }
    return Stream.of(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  private static String currentUserName() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null ? "" : authentication.getName();
  }
}
