package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FeedItemToProcessDto {

    private Long id;

    private FeedDto feed;

    private String refId;

    private String url;

    private String title;

    private String processState;

    private String createdOn;

    private String updatedOn;
}
