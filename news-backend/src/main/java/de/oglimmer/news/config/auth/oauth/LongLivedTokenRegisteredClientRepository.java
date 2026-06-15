/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth.oauth;

import java.time.Duration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/**
 * Wraps a {@link RegisteredClientRepository} so every client — including those created at runtime
 * via Dynamic Client Registration — gets long-lived, rotating refresh tokens.
 *
 * <p>Spring Authorization Server's default {@link TokenSettings} expire the refresh token after 60
 * minutes, which is what made MCP clients (e.g. claude.ai) lose their connection and prompt the
 * user to re-authenticate "after some time". We override the refresh-token lifetime to a
 * configurable window (default 180 days, comfortably over the required 3 months) and enable refresh
 * token rotation ({@code reuseRefreshTokens(false)}) so each refresh resets that window — an
 * actively used client therefore never needs to re-authenticate.
 *
 * <p>Normalisation happens both on {@code save} (new registrations are persisted with the long
 * lifetime) and on read ({@code findById}/{@code findByClientId}), so clients registered before
 * this change also benefit without any database migration. Patching on read is safe and sufficient
 * because the framework resolves token lifetimes from the {@link RegisteredClient} returned here at
 * token-issuance time.
 */
public class LongLivedTokenRegisteredClientRepository implements RegisteredClientRepository {

  private final RegisteredClientRepository delegate;
  private final Duration accessTokenTtl;
  private final Duration refreshTokenTtl;

  public LongLivedTokenRegisteredClientRepository(
      RegisteredClientRepository delegate, Duration accessTokenTtl, Duration refreshTokenTtl) {
    this.delegate = delegate;
    this.accessTokenTtl = accessTokenTtl;
    this.refreshTokenTtl = refreshTokenTtl;
  }

  @Override
  public void save(RegisteredClient registeredClient) {
    delegate.save(withLongLivedTokens(registeredClient));
  }

  @Override
  public RegisteredClient findById(String id) {
    return withLongLivedTokens(delegate.findById(id));
  }

  @Override
  public RegisteredClient findByClientId(String clientId) {
    return withLongLivedTokens(delegate.findByClientId(clientId));
  }

  private RegisteredClient withLongLivedTokens(RegisteredClient client) {
    if (client == null) {
      return null;
    }
    TokenSettings current = client.getTokenSettings();
    TokenSettings updated =
        TokenSettings.withSettings(current.getSettings())
            .accessTokenTimeToLive(accessTokenTtl)
            .refreshTokenTimeToLive(refreshTokenTtl)
            .reuseRefreshTokens(false)
            .build();
    if (updated.getSettings().equals(current.getSettings())) {
      return client; // already normalised — avoid a needless rebuild
    }
    return RegisteredClient.from(client).tokenSettings(updated).build();
  }
}
