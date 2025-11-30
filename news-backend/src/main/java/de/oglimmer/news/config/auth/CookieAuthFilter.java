/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth;

import de.oglimmer.news.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * this is a copy of org.springframework.security.web.authentication.www.BasicAuthenticationFilter
 */
@AllArgsConstructor
public class CookieAuthFilter extends OncePerRequestFilter {

  private SecurityContextHolderStrategy securityContextHolderStrategy =
      SecurityContextHolder.getContextHolderStrategy();

  private AuthenticationEntryPoint authenticationEntryPoint;

  private AuthenticationManager authenticationManager;

  private RememberMeServices rememberMeServices = new NullRememberMeServices();

  private boolean ignoreFailure = false;

  private final CookieAuthAuthenticationConverter authenticationConverter =
      new CookieAuthAuthenticationConverter();

  private SecurityContextRepository securityContextRepository =
      new RequestAttributeSecurityContextRepository();

  public CookieAuthFilter(
      AuthenticationManager authenticationManager,
      UserService userService,
      AuthenticationEntryPoint authenticationEntryPoint) {
    Assert.notNull(authenticationManager, "authenticationManager cannot be null");
    Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
    this.authenticationManager = authenticationManager;
    this.authenticationEntryPoint = authenticationEntryPoint;
    authenticationConverter.setUserService(userService);
  }

  public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
    Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
    this.securityContextRepository = securityContextRepository;
  }

  @Override
  public void afterPropertiesSet() {
    Assert.notNull(this.authenticationManager, "An AuthenticationManager is required");
    if (!isIgnoreFailure()) {
      Assert.notNull(this.authenticationEntryPoint, "An AuthenticationEntryPoint is required");
    }
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      Authentication authentication = this.authenticationConverter.convert(request);
      if (authentication == null) {
        this.logger.trace(
            "Did not process authentication request since failed to find username and password in Basic Authorization header");
        chain.doFilter(request, response);
        return;
      }
      if (authentication.isAuthenticated()) {
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        this.securityContextHolderStrategy.setContext(context);
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authentication));
        }
        this.rememberMeServices.loginSuccess(request, response, authentication);
        this.securityContextRepository.saveContext(context, request, response);
        onSuccessfulAuthentication(request, response, authentication);
      }
    } catch (AuthenticationException ex) {
      this.securityContextHolderStrategy.clearContext();
      this.logger.debug("Failed to process authentication request", ex);
      this.rememberMeServices.loginFail(request, response);
      onUnsuccessfulAuthentication(request, response, ex);
      if (this.ignoreFailure) {
        chain.doFilter(request, response);
      } else {
        this.authenticationEntryPoint.commence(request, response, ex);
      }
      return;
    }

    chain.doFilter(request, response);
  }

  protected void onSuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, Authentication authResult)
      throws IOException {}

  protected void onUnsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException {}

  protected AuthenticationEntryPoint getAuthenticationEntryPoint() {
    return this.authenticationEntryPoint;
  }

  protected AuthenticationManager getAuthenticationManager() {
    return this.authenticationManager;
  }

  protected boolean isIgnoreFailure() {
    return this.ignoreFailure;
  }

  public void setSecurityContextHolderStrategy(
      SecurityContextHolderStrategy securityContextHolderStrategy) {
    Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
    this.securityContextHolderStrategy = securityContextHolderStrategy;
  }

  public void setAuthenticationDetailsSource(
      AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
    authenticationConverter.setAuthenticationDetailsSource(authenticationDetailsSource);
  }

  public void setRememberMeServices(RememberMeServices rememberMeServices) {
    Assert.notNull(rememberMeServices, "rememberMeServices cannot be null");
    this.rememberMeServices = rememberMeServices;
  }
}
