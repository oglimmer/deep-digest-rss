package de.oglimmer.news.db;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface FeedItemToProcessRepository extends ListCrudRepository<FeedItemToProcess, Long> {

    Optional<FeedItemToProcess> findFirstByProcessStateOrderByCreatedOnAsc(Enum<ProcessState> processState);

}

