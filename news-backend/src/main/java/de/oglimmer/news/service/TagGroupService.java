package de.oglimmer.news.service;

import de.oglimmer.news.db.*;
import de.oglimmer.news.web.dto.CreateTagGroupDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // get the today's TagGroup to (1) check if the new tags would reduce the number of news items and (2) to delete them
        List<TagGroup> tagGroups = tagGroupRepository.findByCreatedOn(today);

        // get all tags from today
        List<Tags> tagsInDB = tagGroups.stream()
                .map(TagGroup::getTags)
                .flatMap(List::stream)
                .toList();

        // get all tags from the rest call
        List<String> tagsInRest = createTagGroupDto.getTags().values().stream()
                .flatMap(Arrays::stream)
                .toList();

        long numberTagsInDB = newsRepository.countByTagsInAndCreatedOnBetween(tagsInDB, start, end);
        long numberTagsinRest = newsRepository.countByTagsTextInAndCreatedOnBetween(tagsInRest, start, end);

        if (numberTagsInDB > numberTagsinRest) {
            log.info("Not updating tags, as it would reduce the number of news items. {} / {}", numberTagsInDB, numberTagsinRest);
            return false;
        }
        log.debug("Updating tags, old {} to {}", numberTagsInDB, numberTagsinRest);

        // delete all tags from today
        tagGroupRepository.deleteAll(tagGroups);

        // create new tags
        for (String tag : createTagGroupDto.getTags().keySet()) {
            String[] tags = createTagGroupDto.getTags().get(tag);
            TagGroup tagGroup = new TagGroup();
            tagGroup.setTags(tagsRepository.findByTextIn(List.of(tags)));
            tagGroup.setTitle(tag);
            tagGroup.setCreatedOn(LocalDate.now());
            tagGroupRepository.save(tagGroup);
        }

        return true;
    }

    public Map<String, String[]> getTags(int daysAgo) {
        LocalDate today = LocalDate.now().minusDays(daysAgo);
        return tagGroupRepository.findByCreatedOn(today)
                .stream()
                .collect(Collectors.toMap(TagGroup::getTitle, tagGroup -> tagGroup.getTags().stream().map(Tags::getText).toArray(String[]::new)));
    }
}
