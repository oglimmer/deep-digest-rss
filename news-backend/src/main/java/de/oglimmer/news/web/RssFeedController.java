/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.rss.RssFeedView;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@AllArgsConstructor
public class RssFeedController {

  private RssFeedView view;

  @GetMapping("/rss")
  public ModelAndView getFeed(
      @RequestParam(required = false, defaultValue = "") List<Long> feedIds,
      @RequestParam(required = false, defaultValue = "") List<String> includeTags,
      @RequestParam(required = false, defaultValue = "") List<String> excludeTags) {
    return new ModelAndView(
        view, "rssParams", new RssFeedView.RssParams(feedIds, includeTags, excludeTags));
  }
}
