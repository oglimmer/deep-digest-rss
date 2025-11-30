/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.service.NewsService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

  private final NewsService newsService;

  @GetMapping("/{id}/voted-news")
  public List<String> voteNews(
      @PathVariable String id,
      @RequestParam(required = false, defaultValue = "") String date,
      @RequestParam(required = false, defaultValue = "") String hours,
      @RequestParam(required = false, defaultValue = "") String max,
      Authentication authentication) {
    String email = authentication.getName();
    return newsService.userNews(id, date, hours, max, email);
  }
}
