/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RateLimitConfiguration {

  @Bean(destroyMethod = "shutdown")
  public RedisClient bucket4jRedisClient(
      @Value("${spring.data.redis.host:localhost}") String host,
      @Value("${spring.data.redis.port:6379}") int port) {
    log.info("Initializing Bucket4j Lettuce client against redis://{}:{}", host, port);
    return RedisClient.create(RedisURI.create(host, port));
  }

  @Bean
  public ProxyManager<byte[]> bucket4jProxyManager(RedisClient bucket4jRedisClient) {
    return LettuceBasedProxyManager.builderFor(bucket4jRedisClient)
        .withExpirationStrategy(
            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                Duration.ofMinutes(30)))
        .build();
  }
}
