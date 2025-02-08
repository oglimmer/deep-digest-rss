package de.oglimmer.news.rss;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Item;
import de.oglimmer.news.config.NewsConfiguration;
import de.oglimmer.news.db.News;
import de.oglimmer.news.service.NewsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class RssFeedView extends AbstractRssFeedView {

    private NewsService newsService;
    private NewsConfiguration newsConfiguration;

    @Override
    protected void buildFeedMetadata(Map<String, Object> model,
                                     Channel feed, HttpServletRequest request) {
        feed.setTitle("Lesbare Nachrichten");
        feed.setDescription("Einfach mal Nachrichten lesen.");
        feed.setLink(newsConfiguration.getExternalDomain());
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
                                        HttpServletRequest request, HttpServletResponse response) {
        return newsService.getNewsRollingWindow(1).stream().map(this::createItem).toList();
    }

    private Item createItem(News news) {
        Item entryOne = new Item();
        entryOne.setTitle(news.getTitle());
        Content content = new Content();
        content.setType(Content.TEXT);
        content.setValue(news.getText());
        entryOne.setContent(content);
        entryOne.setAuthor(news.getFeed().getTitle());
        entryOne.setLink(news.getUrl());
        entryOne.setPubDate(Date.from(news.getCreatedOn()));
        return entryOne;
    }
}