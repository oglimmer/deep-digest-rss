/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth.oauth;

import static org.springaicommunity.mcp.security.authorizationserver.config.McpAuthorizationServerConfigurer.mcpAuthorizationServer;
import static org.springaicommunity.mcp.security.server.config.McpServerOAuth2Configurer.mcpServerOAuth2;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security filter chains for the embedded MCP OAuth 2.1 endpoints, layered ahead of the existing
 * app chain ({@code SecurityConfiguration}, lowest precedence). Each chain has an explicit {@link
 * Order} and {@code securityMatcher} so the app's API-key/session behaviour for {@code /api/v1/**}
 * is untouched.
 *
 * <ol>
 *   <li>(1) Authorization Server — token issuance, Dynamic Client Registration (RFC 7591) and
 *       discovery metadata, via the {@code mcp-security} configurer. Unauthenticated browser
 *       requests are redirected to the login page.
 *   <li>(2) MCP resource server — validates bearer JWTs for {@code /mcp/**} and serves the
 *       protected-resource metadata; 401s carry a {@code WWW-Authenticate} header per the MCP
 *       authorization spec.
 *   <li>(3) Login — a form-login page (reusing {@code NewsUserDetailsService}) used by the
 *       authorization_code flow so an end user can sign in during authorization.
 * </ol>
 */
@Configuration
@EnableWebSecurity
public class McpOAuthSecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource mcpCorsConfigurationSource,
      @Value("${app.mcp.dynamic-client-registration-enabled:true}") boolean dcrEnabled)
      throws Exception {
    http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .cors(cors -> cors.configurationSource(mcpCorsConfigurationSource))
        .with(
            mcpAuthorizationServer(),
            mcp -> {
              mcp.dynamicClientRegistration(dcrEnabled);
              mcp.authorizationServer(
                  authzServer ->
                      http.securityMatcher(
                          new OrRequestMatcher(
                              authzServer.getEndpointsMatcher(),
                              PathPatternRequestMatcher.withDefaults()
                                  .matcher("/.well-known/openid-configuration"))));
            })
        .exceptionHandling(
            ex ->
                ex.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"), htmlRequestMatcher()));
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain mcpResourceServerSecurityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource mcpCorsConfigurationSource,
      JwtDecoder jwtDecoder,
      @Value("${app.mcp.issuer-uri}") String issuerUri)
      throws Exception {
    http.securityMatcher(
            "/mcp/**",
            "/.well-known/oauth-protected-resource",
            "/.well-known/oauth-protected-resource/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(mcpCorsConfigurationSource))
        .with(mcpServerOAuth2(), mcp -> mcp.authorizationServer(issuerUri).jwtDecoder(jwtDecoder));
    return http.build();
  }

  @Bean
  @Order(3)
  public SecurityFilterChain mcpLoginSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/login", "/login/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .formLogin(form -> form.loginPage("/login").permitAll());
    return http.build();
  }

  /**
   * CORS for the MCP/OAuth endpoints so browser-based MCP clients (e.g. claude.ai, MCP Inspector)
   * can complete discovery, registration and token exchange. Origins default to {@code *} (via
   * allowed-origin <em>patterns</em>, which is compatible with credentials) and can be locked down
   * through {@code app.mcp.cors-allowed-origins}.
   */
  @Bean
  public CorsConfigurationSource mcpCorsConfigurationSource(
      @Value("${app.mcp.cors-allowed-origins:*}") String allowedOriginsCsv) {
    List<String> origins =
        Arrays.stream(allowedOriginsCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(origins);
    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("WWW-Authenticate"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/mcp/**", config);
    source.registerCorsConfiguration("/oauth2/**", config);
    source.registerCorsConfiguration("/connect/**", config);
    source.registerCorsConfiguration("/.well-known/**", config);
    return source;
  }

  private static RequestMatcher htmlRequestMatcher() {
    MediaTypeRequestMatcher matcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
    matcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
    return matcher;
  }
}
