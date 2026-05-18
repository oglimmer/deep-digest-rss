/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOriginsCsv) {
    List<String> origins =
        Arrays.stream(allowedOriginsCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(
        List.of("Content-Type", "Authorization", "X-API-Key", "X-XSRF-TOKEN", "X-Requested-With"));
    config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
