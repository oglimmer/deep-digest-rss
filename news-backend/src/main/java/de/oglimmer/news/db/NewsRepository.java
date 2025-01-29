package de.oglimmer.news.db;

import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface NewsRepository extends ListCrudRepository<News, Long> {

    List<News> findByCreatedOnBetweenOrderByCreatedOnDesc(Instant start, Instant end);

    List<News> findByFeedIdAndCreatedOnBetweenOrderByCreatedOnDesc(long feedId, Instant start, Instant end);

    long countByTagsInAndCreatedOnBetween(Collection<Tags> tags, Instant start, Instant end);

    long countByTagsTextInAndCreatedOnBetween(Collection<String> tags, Instant start, Instant end);
}

