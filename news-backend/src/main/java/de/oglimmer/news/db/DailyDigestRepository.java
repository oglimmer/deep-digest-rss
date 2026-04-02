/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyDigestRepository extends JpaRepository<DailyDigest, Long> {

  Optional<DailyDigest> findFirstByCreatedOnBeforeOrderByCreatedOnDesc(Instant before);
}
