/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@AllArgsConstructor
@Slf4j
public class AiSummarizationService {

  private static final String SYSTEM_PROMPT =
      "You are a news summarization assistant. Summarize the provided news articles from the last"
          + " 24 hours into a concise daily digest. Group related topics together. Write in the"
          + " same language as the articles. Keep it concise but informative.\n\n"
          + "Format rules (output will be posted to Discord):\n"
          + "- Use ### for category headers (not #### — Discord doesn't support it)\n"
          + "- Use - (dash) for bullet points, not • or *\n"
          + "- Format each item as: - **Title**: One-sentence summary\n"
          + "- Keep each bullet to a single short sentence to avoid line wrapping\n"
          + "- Add a blank line between each category section\n"
          + "- Do not use nested bullet points or indentation";

  private static final String USER_PROMPT =
      "Fasse die folgenden Nachrichtenartikel der letzten 24 Stunden zu einem täglichen Digest"
          + " zusammen.\n\n";

  private final DailyDigestProperties properties;

  public String summarize(String newsContent) {
    String apiKey = properties.getApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("API key is not configured, returning raw content");
      return newsContent;
    }

    return switch (properties.getGenerationEngine()) {
      case "chatgpt" -> callOpenAi(newsContent);
      case "anthropic" -> callAnthropic(newsContent);
      default ->
          throw new IllegalStateException(
              "Unsupported generation engine: " + properties.getGenerationEngine());
    };
  }

  private String callOpenAi(String newsContent) {
    Map<String, Object> request =
        Map.of(
            "model",
            properties.getModel(),
            "messages",
            List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", USER_PROMPT + newsContent)));

    RestClient restClient = RestClient.create();
    Map<?, ?> response =
        restClient
            .post()
            .uri("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + properties.getApiKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Map.class);

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
    @SuppressWarnings("unchecked")
    Map<String, String> message = (Map<String, String>) choices.getFirst().get("message");
    return message.get("content");
  }

  private String callAnthropic(String newsContent) {
    Map<String, Object> request =
        Map.of(
            "model",
            properties.getModel(),
            "max_tokens",
            4096,
            "system",
            SYSTEM_PROMPT,
            "messages",
            List.of(Map.of("role", "user", "content", USER_PROMPT + newsContent)));

    RestClient restClient = RestClient.create();
    Map<?, ?> response =
        restClient
            .post()
            .uri("https://api.anthropic.com/v1/messages")
            .header("x-api-key", properties.getApiKey())
            .header("anthropic-version", "2023-06-01")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(Map.class);

    @SuppressWarnings("unchecked")
    List<Map<String, String>> content = (List<Map<String, String>>) response.get("content");
    return content.getFirst().get("text");
  }
}
