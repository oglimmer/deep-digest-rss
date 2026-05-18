/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  private final ProxyManager<byte[]> proxyManager;
  private final Supplier<BucketConfiguration> emailBucketConfig;
  private final Supplier<BucketConfiguration> ipBucketConfig;

  public RateLimitService(
      @Nullable ProxyManager<byte[]> proxyManager,
      @Value("${app.rate-limit.login-per-email-per-15m:5}") long emailLimit,
      @Value("${app.rate-limit.login-per-ip-per-15m:20}") long ipLimit) {
    this.proxyManager = proxyManager;
    this.emailBucketConfig =
        () ->
            BucketConfiguration.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(emailLimit)
                        .refillGreedy(emailLimit, Duration.ofMinutes(15))
                        .build())
                .build();
    this.ipBucketConfig =
        () ->
            BucketConfiguration.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(ipLimit)
                        .refillGreedy(ipLimit, Duration.ofMinutes(15))
                        .build())
                .build();
  }

  public boolean tryConsumeForEmail(String email) {
    return tryConsume("login:email:" + email.toLowerCase(), emailBucketConfig);
  }

  public boolean tryConsumeForIp(String ip) {
    return tryConsume("login:ip:" + ip, ipBucketConfig);
  }

  public void resetForEmail(String email) {
    if (proxyManager == null) {
      return;
    }
    proxyManager.removeProxy(
        ("login:email:" + email.toLowerCase()).getBytes(StandardCharsets.UTF_8));
  }

  private boolean tryConsume(String key, Supplier<BucketConfiguration> config) {
    if (proxyManager == null) {
      // Redis unavailable — fail open. Logged at startup via RateLimitConfiguration.
      return true;
    }
    BucketProxy bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), config);
    return bucket.tryConsume(1);
  }
}
