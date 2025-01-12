package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateNewsDto {

    private String refId;

    private String url;

    private String text;

    private String title;

}
