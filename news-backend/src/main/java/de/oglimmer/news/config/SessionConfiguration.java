/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 2_592_000)
public class SessionConfiguration {

  @Bean
  public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
    return new GenericJackson2JsonRedisSerializer(mapper);
  }
}
