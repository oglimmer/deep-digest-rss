package de.oglimmer.news.web;

import de.oglimmer.news.db.Tags;
import de.oglimmer.news.service.TagGroupService;
import de.oglimmer.news.web.dto.CreateTagGroupDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tag-group")
public class TagGroupController {

    private final TagGroupService tagGroupService;

    private final ModelMapper modelMapper;

    @GetMapping("/raw")
    public List<String> getRawTodaysTags() {
        return tagGroupService.getRawTodaysTags().stream()
                .map(Tags::getText)
                .collect(Collectors.toList());
    }

    @GetMapping
    public Map<String, String[]> getTodaysTags() {
        return tagGroupService.getTodaysTags();
    }


    @PatchMapping
    public void updateTags(@RequestBody CreateTagGroupDto createTagGroupDto) {
        tagGroupService.updateTags(createTagGroupDto);
    }

}
