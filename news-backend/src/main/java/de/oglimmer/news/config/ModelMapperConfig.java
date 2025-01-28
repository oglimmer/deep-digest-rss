package de.oglimmer.news.config;

import de.oglimmer.news.db.*;
import de.oglimmer.news.service.InvalidDataException;
import de.oglimmer.news.web.dto.CreateFeedItemToProcessDto;
import de.oglimmer.news.web.dto.CreateNewsDto;
import de.oglimmer.news.web.dto.FeedItemToProcessDto;
import de.oglimmer.news.web.dto.NewsDto;
import lombok.AllArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Configuration
public class ModelMapperConfig {

    private FeedRepository feedRepository;
    private FeedItemToProcessRepository feedItemToProcessRepository;
    private TagsRepository tagsRepository;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        confNews(modelMapper);
        confFeedItemToProcess(modelMapper);
        modelMapper.validate();
        return modelMapper;
    }

    private void confFeedItemToProcess(ModelMapper modelMapper) {
        Converter<Long, Feed> feedConverter = context -> context.getSource() != null ? feedRepository
                .findById(context.getSource())
                .orElseThrow(() -> new InvalidDataException("Feed mit der id " + context.getSource() + " existiert nicht"))
                : null;

        modelMapper.addMappings(new PropertyMap<CreateFeedItemToProcessDto, FeedItemToProcess>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getCreatedOn());
                skip(destination.getUpdatedOn());
                skip(destination.getProcessState());
                using(feedConverter).map(source.getFeedId(), destination.getFeed());
            }
        });
        modelMapper.addMappings(new PropertyMap<FeedItemToProcess, FeedItemToProcessDto>() {
            @Override
            protected void configure() {
            }
        });
    }

    private void confNews(ModelMapper modelMapper) {
        Converter<Long, Feed> feedConverter = context -> context.getSource() != null ? feedRepository
                .findById(context.getSource())
                .orElseThrow(() -> new InvalidDataException("Feed mit der id " + context.getSource() + " existiert nicht"))
                : null;
        Converter<Long, FeedItemToProcess> feedItemToProcessConverter = context -> context.getSource() != null ? feedItemToProcessRepository
                .findById(context.getSource())
                .orElseThrow(() -> new InvalidDataException("FeedItemToProcess mit der id " + context.getSource() + " existiert nicht"))
                : null;
        Converter<String[], List<Tags>> tagsConverter = context -> {
            if (context.getSource() == null) {
                return null;
            }
            return Arrays.stream(context.getSource())
                    .map(tagName -> tagsRepository.findByText(tagName).orElse(Tags.builder().text(tagName).build()))
                    .collect(Collectors.toList());
        };
        Converter<List<Tags>, String[]> tagsConverterBack = context -> {
            if (context.getSource() == null) {
                return null;
            }
            return context.getSource().stream().map(Tags::getText).toArray(String[]::new);
        };

        modelMapper.addMappings(new PropertyMap<CreateNewsDto, News>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getCreatedOn());
                skip(destination.getTitle());
                skip(destination.getUrl());
                using(feedConverter).map(source.getFeedId(), destination.getFeed());
                using(feedItemToProcessConverter).map(source.getOriginalFeedItemId(), destination.getOriginalFeedItem());
                using(tagsConverter).map(source.getTags(), destination.getTags());
            }
        });
        modelMapper.addMappings(new PropertyMap<News, NewsDto>() {
            @Override
            protected void configure() {
                using(tagsConverterBack).map(source.getTags(), destination.getTags());
            }
        });
    }

}
