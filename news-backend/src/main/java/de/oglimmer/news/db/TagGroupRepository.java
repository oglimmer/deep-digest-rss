package de.oglimmer.news.db;

import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface TagGroupRepository extends ListCrudRepository<TagGroup, Long> {

    List<TagGroup> findByCreatedOn(LocalDate createdOn);

}

