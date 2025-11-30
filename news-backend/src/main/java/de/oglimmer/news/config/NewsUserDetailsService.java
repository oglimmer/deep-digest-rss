/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import de.oglimmer.news.db.User;
import de.oglimmer.news.db.UserRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class NewsUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;

  public NewsUserDetailsService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${auth.write-user}") String writeUser,
      @Value("${auth.write-password}") String writePassword,
      @Value("${auth.actuator-user}") String actuatorUser,
      @Value("${auth.actuator-password}") String actuatorPassword,
      @Value("${auth.swagger-user}") String swaggerUser,
      @Value("${auth.swagger-password}") String swaggerPassword) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    UserDetails read =
        org.springframework.security.core.userdetails.User.builder()
            .username("read")
            .password(passwordEncoder.encode("read"))
            .roles("READONLY")
            .build();
    UserDetails write =
        org.springframework.security.core.userdetails.User.builder()
            .username(writeUser)
            .password(passwordEncoder.encode(writePassword))
            .roles("ADMIN", "USER", "READONLY")
            .build();
    UserDetails acuator =
        org.springframework.security.core.userdetails.User.builder()
            .username(actuatorUser)
            .password(passwordEncoder.encode(actuatorPassword))
            .roles("ADMIN", "USER", "READONLY")
            .build();
    UserDetails swagger =
        org.springframework.security.core.userdetails.User.builder()
            .username(swaggerUser)
            .password(passwordEncoder.encode(swaggerPassword))
            .roles("ADMIN", "USER", "READONLY")
            .build();
    userDetailsService = new InMemoryUserDetailsManager(write, read, acuator, swagger);
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    try {
      return userDetailsService.loadUserByUsername(email);
    } catch (UsernameNotFoundException ignored) {
    }
    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    if (user.getAuthTokenValidUntil().isBefore(Instant.now())) {
      throw new CredentialsExpiredException(email);
    }
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(passwordEncoder.encode(user.getAuthToken()))
        .roles("USER", "READONLY")
        .build();
  }
}
