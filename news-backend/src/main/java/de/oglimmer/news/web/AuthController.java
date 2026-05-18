/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import de.oglimmer.news.config.auth.AbsoluteSessionTimeoutFilter;
import de.oglimmer.news.db.Role;
import de.oglimmer.news.db.User;
import de.oglimmer.news.service.RateLimitService;
import de.oglimmer.news.service.UserService;
import de.oglimmer.news.web.dto.AuthMeResponse;
import de.oglimmer.news.web.dto.AuthResponse;
import de.oglimmer.news.web.dto.AuthenticateUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

  // Pre-computed BCrypt hash of an unguessable random string; used to make wrong-email
  // and wrong-password code paths take the same time. Generated once at class load.
  private static final String DUMMY_BCRYPT_HASH =
      "$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhW";

  private final UserService userService;
  private final RateLimitService rateLimitService;
  private final PasswordEncoder passwordEncoder;

  private final SecurityContextHolderStrategy securityContextHolderStrategy =
      SecurityContextHolder.getContextHolderStrategy();
  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @RequestBody AuthenticateUserDto authenticateUserDto,
      HttpServletRequest request,
      HttpServletResponse response) {
    String email = authenticateUserDto.getEmail();
    String clientIp = clientIp(request);

    if (email == null || email.isBlank() || !rateLimitService.tryConsumeForIp(clientIp)) {
      return tooManyRequests();
    }
    if (!rateLimitService.tryConsumeForEmail(email)) {
      return tooManyRequests();
    }

    User user = userService.authenticateUser(email, authenticateUserDto.getPassword());
    if (user == null) {
      // Constant-time path: do an equivalent BCrypt compare against a dummy hash so a
      // missing email isn't visibly faster than a present email with wrong password.
      passwordEncoder.matches(authenticateUserDto.getPassword(), DUMMY_BCRYPT_HASH);
      return ResponseEntity.status(401).build();
    }

    rateLimitService.resetForEmail(email);

    List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();
    Authentication auth =
        UsernamePasswordAuthenticationToken.authenticated(
            user.getEmail(),
            null,
            roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
    SecurityContext context = securityContextHolderStrategy.createEmptyContext();
    context.setAuthentication(auth);
    securityContextHolderStrategy.setContext(context);
    securityContextRepository.saveContext(context, request, response);

    HttpSession session = request.getSession(true);
    session.setAttribute(AbsoluteSessionTimeoutFilter.LOGIN_INSTANT_ATTR, Instant.now());

    AuthResponse authResponse = new AuthResponse();
    authResponse.setEmail(user.getEmail());
    authResponse.setRoles(roles);
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<AuthMeResponse> me() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(401).build();
    }
    List<String> roles =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
            .sorted()
            .toList();
    return ResponseEntity.ok(new AuthMeResponse(auth.getName(), roles));
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@RequestBody AuthenticateUserDto authenticateUserDto) {
    userService.registerUser(authenticateUserDto.getEmail(), authenticateUserDto.getPassword());
    return ResponseEntity.ok().build();
  }

  private static ResponseEntity<AuthResponse> tooManyRequests() {
    return ResponseEntity.status(429).header(HttpHeaders.RETRY_AFTER, "60").build();
  }

  private static String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      int comma = forwarded.indexOf(',');
      return (comma < 0 ? forwarded : forwarded.substring(0, comma)).trim();
    }
    return request.getRemoteAddr();
  }
}
