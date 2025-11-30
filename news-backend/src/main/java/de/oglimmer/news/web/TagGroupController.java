/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.db.Tags;
import de.oglimmer.news.service.TagGroupService;
import de.oglimmer.news.web.dto.CreateTagGroupDto;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public Map<String, String[]> getTags(
      @RequestParam(required = false, defaultValue = "") String date,
      @RequestParam(required = false, defaultValue = "Europe/Berlin") String timeZone) {
    LocalDate dateAsLocalDate = date.isEmpty() ? LocalDate.now() : LocalDate.parse(date);
    TimeZone timeZoneAsTimeZone = TimeZone.getTimeZone(timeZone);
    LocalDate dateAsUtc =
        convertDateToDateByMovingTimeFromLocalToUtc(timeZoneAsTimeZone, dateAsLocalDate);
    return tagGroupService.getTags(dateAsUtc);
  }

  private static LocalDate convertDateToDateByMovingTimeFromLocalToUtc(
      TimeZone timeZoneAsTimeZone, LocalDate dateAsLocalDate) {
    ZoneId zoneId = timeZoneAsTimeZone.toZoneId();
    LocalTime localTime = LocalTime.now(zoneId);
    LocalDateTime zonedDateTimeOriginal = dateAsLocalDate.atTime(localTime);
    ZonedDateTime utcZonedDateTime = zonedDateTimeOriginal.atZone((ZoneId.of("UTC")));
    return utcZonedDateTime.toLocalDate();
  }

  @PatchMapping
  public ResponseEntity<Void> updateTags(@RequestBody CreateTagGroupDto createTagGroupDto) {
    if (tagGroupService.updateTags(createTagGroupDto)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
  }
}
