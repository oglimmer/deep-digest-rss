package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class NewsDto extends CreateNewsDto {

    private Long id;

    private Instant createdOn;

}
