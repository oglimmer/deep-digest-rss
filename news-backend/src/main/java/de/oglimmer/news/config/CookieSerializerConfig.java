/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CookieSerializerConfig {

  @Bean
  public CookieSerializer cookieSerializer(
      @Value("${app.cookie-secure:true}") boolean cookieSecure) {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName("DDRSS_SESSION");
    serializer.setUseHttpOnlyCookie(true);
    serializer.setUseSecureCookie(cookieSecure);
    serializer.setSameSite("Lax");
    serializer.setCookiePath("/");
    return serializer;
  }
}
