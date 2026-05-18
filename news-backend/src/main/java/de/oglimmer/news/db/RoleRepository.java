/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface RoleRepository extends ListCrudRepository<Role, Long> {

  Optional<Role> findByName(String name);
}
