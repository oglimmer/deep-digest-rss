/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface ApiKeyRepository extends ListCrudRepository<ApiKey, Long> {

  Optional<ApiKey> findByName(String name);

  List<ApiKey> findAllByRevokedAtIsNull();
}
