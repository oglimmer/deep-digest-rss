/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public interface NewsVoteRespository extends ListCrudRepository<NewsVote, Long> {

  List<NewsVote> findAllByNewsInAndUser(Collection<News> newsList, User user);
}
