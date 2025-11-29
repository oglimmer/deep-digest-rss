package de.oglimmer.news.service;

import de.oglimmer.news.db.FeedItemToProcess;
import de.oglimmer.news.db.FeedItemToProcessRepository;
import de.oglimmer.news.db.ProcessState;
import de.oglimmer.news.web.dto.FilterFeedItemToProcessDto;
import de.oglimmer.news.web.dto.PatchFeedItemToProcessDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class FeedItemToProcessService {

    private FeedItemToProcessRepository feedItemToProcessRepository;

    public FeedItemToProcess getFeedItemToProcessAndMarkAsInProcess() {
        Optional<FeedItemToProcess> feedItemToProcess = feedItemToProcessRepository.findFirstByProcessStateOrderByCreatedOnAsc(ProcessState.NEW);
        if (feedItemToProcess.isPresent()) {
            FeedItemToProcess itemToProcess = feedItemToProcess.get();
            itemToProcess.setProcessState(ProcessState.IN_PROGRESS);
            itemToProcess.setUpdatedOn(Instant.now());
            itemToProcess.getFeed().getUrl();
            return itemToProcess;
        }
        return null;
    }

    public boolean getFeedItemToProcess() {
        Optional<FeedItemToProcess> feedItemToProcess = feedItemToProcessRepository.findFirstByProcessStateOrderByCreatedOnAsc(ProcessState.NEW);
        return feedItemToProcess.isPresent();
    }

    public FeedItemToProcess createFeedItemToProcess(FeedItemToProcess feedItemToProcess) {
        feedItemToProcess.setCreatedOn(Instant.now());
        feedItemToProcess.setUpdatedOn(Instant.now());
        feedItemToProcess.setProcessState(ProcessState.NEW);
        return feedItemToProcessRepository.save(feedItemToProcess);
    }

    public FeedItemToProcess patchFeedItemToProcess(Long id, PatchFeedItemToProcessDto patchFeedItemToProcessDto) {
        FeedItemToProcess feedItemToProcess = feedItemToProcessRepository.findById(id).orElseThrow();
        switch (feedItemToProcess.getProcessState()) {
            case ProcessState.IN_PROGRESS:
                if (!patchFeedItemToProcessDto.getProcessState().equals(ProcessState.ERROR.name())
                && !patchFeedItemToProcessDto.getProcessState().equals(ProcessState.NEW.name())) {
                    throw new IllegalArgumentException("Cannot change state from IN_PROGRESS to " + patchFeedItemToProcessDto.getProcessState());
                }
                break;
            case ProcessState.NEW:
            case ProcessState.DONE:
            case ProcessState.ERROR:
            case ProcessState.FAILED:
                throw new IllegalArgumentException("Cannot change state of " + feedItemToProcess.getProcessState());
        }
        feedItemToProcess.setUpdatedOn(Instant.now());

        // Handle failure counting
        ProcessState newState = ProcessState.valueOf(patchFeedItemToProcessDto.getProcessState());
        if (newState == ProcessState.ERROR || newState == ProcessState.NEW) {
            feedItemToProcess.setFailureCount(feedItemToProcess.getFailureCount() + 1);

            // If failure count reaches 5, set to FAILED instead
            if (feedItemToProcess.getFailureCount() >= 5) {
                feedItemToProcess.setProcessState(ProcessState.FAILED);
            } else {
                feedItemToProcess.setProcessState(newState);
            }
        } else {
            feedItemToProcess.setProcessState(newState);
        }

        return feedItemToProcessRepository.save(feedItemToProcess);
    }

    public List<String> filterFeedItemToProcess(FilterFeedItemToProcessDto filterFeedItemToProcessDto) {
        List<FeedItemToProcess> all = feedItemToProcessRepository.findAll();
        // return only refId not found in all
        return filterFeedItemToProcessDto.getRefIds().stream()
                .filter(refId -> all.stream().noneMatch(feedItemToProcess -> feedItemToProcess.getRefId().equals(refId)))
                .toList();
    }
}
