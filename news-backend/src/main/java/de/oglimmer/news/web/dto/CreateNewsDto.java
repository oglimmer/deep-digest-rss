/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateNewsDto {

  private Long feedId;

  private Long originalFeedItemId;

  private String text;

  private Boolean advertising;

  private String[] tags;

  private Boolean timely;

  @Pattern(regexp = "global|international|europa|deutschland|regional|branche")
  private String impactScope;
}
