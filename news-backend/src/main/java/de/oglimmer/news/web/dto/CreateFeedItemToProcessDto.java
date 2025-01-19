package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateFeedItemToProcessDto {

    private Long feedId;

    private String refId;

    private String url;

    private String title;

}
