/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import de.oglimmer.news.db.ApiKey;
import de.oglimmer.news.db.ApiKeyRepository;
import de.oglimmer.news.db.User;
import de.oglimmer.news.db.UserRepository;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupPasswordSyncer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final ApiKeyRepository apiKeyRepository;
  private final PasswordEncoder passwordEncoder;
  private final Map<String, String> userPasswords;
  private final Map<String, String> apiKeyValues;

  public StartupPasswordSyncer(
      UserRepository userRepository,
      ApiKeyRepository apiKeyRepository,
      PasswordEncoder passwordEncoder,
      @Value("${AUTH_WRITE_PASSWORD:}") String writePassword,
      @Value("${AUTH_ACTUATOR_PASSWORD:}") String actuatorPassword,
      @Value("${AUTH_SWAGGER_PASSWORD:}") String swaggerPassword,
      @Value("${SCRAPER_API_KEY:}") String scraperApiKey) {
    this.userRepository = userRepository;
    this.apiKeyRepository = apiKeyRepository;
    this.passwordEncoder = passwordEncoder;

    this.userPasswords = new LinkedHashMap<>();
    this.userPasswords.put("write", writePassword);
    this.userPasswords.put("actuator", actuatorPassword);
    this.userPasswords.put("swagger", swaggerPassword);

    this.apiKeyValues = new LinkedHashMap<>();
    this.apiKeyValues.put("scraper", scraperApiKey);
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    userPasswords.forEach(this::syncUserPassword);
    apiKeyValues.forEach(this::syncApiKey);
  }

  private void syncUserPassword(String email, String plaintext) {
    if (plaintext == null || plaintext.isBlank()) {
      log.debug("No env password set for service account '{}', leaving placeholder hash", email);
      return;
    }
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      log.warn("Service account '{}' not found in DB; skipping password sync", email);
      return;
    }
    if (passwordEncoder.matches(plaintext, user.getPassword())) {
      return;
    }
    user.setPassword(passwordEncoder.encode(plaintext));
    userRepository.save(user);
    log.info("Synced password for service account '{}'", email);
  }

  private void syncApiKey(String name, String plaintext) {
    if (plaintext == null || plaintext.isBlank()) {
      log.debug("No env value set for api key '{}', leaving placeholder hash", name);
      return;
    }
    ApiKey key = apiKeyRepository.findByName(name).orElse(null);
    if (key == null) {
      log.warn("Api key row '{}' not found in DB; skipping sync", name);
      return;
    }
    if (passwordEncoder.matches(plaintext, key.getKeyHash())) {
      return;
    }
    key.setKeyHash(passwordEncoder.encode(plaintext));
    apiKeyRepository.save(key);
    log.info("Synced api key '{}'", name);
  }
}
