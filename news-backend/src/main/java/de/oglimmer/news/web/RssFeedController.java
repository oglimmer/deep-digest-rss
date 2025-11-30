/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.rss.RssFeedView;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

@RestController
@AllArgsConstructor
public class RssFeedController {

  private RssFeedView view;

  @GetMapping("/rss")
  public View getFeed() {
    return view;
  }
}
