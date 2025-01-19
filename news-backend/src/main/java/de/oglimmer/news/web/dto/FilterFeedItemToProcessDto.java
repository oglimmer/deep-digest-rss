package de.oglimmer.news.web.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FilterFeedItemToProcessDto {
    private List<String> refIds;
}
