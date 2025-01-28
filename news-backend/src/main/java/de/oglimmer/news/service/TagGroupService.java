package de.oglimmer.news.service;

import de.oglimmer.news.db.TagGroup;
import de.oglimmer.news.db.TagGroupRepository;
import de.oglimmer.news.db.Tags;
import de.oglimmer.news.db.TagsRepository;
import de.oglimmer.news.web.dto.CreateTagGroupDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class TagGroupService {

    private final TagsRepository tagsRepository;
    private final TagGroupRepository tagGroupRepository;

    public List<Tags> getRawTodaysTags() {
        Instant start = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(0, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.DAYS);

        return tagsRepository.findByNewsCreatedOnBetween(start, end);
    }

    public void updateTags(CreateTagGroupDto createTagGroupDto) {
        // delete all tags from today
        LocalDate today = LocalDate.now();
        List<TagGroup> tagGroups = tagGroupRepository.findByCreatedOn(today);
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
    }

    public Map<String, String[]> getTodaysTags() {
        LocalDate today = LocalDate.now();
        return tagGroupRepository.findByCreatedOn(today)
                .stream()
                .collect(Collectors.toMap(TagGroup::getTitle, tagGroup -> tagGroup.getTags().stream().map(Tags::getText).toArray(String[]::new)));
    }
}
