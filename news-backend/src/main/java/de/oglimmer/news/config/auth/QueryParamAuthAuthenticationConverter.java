/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;

/**
 * this is an adoption of
 * org.springframework.security.web.authentication.www.BasicAuthenticationConverter
 */
public class QueryParamAuthAuthenticationConverter implements AuthenticationConverter {

  public static final String AUTHENTICATION_QUERY_NAME = "auth";

  private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

  private Charset credentialsCharset = StandardCharsets.UTF_8;

  public QueryParamAuthAuthenticationConverter() {
    this(new WebAuthenticationDetailsSource());
  }

  public QueryParamAuthAuthenticationConverter(
      AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
    this.authenticationDetailsSource = authenticationDetailsSource;
  }

  public Charset getCredentialsCharset() {
    return this.credentialsCharset;
  }

  public void setCredentialsCharset(Charset credentialsCharset) {
    this.credentialsCharset = credentialsCharset;
  }

  public AuthenticationDetailsSource<HttpServletRequest, ?> getAuthenticationDetailsSource() {
    return this.authenticationDetailsSource;
  }

  public void setAuthenticationDetailsSource(
      AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
    Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
    this.authenticationDetailsSource = authenticationDetailsSource;
  }

  @Override
  public UsernamePasswordAuthenticationToken convert(HttpServletRequest request) {
    String requestParameter = request.getParameter(AUTHENTICATION_QUERY_NAME);
    if (requestParameter == null) {
      return null;
    }
    requestParameter = requestParameter.trim();
    byte[] base64Token = requestParameter.getBytes(StandardCharsets.UTF_8);
    byte[] decoded = decode(base64Token);
    String token = new String(decoded, getCredentialsCharset(request));
    int delim = token.indexOf(":");
    if (delim == -1) {
      throw new BadCredentialsException("Invalid basic authentication token");
    }
    UsernamePasswordAuthenticationToken result =
        UsernamePasswordAuthenticationToken.unauthenticated(
            token.substring(0, delim), token.substring(delim + 1));
    result.setDetails(this.authenticationDetailsSource.buildDetails(request));
    return result;
  }

  private byte[] decode(byte[] base64Token) {
    try {
      return Base64.getDecoder().decode(base64Token);
    } catch (IllegalArgumentException ex) {
      throw new BadCredentialsException("Failed to decode basic authentication token");
    }
  }

  protected Charset getCredentialsCharset(HttpServletRequest request) {
    return getCredentialsCharset();
  }
}
