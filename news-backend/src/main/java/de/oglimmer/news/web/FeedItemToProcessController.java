package de.oglimmer.news.web;

import de.oglimmer.news.db.FeedItemToProcess;
import de.oglimmer.news.service.FeedItemToProcessService;
import de.oglimmer.news.web.dto.CreateFeedItemToProcessDto;
import de.oglimmer.news.web.dto.FeedItemToProcessDto;
import de.oglimmer.news.web.dto.FilterFeedItemToProcessDto;
import de.oglimmer.news.web.dto.PatchFeedItemToProcessDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/feed-item-to-process")
public class FeedItemToProcessController {

    private final FeedItemToProcessService feedItemToProcessService;

    private final ModelMapper modelMapper;

    @GetMapping("/next")
    public ResponseEntity<?> getNextFeedItemToProcess() {
        FeedItemToProcess feedItemToProcess = feedItemToProcessService.getFeedItemToProcess();
        if (feedItemToProcess == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(modelMapper.map(feedItemToProcess, FeedItemToProcessDto.class));
    }

    @PostMapping("/filter")
    public List<String> filterFeedItemToProcess(@RequestBody FilterFeedItemToProcessDto filterFeedItemToProcessDto) {
        return feedItemToProcessService.filterFeedItemToProcess(filterFeedItemToProcessDto);
    }

    @PostMapping
    public FeedItemToProcessDto createFeedItemToProcess(@RequestBody CreateFeedItemToProcessDto createFeedItemToProcessDto) {
        FeedItemToProcess dataFromUser = modelMapper.map(createFeedItemToProcessDto, FeedItemToProcess.class);
        FeedItemToProcess dataAfterPersist = feedItemToProcessService.createFeedItemToProcess(dataFromUser);
        return modelMapper.map(dataAfterPersist, FeedItemToProcessDto.class);
    }

    @PatchMapping("/{id}")
    public FeedItemToProcessDto patchFeedItemToProcess(@RequestBody PatchFeedItemToProcessDto patchFeedItemToProcessDto, @PathVariable Long id) {
        return modelMapper.map(feedItemToProcessService.patchFeedItemToProcess(id, patchFeedItemToProcessDto), FeedItemToProcessDto.class);
    }

}
