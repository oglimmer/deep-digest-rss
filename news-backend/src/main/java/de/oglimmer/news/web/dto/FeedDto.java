package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class FeedDto {

    private Long id;

    private String url;

    private String title;

    private Instant createdOn;

}
