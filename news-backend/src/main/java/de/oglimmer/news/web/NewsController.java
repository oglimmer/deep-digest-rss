package de.oglimmer.news.web;

import de.oglimmer.news.db.News;
import de.oglimmer.news.service.NewsService;
import de.oglimmer.news.web.dto.CreateNewsDto;
import de.oglimmer.news.web.dto.NewsDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    private final ModelMapper modelMapper;

    @GetMapping
    public List<NewsDto> getNews(@RequestParam(required = false, defaultValue = "") String date,
                                 @RequestParam(required = false, defaultValue = "0") long feedId,
                                 @RequestParam(required = false, defaultValue = "Europe/Berlin") String timeZone) {
        LocalDate dateAsLocalDate = date.isEmpty() ? LocalDate.now() : LocalDate.parse(date);
        return newsService.getNews(feedId, dateAsLocalDate, TimeZone.getTimeZone(timeZone)).stream()
                .map(news -> modelMapper.map(news, NewsDto.class))
                .collect(Collectors.toList());
    }

    @PostMapping
    public NewsDto createNews(@RequestBody CreateNewsDto newsDto) {
        News dataFromUser = modelMapper.map(newsDto, News.class);
        News dataAfterPersist = newsService.createNews(dataFromUser);
        return modelMapper.map(dataAfterPersist, NewsDto.class);
    }

}
