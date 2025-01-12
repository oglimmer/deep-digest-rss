package de.oglimmer.news.service;

import de.oglimmer.news.db.NewsRepository;
import de.oglimmer.news.db.News;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public List<News> getNews(int daysAgo) {
        // the variable daysAgo defines for which day data is shown. daysAgo = 0 means today. daysAgo = 1 yesterday and so on. we only return the data for this day
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(daysAgo, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.DAYS);
        return newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);
    }

    public News createNews(News news) {
        news.setCreatedOn(Instant.now());
        return newsRepository.save(news);
    }


}
