/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.db.Feed;
import de.oglimmer.news.service.FeedService;
import de.oglimmer.news.web.dto.FeedDto;
import de.oglimmer.news.web.dto.PatchFeedDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/feed")
public class FeedController {

  private final FeedService feedService;

  private final ModelMapper modelMapper;

  @GetMapping
  public List<FeedDto> getFeeds() {
    return feedService.Feeds().stream()
        .map(news -> modelMapper.map(news, FeedDto.class))
        .collect(Collectors.toList());
  }

  @PatchMapping("/{id}")
  public FeedDto updateFeed(@PathVariable Long id, @RequestBody PatchFeedDto feedDto) {
    Feed feed = feedService.updateFeed(id, feedDto);
    return modelMapper.map(feed, FeedDto.class);
  }
}
