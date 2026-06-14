/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.config.auth.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Supplies the RSA key the Authorization Server uses to sign MCP access tokens.
 *
 * <p>A configured key ({@code app.mcp.jwk.private-key}/{@code public-key}, base64 DER or PEM) is
 * required in production: without a stable key every restart invalidates all issued tokens. When
 * unset, an ephemeral key is generated and a warning is logged — convenient for local dev only.
 *
 * <p>The {@code kid} is derived from the key thumbprint, so a stable key yields a stable {@code
 * kid} and previously issued tokens keep validating against the published JWK set.
 */
@Slf4j
@Configuration
public class AuthServerKeyConfig {

  @Bean
  public JWKSource<SecurityContext> jwkSource(
      @Value("${app.mcp.jwk.private-key:}") String privateKeyBase64,
      @Value("${app.mcp.jwk.public-key:}") String publicKeyBase64) {
    RSAKey rsaKey =
        (privateKeyBase64.isBlank() || publicKeyBase64.isBlank())
            ? generateEphemeralKey()
            : loadKey(privateKeyBase64, publicKeyBase64);
    return new ImmutableJWKSet<>(new JWKSet(rsaKey));
  }

  /**
   * A {@link JwtDecoder} backed by the local {@link JWKSource}. Used by the MCP resource server so
   * it validates token signatures against the in-process key directly, instead of fetching the
   * issuer's discovery metadata over HTTP — which would deadlock at startup since this same
   * application is both the authorization server and the resource server.
   */
  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    JWSKeySelector<SecurityContext> keySelector =
        new JWSVerificationKeySelector<>(JWSAlgorithm.Family.RSA, jwkSource);
    ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
    jwtProcessor.setJWSKeySelector(keySelector);
    // Issuer/audience are validated by the resource-server configurer, not here.
    jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {});
    return new NimbusJwtDecoder(jwtProcessor);
  }

  private RSAKey generateEphemeralKey() {
    log.warn(
        "app.mcp.jwk.private-key/public-key not set — generating an ephemeral RSA signing key. "
            + "All MCP OAuth tokens become invalid on restart. Configure a persistent key for "
            + "production.");
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      return toRsaKey((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to generate ephemeral RSA key", e);
    }
  }

  private RSAKey loadKey(String privateKeyBase64, String publicKeyBase64) {
    try {
      KeyFactory factory = KeyFactory.getInstance("RSA");
      RSAPrivateKey privateKey =
          (RSAPrivateKey)
              factory.generatePrivate(
                  new PKCS8EncodedKeySpec(Base64.getDecoder().decode(unwrap(privateKeyBase64))));
      RSAPublicKey publicKey =
          (RSAPublicKey)
              factory.generatePublic(
                  new X509EncodedKeySpec(Base64.getDecoder().decode(unwrap(publicKeyBase64))));
      return toRsaKey(publicKey, privateKey);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to load configured RSA JWK from app.mcp.jwk.*", e);
    }
  }

  private static RSAKey toRsaKey(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    try {
      RSAKey withoutKid = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
      return new RSAKey.Builder(withoutKid)
          .keyID(withoutKid.computeThumbprint().toString())
          .build();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to compute JWK thumbprint", e);
    }
  }

  private static String unwrap(String key) {
    return key.replaceAll("-----BEGIN (.*)-----", "")
        .replaceAll("-----END (.*)-----", "")
        .replaceAll("\\s", "");
  }
}
