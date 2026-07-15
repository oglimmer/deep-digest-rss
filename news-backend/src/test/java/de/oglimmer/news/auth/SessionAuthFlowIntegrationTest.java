/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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

@SpringBootTest
@Testcontainers
@EnabledIfEnvironmentVariable(
    named = "RUN_INTEGRATION_TESTS",
    matches = "true",
    disabledReason = "Set RUN_INTEGRATION_TESTS=true to run Docker-based integration tests")
@TestPropertySource(
    properties = {"app.cookie-secure=false", "spring.jpa.hibernate.ddl-auto=validate"})
@DirtiesContext
class SessionAuthFlowIntegrationTest {

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
  void login_issues_session_cookie_with_secure_attributes() throws Exception {
    MockMvc mockMvc = mvc();
    String email = "session-test@example.com";
    String password = "hunter22hunter22";

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TestLoginPayload(email, password))))
        .andExpect(status().isOk());

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(new TestLoginPayload(email, password))))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("DDRSS_SESSION"))
            .andExpect(cookie().httpOnly("DDRSS_SESSION", true))
            .andExpect(cookie().path("DDRSS_SESSION", "/"))
            .andReturn();

    Cookie sessionCookie = result.getResponse().getCookie("DDRSS_SESSION");
    assertThat(sessionCookie).isNotNull();
    assertThat(sessionCookie.getValue()).isNotBlank();
    assertThat(sessionCookie.getAttribute("SameSite")).isEqualTo("Lax");

    mockMvc.perform(get("/api/v1/news").cookie(sessionCookie)).andExpect(status().isOk());
  }

  @Test
  void login_with_wrong_password_returns_401_and_no_cookie() throws Exception {
    MockMvc mockMvc = mvc();
    String email = "wrong-pw@example.com";
    String password = "hunter22hunter22";

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TestLoginPayload(email, password))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new TestLoginPayload(email, "not-the-password"))))
        .andExpect(status().isUnauthorized())
        .andExpect(cookie().doesNotExist("DDRSS_SESSION"));
  }

  record TestLoginPayload(String email, String password) {}
}
