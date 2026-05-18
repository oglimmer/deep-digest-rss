/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Eagerly resolves the CSRF token on every request so the XSRF-TOKEN cookie is set before the SPA
 * makes any mutating call. Without this, Spring only issues the cookie lazily, which means the
 * first POST after a fresh page load misses the token.
 */
@Component
public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (token != null) {
      token.getToken();
    }
    chain.doFilter(request, response);
  }
}
