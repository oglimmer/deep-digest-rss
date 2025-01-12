package de.oglimmer.news.web;

import de.oglimmer.news.db.News;
import de.oglimmer.news.service.NewsService;
import de.oglimmer.news.web.dto.CreateNewsDto;
import de.oglimmer.news.web.dto.NewsDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    private final ModelMapper modelMapper;

    @GetMapping
    public List<NewsDto> getNews(@RequestParam(required = false, defaultValue = "0") int daysAgo) {
        return newsService.getNews(daysAgo).stream()
                .map(news -> modelMapper.map(news, NewsDto.class))
                .collect(Collectors.toList());
    }

    @PostMapping
    public NewsDto createNews(@RequestBody CreateNewsDto newsDto) {
        return modelMapper.map(newsService.createNews(modelMapper.map(newsDto, News.class)), NewsDto.class);
    }

}
