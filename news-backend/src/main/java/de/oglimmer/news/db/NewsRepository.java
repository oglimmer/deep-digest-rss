package de.oglimmer.news.db;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface NewsRepository extends ListCrudRepository<News, Long> {

    @Query("select n from news n join fetch tags t where n.createdOn between :start and :end order by n.createdOn desc")
    List<News> findByCreatedOnBetweenOrderByCreatedOnDesc(Instant start, Instant end);

    @Query("select n from news n join fetch tags t join feed f where n.createdOn between :start and :end and f.id in :feedIds order by n.createdOn desc")
    List<News> findByFeedIdAndCreatedOnBetweenOrderByCreatedOnDesc(List<Long> feedIds, Instant start, Instant end);

    long countByTagsInAndCreatedOnBetween(Collection<Tags> tags, Instant start, Instant end);

    long countByTagsTextInAndCreatedOnBetween(Collection<String> tags, Instant start, Instant end);
}

