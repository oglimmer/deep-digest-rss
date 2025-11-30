/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import de.oglimmer.news.db.User;
import de.oglimmer.news.service.UserService;
import de.oglimmer.news.web.AuthController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/**
 * this is an adoption of
 * org.springframework.security.web.authentication.www.BasicAuthenticationConverter
 */
@Setter
public class CookieAuthAuthenticationConverter implements AuthenticationConverter {

  private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
      new WebAuthenticationDetailsSource();

  private UserService userService;

  @Override
  public UsernamePasswordAuthenticationToken convert(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    String requestParameter =
        Arrays.stream(request.getCookies())
            .filter(c -> c.getName().equals(AuthController.AUTH))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    if (requestParameter == null) {
      return null;
    }
    requestParameter = requestParameter.trim();
    int delim = requestParameter.indexOf(":");
    if (delim == -1) {
      throw new BadCredentialsException("Invalid cookie token");
    }
    String[] token = requestParameter.split(":");
    String email = token[0];
    String authToken = token[1];
    User user = userService.authenticateUserAgainstToken(email, authToken);
    if (user == null) {
      throw new BadCredentialsException("Invalid cookie token");
    }
    List<GrantedAuthority> authorities =
        List.of(
            new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
    UsernamePasswordAuthenticationToken result =
        UsernamePasswordAuthenticationToken.authenticated(email, authToken, authorities);
    result.setDetails(this.authenticationDetailsSource.buildDetails(request));
    return result;
  }
}
