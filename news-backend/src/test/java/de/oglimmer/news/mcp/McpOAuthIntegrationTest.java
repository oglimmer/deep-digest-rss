/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end checks for the embedded MCP OAuth endpoints: public discovery metadata, the
 * unauthenticated {@code /mcp} 401 challenge, and Dynamic Client Registration (RFC 7591).
 */
@SpringBootTest
@Testcontainers
@EnabledIfEnvironmentVariable(
    named = "RUN_INTEGRATION_TESTS",
    matches = "true",
    disabledReason = "Set RUN_INTEGRATION_TESTS=true to run Docker-based integration tests")
@TestPropertySource(
    properties = {"app.cookie-secure=false", "spring.jpa.hibernate.ddl-auto=validate"})
@DirtiesContext
class McpOAuthIntegrationTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

  @Container
  static final GenericContainer<?> redis =
      new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProps(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @Autowired private WebApplicationContext context;
  @Autowired private ObjectMapper objectMapper;

  private MockMvc mvc() {
    return MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  void protectedResourceMetadata_isPublic() throws Exception {
    mvc()
        .perform(get("/.well-known/oauth-protected-resource"))
        .andExpect(status().isOk())
        .andExpect(
            content ->
                assertThat(content.getResponse().getContentAsString())
                    .contains("authorization_servers"));
  }

  @Test
  void authorizationServerMetadata_advertisesRegistrationEndpoint() throws Exception {
    String body =
        mvc()
            .perform(get("/.well-known/oauth-authorization-server"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(objectMapper.readTree(body).has("registration_endpoint")).isTrue();
  }

  @Test
  void mcpEndpoint_withoutToken_returns401WithWwwAuthenticate() throws Exception {
    mvc()
        .perform(post("/mcp").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(
            content ->
                assertThat(content.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE))
                    .isNotBlank());
  }

  @Test
  void dynamicClientRegistration_returnsClientId() throws Exception {
    String registrationEndpoint =
        objectMapper
            .readTree(
                mvc()
                    .perform(get("/.well-known/oauth-authorization-server"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("registration_endpoint")
            .asText();
    // Endpoint is an absolute URL; reduce to a path MockMvc can dispatch.
    String path = registrationEndpoint.replaceFirst("^https?://[^/]+", "");

    // Public client (PKCE, no secret) as MCP clients such as Claude register. No custom scope is
    // requested: the resource server gates on authentication + audience, not on a named scope.
    String registration =
        """
        {
          "client_name": "integration-test-client",
          "redirect_uris": ["http://localhost/callback"],
          "grant_types": ["authorization_code"],
          "response_types": ["code"],
          "token_endpoint_auth_method": "none"
        }
        """;

    MvcResult result =
        mvc()
            .perform(post(path).contentType(MediaType.APPLICATION_JSON).content(registration))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    assertThat(response.get("client_id").asText()).isNotBlank();
  }
}
