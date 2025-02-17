package de.oglimmer.news.web;

import de.oglimmer.news.db.News;
import de.oglimmer.news.service.NewsService;
import de.oglimmer.news.web.dto.CreateNewsDto;
import de.oglimmer.news.web.dto.NewsDto;
import de.oglimmer.news.web.dto.PatchNewsDto;
import de.oglimmer.news.web.dto.VoteDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    private final ModelMapper modelMapper;

    @GetMapping
    public List<NewsDto> getNews(@RequestParam(required = false, defaultValue = "") String date,
                                 @RequestParam(required = false, defaultValue = "") String feedIdList,
                                 Authentication authentication) {
        LocalDate dateAsLocalDate = date.isEmpty() ? LocalDate.now() : LocalDate.parse(date);
        List<Long> feedIds = feedIdList.isEmpty() ? Collections.emptyList() : Stream.of(feedIdList.split(",")).map(Long::parseLong).collect(Collectors.toList());
        return newsService.getNewsDto(feedIds, dateAsLocalDate, authentication.getName());
    }

    @PostMapping
    public NewsDto createNews(@RequestBody CreateNewsDto newsDto) {
        News dataFromUser = modelMapper.map(newsDto, News.class);
        News dataAfterPersist = newsService.createNews(dataFromUser);
        return modelMapper.map(dataAfterPersist, NewsDto.class);
    }

    @PostMapping("/{id}/vote")
    public void voteNews(@RequestBody VoteDto voteDto, @PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        newsService.vote(id, voteDto.isVote(), email);
    }

    @GetMapping("/by-ref/{id}")
    public NewsDto getFeedByRefId(@PathVariable String id) {
        News news = newsService.getNewsByFeedItemToProcessId(id);
        return modelMapper.map(news, NewsDto.class);
    }

    @PatchMapping("/{id}")
    public NewsDto patch(@RequestBody PatchNewsDto patchNewsDto, @PathVariable Long id) {
        News news = newsService.patch(id, patchNewsDto);
        return modelMapper.map(news, NewsDto.class);
    }
}
