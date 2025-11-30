/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public interface TagGroupRepository extends ListCrudRepository<TagGroup, Long> {

  List<TagGroup> findByCreatedOn(LocalDate createdOn);
}
