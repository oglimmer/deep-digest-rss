package de.oglimmer.news.service;

import de.oglimmer.news.db.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimeZone;

@Service
@AllArgsConstructor
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final FeedItemToProcessRepository feedItemToProcessRepository;
    private final TagsRepository tagsRepository;

    public List<News> getNews(long feedId, LocalDate date, TimeZone timeZone) {
        Instant start = date.atStartOfDay(timeZone.toZoneId()).toInstant();
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
