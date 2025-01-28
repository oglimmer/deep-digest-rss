package de.oglimmer.news.web.dto;

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

}
