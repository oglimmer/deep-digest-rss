/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.*;
import de.oglimmer.news.web.dto.NewsDto;
import de.oglimmer.news.web.dto.PatchNewsDto;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class NewsService {

  private final NewsRepository newsRepository;
  private final FeedItemToProcessRepository feedItemToProcessRepository;
  private final TagsRepository tagsRepository;
  private final UserRepository userRepository;
  private final NewsVoteRespository newsVoteRespository;
  private final ModelMapper modelMapper;

  private List<News> getNews(List<Long> feedIds, LocalDate date, User user) {
    TimeZone timeZone =
        user == null
            ? TimeZone.getTimeZone("Europe/Berlin")
            : TimeZone.getTimeZone(user.getTimezone());
    Instant start = date.atStartOfDay(timeZone.toZoneId()).toInstant();
    Instant end = start.plus(1, ChronoUnit.DAYS);
    if (feedIds.isEmpty()) {
      return newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);
    } else {
      return newsRepository.findByFeedIdAndCreatedOnBetweenOrderByCreatedOnDesc(
          feedIds, start, end);
    }
  }

  public List<News> getNewsRollingWindow(int daysAgo) {
    Instant start = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
    Instant end = start.plus(1, ChronoUnit.DAYS);
    return newsRepository.findByCreatedOnBetweenOrderByCreatedOnDesc(start, end);
  }

  public News createNews(News news) {
    news.getOriginalFeedItem().setProcessState(ProcessState.DONE);
    news.getOriginalFeedItem().setUpdatedOn(Instant.now());
    news.setCreatedOn(Instant.now());
    news.setTitle(news.getOriginalFeedItem().getTitle());
    news.setUrl(news.getOriginalFeedItem().getUrl());
    tagsRepository.saveAll(news.getTags());
    feedItemToProcessRepository.save(news.getOriginalFeedItem());
    return newsRepository.save(news);
  }

  public void vote(Long id, boolean vote, String email) {
    News news = newsRepository.findById(id).orElseThrow();
    User user = userRepository.findByEmail(email).orElseThrow();
    if (vote) {
      NewsVote newsVote =
          NewsVote.builder().user(user).news(news).voteDate(LocalDate.now()).build();
      news.getVotes().add(newsVote);
      user.getVotes().add(newsVote);
      newsVoteRespository.save(newsVote);
    } else {
      Optional<NewsVote> first =
          news.getVotes().stream().filter(v -> v.getUser() == user).findFirst();
      if (first.isEmpty()) {
        log.info("Vote not found for user {} and news {}", user.getEmail(), news.getId());
      } else {
        NewsVote newsVote = first.get();
        newsVoteRespository.delete(newsVote);
      }
    }
  }

  public Map<Long, Boolean> getVotes(List<News> newsList, User user) {
    List<NewsVote> allByNewsIdIn = newsVoteRespository.findAllByNewsInAndUser(newsList, user);
    return allByNewsIdIn.stream().collect(Collectors.toMap(v -> v.getNews().getId(), v -> true));
  }

  public List<NewsDto> getNewsDto(List<Long> feedIds, LocalDate dateAsLocalDate, String name) {
    User user = userRepository.findByEmail(name).orElse(null);
    List<News> newsList = getNews(feedIds, dateAsLocalDate, user);
    List<NewsDto> list =
        newsList.stream().map(news -> modelMapper.map(news, NewsDto.class)).toList();
    if (user != null) {
      Map<Long, Boolean> voteMap = getVotes(newsList, user);
      list.forEach(newsDto -> newsDto.setVoted(voteMap.getOrDefault(newsDto.getId(), false)));
    }
    return list;
  }

  public List<String> userNews(String id, String date, String hours, String max, String email) {
    User user;
    if ("me".equals(id)) {
      user = userRepository.findByEmail(email).orElseThrow();
    } else {
      user = userRepository.findById(Long.parseLong(id)).orElseThrow();
    }
    int iMax = max.isEmpty() ? 50 : Integer.parseInt(max);
    Instant start, end;
    if (!date.isEmpty()) {
      TimeZone timeZone = TimeZone.getTimeZone(user.getTimezone());
      LocalDate lDate = LocalDate.parse(date);
      start = lDate.atStartOfDay(timeZone.toZoneId()).toInstant();
      end = start.plus(1, ChronoUnit.DAYS);
    } else {
      long iHours = 24;
      if (!hours.isEmpty()) {
        iHours = Long.parseLong(hours);
      }
      start = Instant.now().minus(iHours, ChronoUnit.HOURS);
      end = Instant.now();
    }
    List<News> byVotesUserAndCreatedOnBetween =
        newsRepository.findByVotesUserAndCreatedOnBetweenOrderByCreatedOn(
            user, start, end, Limit.of(iMax));
    return byVotesUserAndCreatedOnBetween.stream().map(News::getTitle).toList();
  }

  public News getNewsByFeedItemToProcessId(String id) {
    return newsRepository.findByOriginalFeedItemId(Long.parseLong(id)).orElseThrow();
  }

  public News patch(Long id, PatchNewsDto patchNewsDto) {
    News news = newsRepository.findById(id).orElseThrow();
    List<Tags> tags = news.getTags();
    String[] tagsToAdd = patchNewsDto.getTagsToAdd();
    if (tagsToAdd != null) {
      for (String tag : tagsToAdd) {
        boolean tagExists =
            tags.stream().anyMatch(existingTag -> existingTag.getText().equalsIgnoreCase(tag));
        if (!tagExists) {
          Tags newTag = Tags.builder().text(tag).build();
          tags.add(newTag);
        }
      }
    }
    tagsRepository.saveAll(tags);
    return news;
  }
}
