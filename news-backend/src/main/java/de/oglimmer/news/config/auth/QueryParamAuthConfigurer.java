package de.oglimmer.news.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.*;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import java.util.Arrays;
import java.util.Collections;

/**
 * this is a copy of org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
 *
 * @param <B>
 */
public final class QueryParamAuthConfigurer<B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<QueryParamAuthConfigurer<B>, B> {


    private AuthenticationEntryPoint authenticationEntryPoint;

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

    private SecurityContextRepository securityContextRepository;


    public QueryParamAuthConfigurer() {
        this.authenticationEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }


    public QueryParamAuthConfigurer<B> authenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        return this;
    }

    public QueryParamAuthConfigurer<B> authenticationDetailsSource(
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
        return this;
    }


    public QueryParamAuthConfigurer<B> securityContextRepository(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
        return this;
    }

    @Override
    public void init(B http) {
        registerDefaults(http);
    }

    private void registerDefaults(B http) {
        ContentNegotiationStrategy contentNegotiationStrategy = http.getSharedObject(ContentNegotiationStrategy.class);
        if (contentNegotiationStrategy == null) {
            contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
        }
        MediaTypeRequestMatcher restMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy,
                MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA,
                MediaType.TEXT_XML);
        restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        MediaTypeRequestMatcher allMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.ALL);
        allMatcher.setUseEquals(true);
        RequestMatcher notHtmlMatcher = new NegatedRequestMatcher(new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.TEXT_HTML));
        RequestMatcher restNotHtmlMatcher = new AndRequestMatcher(Arrays.asList(notHtmlMatcher, restMatcher));
        RequestMatcher preferredMatcher = new OrRequestMatcher(Arrays.asList(restNotHtmlMatcher, allMatcher));
        registerDefaultEntryPoint(http, preferredMatcher);
        registerDefaultLogoutSuccessHandler(http, preferredMatcher);
    }

    private void registerDefaultEntryPoint(B http, RequestMatcher preferredMatcher) {
        ExceptionHandlingConfigurer<B> exceptionHandling = http.getConfigurer(ExceptionHandlingConfigurer.class);
        if (exceptionHandling == null) {
            return;
        }
        exceptionHandling.defaultAuthenticationEntryPointFor(postProcess(this.authenticationEntryPoint),
                preferredMatcher);
    }

    private void registerDefaultLogoutSuccessHandler(B http, RequestMatcher preferredMatcher) {
        LogoutConfigurer<B> logout = http.getConfigurer(LogoutConfigurer.class);
        if (logout == null) {
            return;
        }
        logout.defaultLogoutSuccessHandlerFor(postProcess(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)), preferredMatcher);
    }

    @Override
    public void configure(B http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        QueryParamAuthFilter queryParamAuthFilter = new QueryParamAuthFilter(authenticationManager, this.authenticationEntryPoint);
        if (this.authenticationDetailsSource != null) {
            queryParamAuthFilter.setAuthenticationDetailsSource(this.authenticationDetailsSource);
        }
        if (this.securityContextRepository != null) {
            queryParamAuthFilter.setSecurityContextRepository(this.securityContextRepository);
        }
        RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);
        if (rememberMeServices != null) {
            queryParamAuthFilter.setRememberMeServices(rememberMeServices);
        }
        queryParamAuthFilter.setSecurityContextHolderStrategy(getSecurityContextHolderStrategy());
        queryParamAuthFilter = postProcess(queryParamAuthFilter);
        http.addFilterBefore(queryParamAuthFilter, BasicAuthenticationFilter.class);
    }

}
