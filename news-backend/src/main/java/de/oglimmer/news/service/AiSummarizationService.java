/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.service;

import java.util.ArrayList;
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

  private static final int MAX_CONTENT_CHARS = 400_000;

  private static final String FINAL_SYSTEM_PROMPT =
      "You are a news summarization assistant. Summarize the provided news articles from the last"
          + " 24 hours into a concise daily digest. Focus only on the most significant and impactful"
          + " stories. Ignore minor updates, routine announcements, repetitive follow-ups, and"
          + " low-importance items. Group related topics together. Write in the same language as the"
          + " articles. Keep it concise but informative.\n\n"
          + "Each article has metadata tags: [Aktuell: Ja/Nein] indicates whether it covers a"
          + " current event, and [Reichweite: ...] indicates the impact scope (global,"
          + " international, europa, deutschland, regional, branche). Use this metadata to:\n"
          + "- Prioritize current events (Aktuell: Ja) over background/opinion pieces\n"
          + "- Lead with global/international stories, then narrow to deutschland/regional\n"
          + "- You may skip non-timely articles with limited scope if space is tight\n\n"
          + "Format rules (output will be posted to Discord):\n"
          + "- Use ### for category headers (not #### — Discord doesn't support it)\n"
          + "- Use - (dash) for bullet points, not • or *\n"
          + "- Format each item as: - **Title**: One-sentence summary\n"
          + "- Keep each bullet to a single short sentence to avoid line wrapping\n"
          + "- Add a blank line between each category section\n"
          + "- Do not use nested bullet points or indentation";

  private static final String BATCH_SYSTEM_PROMPT =
      "You are a news summarization assistant. Summarize the provided news articles into a concise"
          + " bullet-point list. Focus only on the most significant and impactful stories. Ignore"
          + " minor updates, routine announcements, repetitive follow-ups, and low-importance items."
          + " Group related topics together. Write in the same language as the articles. For each"
          + " item write: **Title**: One-sentence summary.\n\n"
          + "Each article has metadata tags: [Aktuell: Ja/Nein] indicates whether it covers a"
          + " current event, and [Reichweite: ...] indicates the impact scope. Prioritize current"
          + " events and broader-scope stories.";

  private static final String MERGE_SYSTEM_PROMPT =
      "You are a news summarization assistant. Merge the following partial news summaries into one"
          + " cohesive daily digest. Remove duplicates, group related topics together. Keep only the"
          + " most significant and impactful stories — drop minor updates, routine announcements,"
          + " and low-importance items. Write in the same language as the summaries. Keep it concise"
          + " but informative. Lead with global/international stories, then narrow to"
          + " deutschland/regional.\n\n"
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

  private static final String MERGE_USER_PROMPT =
      "Fasse die folgenden Teil-Zusammenfassungen zu einem einheitlichen täglichen Digest"
          + " zusammen.\n\n";

  private final DailyDigestProperties properties;

  public String summarize(List<String> articles) {
    String apiKey = properties.getApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("API key is not configured, returning raw content");
      return String.join("\n\n", articles);
    }

    List<List<String>> batches = splitIntoBatches(articles);

    if (batches.size() == 1) {
      log.info("Single batch with {} articles, summarizing directly", articles.size());
      String content = String.join("\n\n---\n\n", batches.getFirst());
      return callAi(FINAL_SYSTEM_PROMPT, USER_PROMPT + content);
    }

    log.info("Splitting {} articles into {} batches", articles.size(), batches.size());
    List<String> batchSummaries = new ArrayList<>();
    for (int i = 0; i < batches.size(); i++) {
      String content = String.join("\n\n---\n\n", batches.get(i));
      log.info(
          "Summarizing batch {}/{} ({} articles)", i + 1, batches.size(), batches.get(i).size());
      String summary = callAi(BATCH_SYSTEM_PROMPT, USER_PROMPT + content);
      batchSummaries.add(summary);
    }

    String mergedContent = String.join("\n\n---\n\n", batchSummaries);
    log.info("Merging {} batch summaries into final digest", batchSummaries.size());
    return callAi(MERGE_SYSTEM_PROMPT, MERGE_USER_PROMPT + mergedContent);
  }

  private List<List<String>> splitIntoBatches(List<String> articles) {
    List<List<String>> batches = new ArrayList<>();
    List<String> currentBatch = new ArrayList<>();
    int currentSize = 0;

    for (String article : articles) {
      if (currentSize + article.length() > MAX_CONTENT_CHARS && !currentBatch.isEmpty()) {
        batches.add(currentBatch);
        currentBatch = new ArrayList<>();
        currentSize = 0;
      }
      currentBatch.add(article);
      currentSize += article.length();
    }

    if (!currentBatch.isEmpty()) {
      batches.add(currentBatch);
    }

    return batches;
  }

  private String callAi(String systemPrompt, String userContent) {
    return switch (properties.getGenerationEngine()) {
      case "chatgpt" -> callOpenAi(systemPrompt, userContent);
      case "anthropic" -> callAnthropic(systemPrompt, userContent);
      default ->
          throw new IllegalStateException(
              "Unsupported generation engine: " + properties.getGenerationEngine());
    };
  }

  private String callOpenAi(String systemPrompt, String userContent) {
    Map<String, Object> request =
        Map.of(
            "model",
            properties.getModel(),
            "messages",
            List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent)));

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

  private String callAnthropic(String systemPrompt, String userContent) {
    Map<String, Object> request =
        Map.of(
            "model",
            properties.getModel(),
            "max_tokens",
            4096,
            "system",
            systemPrompt,
            "messages",
            List.of(Map.of("role", "user", "content", userContent)));

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
