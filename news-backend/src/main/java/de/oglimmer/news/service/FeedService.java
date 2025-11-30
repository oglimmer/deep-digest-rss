/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.Feed;
import de.oglimmer.news.db.FeedRepository;
import de.oglimmer.news.web.dto.PatchFeedDto;
import jakarta.transaction.Transactional;
import java.util.Collection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class FeedService {

  private FeedRepository feedRepository;

  public Collection<Feed> Feeds() {
    return feedRepository.findAll();
  }

  public Feed updateFeed(Long id, PatchFeedDto feedDto) {
    Feed feed = feedRepository.findById(id).orElseThrow();
    feed.setCookie(feedDto.getCookie());
    return feed;
  }
}
