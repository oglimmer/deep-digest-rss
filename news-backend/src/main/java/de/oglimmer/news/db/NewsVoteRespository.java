package de.oglimmer.news.db;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Collection;
import java.util.List;

public interface NewsVoteRespository extends ListCrudRepository<NewsVote, Long> {

    List<NewsVote> findAllByNewsInAndUser(Collection<News> newsList, User user);
}

