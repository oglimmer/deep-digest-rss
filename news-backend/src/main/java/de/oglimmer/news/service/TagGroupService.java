/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.*;
import de.oglimmer.news.web.dto.CreateTagGroupDto;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class TagGroupService {

  private final TagsRepository tagsRepository;
  private final TagGroupRepository tagGroupRepository;
  private final NewsRepository newsRepository;

  public List<Tags> getRawTodaysTags() {
    Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(0, ChronoUnit.DAYS);
    Instant end = start.plus(1, ChronoUnit.DAYS);

    return tagsRepository.findByNewsCreatedOnBetween(start, end);
  }

  public boolean updateTags(CreateTagGroupDto createTagGroupDto) {
    Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(0, ChronoUnit.DAYS);
    Instant end = start.plus(1, ChronoUnit.DAYS);
    LocalDate today = LocalDate.now();

    // get the today's TagGroup to (1) check if the new tags would reduce the number of news items
    // and (2) to delete them
    List<TagGroup> tagGroups = tagGroupRepository.findByCreatedOn(today);

    // get all tags from today
    List<Tags> tagsInDB = tagGroups.stream().map(TagGroup::getTags).flatMap(List::stream).toList();

    // get all tags from the rest call
    List<String> tagsInRest =
        createTagGroupDto.getTags().values().stream().flatMap(Arrays::stream).toList();

    long numberTagsInDB =
        tagsInDB.isEmpty()
            ? 0
            : newsRepository.countByTagsInAndCreatedOnBetween(tagsInDB, start, end);
    long numberTagsinRest =
        tagsInRest.isEmpty()
            ? 0
            : newsRepository.countByTagsTextInAndCreatedOnBetween(tagsInRest, start, end);

    if (numberTagsInDB > numberTagsinRest) {
      log.info(
          "Not updating tags, as it would reduce the number of news items. {} / {}",
          numberTagsInDB,
          numberTagsinRest);
      return false;
    }
    log.debug("Updating tags, old {} to {}", numberTagsInDB, numberTagsinRest);

    // delete all tags from today
    tagGroupRepository.deleteAll(tagGroups);

    // create new tags
    for (String tag : createTagGroupDto.getTags().keySet()) {
      String[] tags = createTagGroupDto.getTags().get(tag);
      List<String> tagList = new ArrayList<>(Arrays.asList(tags));
      tagList.removeIf(tagItem -> tagItem.equalsIgnoreCase("interessant"));
      TagGroup tagGroup = new TagGroup();
      tagGroup.setTags(tagsRepository.findByTextIn(tagList));
      tagGroup.setTitle(tag);
      tagGroup.setCreatedOn(LocalDate.now());
      tagGroupRepository.save(tagGroup);
    }

    // create additional tagGroup for Interessant
    TagGroup interessantTagGroup = new TagGroup();
    interessantTagGroup.setTags(tagsRepository.findByTextIn(List.of("Interessant")));
    interessantTagGroup.setTitle("Interessant");
    interessantTagGroup.setCreatedOn(LocalDate.now());
    tagGroupRepository.save(interessantTagGroup);

    return true;
  }

  public Map<String, String[]> getTags(LocalDate dateUtc) {
    return tagGroupRepository.findByCreatedOn(dateUtc).stream()
        .collect(
            Collectors.toMap(
                TagGroup::getTitle,
                tagGroup -> tagGroup.getTags().stream().map(Tags::getText).toArray(String[]::new)));
  }
}
