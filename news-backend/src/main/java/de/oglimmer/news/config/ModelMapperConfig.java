package de.oglimmer.news.config;

import de.oglimmer.news.db.News;
import de.oglimmer.news.web.dto.CreateNewsDto;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class ModelMapperConfig {


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        confNews(modelMapper);
        modelMapper.validate();
        return modelMapper;
    }

    private void confNews(ModelMapper modelMapper) {

        modelMapper.addMappings(new PropertyMap<CreateNewsDto, News>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getCreatedOn());
            }
        });
    }

}
