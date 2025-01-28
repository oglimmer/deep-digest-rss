package de.oglimmer.news.db;

import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagsRepository extends ListCrudRepository<Tags, Long> {

    Optional<Tags> findByText(String text);

    List<Tags> findByNewsCreatedOnBetween(Instant startTime, Instant endTime);

    List<Tags> findByTextIn(Collection<String> texts);
}

