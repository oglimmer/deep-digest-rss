package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

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

}
