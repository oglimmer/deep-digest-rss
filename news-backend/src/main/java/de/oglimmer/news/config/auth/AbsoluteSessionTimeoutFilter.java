/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AbsoluteSessionTimeoutFilter extends OncePerRequestFilter {

  public static final String LOGIN_INSTANT_ATTR = "DDRSS_LOGIN_INSTANT";

  private final long absoluteTimeoutDays;

  public AbsoluteSessionTimeoutFilter(
      @Value("${app.session.absolute-timeout-days:90}") long absoluteTimeoutDays) {
    this.absoluteTimeoutDays = absoluteTimeoutDays;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Object loginInstant = session.getAttribute(LOGIN_INSTANT_ATTR);
      if (loginInstant instanceof Instant instant) {
        if (Duration.between(instant, Instant.now()).toDays() >= absoluteTimeoutDays) {
          session.invalidate();
          SecurityContextHolder.clearContext();
        }
      }
    }
    chain.doFilter(request, response);
  }
}
