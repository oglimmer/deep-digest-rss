/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

import de.oglimmer.news.config.auth.AbsoluteSessionTimeoutFilter;
import de.oglimmer.news.config.auth.ApiKeyAuthFilter;
import de.oglimmer.news.config.auth.CsrfTokenResponseHeaderBindingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private final AbsoluteSessionTimeoutFilter absoluteSessionTimeoutFilter;
  private final ApiKeyAuthFilter apiKeyAuthFilter;
  private final CsrfTokenResponseHeaderBindingFilter csrfTokenResponseHeaderBindingFilter;
  private final CorsConfigurationSource corsConfigurationSource;
  private final boolean csrfEnabled;

  public SecurityConfiguration(
      AbsoluteSessionTimeoutFilter absoluteSessionTimeoutFilter,
      ApiKeyAuthFilter apiKeyAuthFilter,
      CsrfTokenResponseHeaderBindingFilter csrfTokenResponseHeaderBindingFilter,
      CorsConfigurationSource corsConfigurationSource,
      @Value("${app.security.csrf-enabled:false}") boolean csrfEnabled) {
    this.absoluteSessionTimeoutFilter = absoluteSessionTimeoutFilter;
    this.apiKeyAuthFilter = apiKeyAuthFilter;
    this.csrfTokenResponseHeaderBindingFilter = csrfTokenResponseHeaderBindingFilter;
    this.corsConfigurationSource = corsConfigurationSource;
    this.csrfEnabled = csrfEnabled;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  // Lowest precedence: catch-all chain for the app's own endpoints (/api/v1, actuator, swagger).
  // The MCP/OAuth chains in McpOAuthSecurityConfig have explicit higher-priority securityMatchers
  // and are consulted first, so this chain only ever sees the remaining requests.
  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .headers(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        // Without anonymous: no-auth → AuthenticationException → 401 via the entry point below.
        // Authed-but-missing-role → AccessDeniedException → default 403 handler.
        .anonymous(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .addFilterBefore(absoluteSessionTimeoutFilter, SecurityContextHolderFilter.class)
        .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            authorizeRequests ->
                authorizeRequests
                    // K8s probes — unauthenticated, no internal state leaked.
                    .requestMatchers("/actuator/health/liveness", "/actuator/health/readiness")
                    .permitAll()
                    .requestMatchers("/actuator/**", "/actuator")
                    .hasRole("ADMIN")
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/**")
                    .authenticated()
                    .requestMatchers(POST, "/api/v1/feed")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/feed-item-to-process")
                    .hasRole("ADMIN")
                    .requestMatchers(POST, "/api/v1/news")
                    .hasRole("ADMIN")
                    .requestMatchers(POST, "/api/v1/daily-digest")
                    .hasRole("ADMIN")
                    .requestMatchers(POST, "/api/v1/news/*/vote")
                    .hasRole("USER")
                    .requestMatchers(PATCH, "/api/v1/tag-group")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/**")
                    .hasRole("READONLY")
                    .anyRequest()
                    .permitAll());

    if (csrfEnabled) {
      CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
      handler.setCsrfRequestAttributeName(null); // resolve token eagerly (BREACH mitigation)
      http.csrf(
              csrf ->
                  csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                      .csrfTokenRequestHandler(handler)
                      .ignoringRequestMatchers("/api/v1/auth/login", "/api/v1/auth/register")
                      // Stateless API-key callers carry no session and present a custom
                      // header, which is itself sufficient CSRF mitigation.
                      .ignoringRequestMatchers(
                          request -> request.getHeader(ApiKeyAuthFilter.HEADER) != null))
          .addFilterAfter(csrfTokenResponseHeaderBindingFilter, CsrfFilter.class);
    } else {
      http.csrf(AbstractHttpConfigurer::disable);
    }

    return http.build();
  }
}
