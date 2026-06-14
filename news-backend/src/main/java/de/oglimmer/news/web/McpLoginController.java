/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the themed, server-rendered login page used by the MCP OAuth authorization_code flow (see
 * {@code McpOAuthSecurityConfig}). The page itself reads the {@code error} query parameter directly
 * in the template; this controller only maps the route to the {@code login} template.
 */
@Controller
public class McpLoginController {

  @GetMapping("/login")
  public String login() {
    return "login";
  }
}
