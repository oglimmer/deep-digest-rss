/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.User;
import de.oglimmer.news.db.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Random;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User registerUser(String email, String password) {
    String encodedPassword = passwordEncoder.encode(password);
    User user =
        User.builder().email(email).password(encodedPassword).timezone("Europe/Berlin").build();
    return userRepository.save(user);
  }

  public User authenticateUserAgainstToken(String email, String authToken) {
    User user = userRepository.findByEmail(email).orElseThrow();
    if (user.getAuthToken().equals(authToken)
        && user.getAuthTokenValidUntil().isAfter(Instant.now())) {
      return user;
    }
    return null;
  }

  public User authenticateUser(String email, String password) {
    User user = userRepository.findByEmail(email).orElseThrow();
    if (passwordEncoder.matches(password, user.getPassword())) {
      if (user.getAuthToken() != null && user.getAuthTokenValidUntil().isAfter(Instant.now())) {
        return user;
      }
      long token = new Random().nextLong();
      user.setAuthToken(Long.toString(token < 0 ? -token : token));
      user.setAuthTokenValidUntil(Instant.now().plusSeconds(365 * 24 * 60 * 60));
      return user;
    }
    return null;
  }
}
