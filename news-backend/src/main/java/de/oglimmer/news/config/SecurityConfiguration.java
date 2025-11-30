/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

import de.oglimmer.news.config.auth.QueryParamAuthConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

  private NewsConfiguration newsConfiguration;

  //    @Bean
  //    public UserDetailsService userDetailsService(@Value("${auth.write-user}") String writeUser,
  //                                                 @Value("${auth.write-password}") String
  // writePassword,
  //                                                 @Value("${auth.actuator-user}") String
  // actuatorUser,
  //                                                 @Value("${auth.actuator-password}") String
  // actuatorPassword,
  //                                                 @Value("${auth.swagger-user}") String
  // swaggerUser,
  //                                                 @Value("${auth.swagger-password}") String
  // swaggerPassword,
  //                                                 PasswordEncoder passwordEncoder) {
  //        UserDetails read = User.builder()
  //                .username("read")
  //                .password(passwordEncoder.encode("read"))
  //                .roles("ANONYMOUS")
  //                .build();
  //        UserDetails write = User.builder()
  //                .username(writeUser)
  //                .password(passwordEncoder.encode(writePassword))
  //                .roles("ADMIN", "USER", "ANONYMOUS")
  //                .build();
  //        UserDetails acuator = User.builder()
  //                .username(actuatorUser)
  //                .password(passwordEncoder.encode(actuatorPassword))
  //                .roles("ADMIN", "USER", "ANONYMOUS")
  //                .build();
  //        UserDetails swagger = User.builder()
  //                .username(swaggerUser)
  //                .password(passwordEncoder.encode(swaggerPassword))
  //                .roles("ADMIN", "USER", "ANONYMOUS")
  //                .build();
  //        return new InMemoryUserDetailsManager(write, read, acuator, swagger);
  //    }

  //    @Bean
  //    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
  //        TokenBasedRememberMeServices.RememberMeTokenAlgorithm encodingAlgorithm =
  // TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;
  //        TokenBasedRememberMeServices rememberMe = new
  // TokenBasedRememberMeServices(newsConfiguration.getTokenKey(), userDetailsService,
  // encodingAlgorithm);
  //
  // rememberMe.setMatchingAlgorithm(TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256);
  //        rememberMe.setAlwaysRemember(true);
  //        return rememberMe;
  //    }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http /*, RememberMeServices rememberMeServices, UserService userService*/)
      throws Exception {
    return http.sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/actuator/**", "/actuator")
                    .hasRole("ADMIN")
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers(POST, "/api/v1/feed")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/feed-item-to-process")
                    .hasRole("ADMIN")
                    .requestMatchers(POST, "/api/v1/news")
                    .hasRole("ADMIN")
                    .requestMatchers(POST, "/api/v1/news/*/vote")
                    .hasRole("USER")
                    .requestMatchers(PATCH, "/api/v1/tag-group")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/v1/**")
                    .hasRole("READONLY")
                    .anyRequest()
                    .permitAll() // always end with permitAll otherwise exceptions are always
            // converted to 401
            )
        //                .rememberMe((remember) -> remember
        //                        .rememberMeServices(rememberMeServices)
        //                )
        .httpBasic(Customizer.withDefaults())
        .with(new QueryParamAuthConfigurer<>(), Customizer.withDefaults())
        //                .with(new CookieAuthConfigurer<>(), (c) -> {
        //                    c.userService(userService);
        //                })
        .build();
  }
}
