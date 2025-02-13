package de.oglimmer.news.service;

import de.oglimmer.news.db.Feed;
import de.oglimmer.news.db.FeedRepository;
import de.oglimmer.news.web.dto.PatchFeedDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@AllArgsConstructor
@Transactional
public class FeedService {

    private FeedRepository feedRepository;

    public Collection<Feed> Feeds() {
        return feedRepository.findAll();
    }

    public Feed updateFeed(Long id, PatchFeedDto feedDto) {
        Feed feed = feedRepository.findById(id).orElseThrow();
        feed.setCookie(feedDto.getCookie());
        return feed;
    }
}
