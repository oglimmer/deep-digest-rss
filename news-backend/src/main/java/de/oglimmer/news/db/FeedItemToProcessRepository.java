/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface FeedItemToProcessRepository extends ListCrudRepository<FeedItemToProcess, Long> {

  Optional<FeedItemToProcess> findFirstByProcessStateOrderByCreatedOnAsc(
      Enum<ProcessState> processState);

  @Query("SELECT f.refId FROM feed_item_to_process f WHERE f.refId IN :refIds")
  List<String> findExistingRefIds(@Param("refIds") Collection<String> refIds);
}
