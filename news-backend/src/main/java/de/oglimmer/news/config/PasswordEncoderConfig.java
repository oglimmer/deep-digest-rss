/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The PasswordEncoder lives in its own configuration class so it can be injected into filters that
 * SecurityConfiguration itself depends on (e.g. ApiKeyAuthFilter) without creating a bean-creation
 * cycle.
 */
@Configuration
public class PasswordEncoderConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
