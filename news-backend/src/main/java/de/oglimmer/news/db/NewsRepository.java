package de.oglimmer.news.db;

import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.List;

public interface NewsRepository extends ListCrudRepository<News, Long> {

    List<News> findByCreatedOnBetweenOrderByCreatedOnDesc(Instant start, Instant end);

}

