/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth.oauth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

/**
 * JDBC-backed Spring Authorization Server stores plus issuer settings.
 *
 * <p>Dynamic Client Registration creates clients at runtime, so the registered-client,
 * authorization and consent stores are persisted (tables in Flyway migration {@code
 * V18.0.0__add_oauth2_server.sql}) rather than in-memory — otherwise registered MCP clients and
 * in-flight authorizations would be lost on every restart.
 *
 * <p>The issuer is pinned to {@code app.mcp.issuer-uri} so the value advertised in discovery
 * metadata matches the externally reachable URL and the resource server's expected issuer.
 */
@Configuration
public class AuthorizationServerBeans {

  /**
   * Registered-client store wrapped so every client (including Dynamic Client Registration clients
   * created at runtime) issues long-lived, rotating refresh tokens. Without this, the default
   * 60-minute refresh-token lifetime forced MCP clients to re-authenticate after about an hour. See
   * {@link LongLivedTokenRegisteredClientRepository}.
   */
  @Bean
  public RegisteredClientRepository registeredClientRepository(
      JdbcTemplate jdbcTemplate,
      @Value("${app.mcp.token.access-token-ttl:30m}") Duration accessTokenTtl,
      @Value("${app.mcp.token.refresh-token-ttl:180d}") Duration refreshTokenTtl) {
    return new LongLivedTokenRegisteredClientRepository(
        new JdbcRegisteredClientRepository(jdbcTemplate), accessTokenTtl, refreshTokenTtl);
  }

  @Bean
  public OAuth2AuthorizationService authorizationService(
      JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
    return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
  }

  @Bean
  public OAuth2AuthorizationConsentService authorizationConsentService(
      JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
    return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings(
      @Value("${app.mcp.issuer-uri}") String issuerUri) {
    return AuthorizationServerSettings.builder().issuer(issuerUri).build();
  }
}
