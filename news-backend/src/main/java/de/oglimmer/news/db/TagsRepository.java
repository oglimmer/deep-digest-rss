/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface TagsRepository extends ListCrudRepository<Tags, Long> {

  Optional<Tags> findByText(String text);

  List<Tags> findByNewsCreatedOnBetween(Instant startTime, Instant endTime);

  List<Tags> findByTextIn(Collection<String> texts);
}
