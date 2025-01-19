package de.oglimmer.news.web;

import de.oglimmer.news.service.FeedService;
import de.oglimmer.news.web.dto.FeedDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final FeedService feedService;

    private final ModelMapper modelMapper;

    @GetMapping
    public List<FeedDto> getFeeds() {
        return feedService.Feeds().stream()
                .map(news -> modelMapper.map(news, FeedDto.class))
                .collect(Collectors.toList());
    }


}
