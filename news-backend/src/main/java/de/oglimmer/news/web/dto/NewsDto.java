/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web.dto;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewsDto {

  private Long id;

  private Long feedId;

  private Instant createdOn;

  private String url;

  private String text;

  private String title;

  private Boolean advertising;

  private String[] tags;

  private boolean voted;
}
