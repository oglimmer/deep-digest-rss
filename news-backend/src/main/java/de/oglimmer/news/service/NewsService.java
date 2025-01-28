package de.oglimmer.news.service;

import de.oglimmer.news.db.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final FeedItemToProcessRepository feedItemToProcessRepository;
    private final TagsRepository tagsRepository;

    public List<News> getNews(long feedId, int daysAgo) {
        // the variable daysAgo defines for which day data is shown. daysAgo = 0 means today. daysAgo = 1 yesterday and so on. we only return the data for this day
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(daysAgo, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.DAYS);
        if (feedId == 0) {
            return newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);
        } else {
            return newsRepository.findByFeedIdAndCreatedOnBetweenOrderByCreatedOnDesc(feedId, start, end);
        }
    }

    public List<News> getNewsRollingWindow(long feedId, int daysAgo) {
        Instant start = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.DAYS);
        if (feedId == 0) {
            return newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);
        } else {
            return newsRepository.findByFeedIdAndCreatedOnBetweenOrderByCreatedOnDesc(feedId, start, end);
        }
    }

    public News createNews(News news) {
        news.getOriginalFeedItem().setProcessState(ProcessState.DONE);
        news.getOriginalFeedItem().setUpdatedOn(Instant.now());
        news.setCreatedOn(Instant.now());
        news.setTitle(news.getOriginalFeedItem().getTitle());
        news.setUrl(news.getOriginalFeedItem().getUrl());
        tagsRepository.saveAll(news.getTags());
        feedItemToProcessRepository.save(news.getOriginalFeedItem());
        return newsRepository.save(news);
    }


}
