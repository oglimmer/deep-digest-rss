/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AbsoluteSessionTimeoutFilterTest {

  @Test
  void invalidates_session_when_login_instant_older_than_timeout() throws Exception {
    AbsoluteSessionTimeoutFilter filter = new AbsoluteSessionTimeoutFilter(90);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(
        AbsoluteSessionTimeoutFilter.LOGIN_INSTANT_ATTR, Instant.now().minus(91, ChronoUnit.DAYS));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();
    SecurityContextHolder.getContext()
        .setAuthentication(UsernamePasswordAuthenticationToken.authenticated("u", null, null));

    filter.doFilter(request, response, chain);

    assertThat(session.isInvalid()).isTrue();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void leaves_session_alone_when_login_instant_within_timeout() throws Exception {
    AbsoluteSessionTimeoutFilter filter = new AbsoluteSessionTimeoutFilter(90);
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(
        AbsoluteSessionTimeoutFilter.LOGIN_INSTANT_ATTR, Instant.now().minus(1, ChronoUnit.DAYS));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(session.isInvalid()).isFalse();
  }

  @Test
  void leaves_session_alone_when_no_login_instant_attribute() throws Exception {
    AbsoluteSessionTimeoutFilter filter = new AbsoluteSessionTimeoutFilter(90);
    MockHttpSession session = new MockHttpSession();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(session.isInvalid()).isFalse();
  }

  @Test
  void noop_when_no_session_exists() throws Exception {
    AbsoluteSessionTimeoutFilter filter = new AbsoluteSessionTimeoutFilter(90);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertThat(request.getSession(false)).isNull();
  }
}
