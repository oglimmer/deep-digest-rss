/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import de.oglimmer.news.db.Role;
import de.oglimmer.news.db.RoleRepository;
import de.oglimmer.news.db.User;
import de.oglimmer.news.db.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

  private static final List<String> DEFAULT_ROLES = List.of("USER", "READONLY");

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public User registerUser(String email, String password) {
    String encodedPassword = passwordEncoder.encode(password);
    Set<Role> roles = new HashSet<>();
    for (String name : DEFAULT_ROLES) {
      roleRepository.findByName(name).ifPresent(roles::add);
    }
    User user =
        User.builder()
            .email(email)
            .password(encodedPassword)
            .timezone("Europe/Berlin")
            .roles(roles)
            .build();
    return userRepository.save(user);
  }

  public User authenticateUser(String email, String password) {
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
      return null;
    }
    return user;
  }
}
