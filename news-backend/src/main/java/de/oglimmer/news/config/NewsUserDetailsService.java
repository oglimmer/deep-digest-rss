/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import de.oglimmer.news.db.Role;
import de.oglimmer.news.db.User;
import de.oglimmer.news.db.UserRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NewsUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));

    List<SimpleGrantedAuthority> authorities =
        user.getRoles().stream()
            .map(Role::getName)
            .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
            .toList();

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPassword())
        .authorities(authorities)
        .build();
  }
}
