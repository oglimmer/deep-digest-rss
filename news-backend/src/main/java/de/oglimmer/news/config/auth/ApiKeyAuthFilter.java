/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import de.oglimmer.news.db.ApiKey;
import de.oglimmer.news.db.ApiKeyRepository;
import de.oglimmer.news.db.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-API-Key";

  private final ApiKeyRepository apiKeyRepository;
  private final PasswordEncoder passwordEncoder;
  private final TaskExecutor taskExecutor;

  public ApiKeyAuthFilter(
      ApiKeyRepository apiKeyRepository,
      PasswordEncoder passwordEncoder,
      @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
    this.apiKeyRepository = apiKeyRepository;
    this.passwordEncoder = passwordEncoder;
    this.taskExecutor = taskExecutor;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String presented = request.getHeader(HEADER);
    if (presented != null && !presented.isBlank()) {
      authenticate(presented);
    }
    chain.doFilter(request, response);
  }

  private void authenticate(String presented) {
    if (SecurityContextHolder.getContext().getAuthentication() != null
        && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
      return;
    }
    List<ApiKey> activeKeys = apiKeyRepository.findAllByRevokedAtIsNull();
    for (ApiKey key : activeKeys) {
      if (passwordEncoder.matches(presented, key.getKeyHash())) {
        AbstractAuthenticationToken auth =
            UsernamePasswordAuthenticationToken.authenticated(
                "api-key:" + key.getName(),
                null,
                key.getRoles().stream()
                    .map(Role::getName)
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);

        Long keyId = key.getId();
        taskExecutor.execute(() -> recordUsage(keyId));
        return;
      }
    }
  }

  private void recordUsage(Long keyId) {
    try {
      apiKeyRepository
          .findById(keyId)
          .ifPresent(
              k -> {
                k.setLastUsedAt(Instant.now());
                apiKeyRepository.save(k);
              });
    } catch (RuntimeException e) {
      log.debug("Failed to record api-key usage for id={}: {}", keyId, e.toString());
    }
  }
}
