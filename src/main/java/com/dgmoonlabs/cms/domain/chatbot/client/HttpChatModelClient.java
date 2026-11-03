package com.dgmoonlabs.cms.domain.chatbot.client;

import com.dgmoonlabs.cms.domain.chatbot.constant.ChatbotProvider;
import com.dgmoonlabs.cms.domain.chatbot.dto.ChatbotDtos.ChatMessage;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class HttpChatModelClient implements ChatModelClient {
    private static final int MAX_OUTPUT_TOKENS = 1024;

    private final RestClient restClient;

    public HttpChatModelClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String complete(final ChatbotProvider provider, final String apiKey, final String model, final String system,
                           final List<ChatMessage> messages) {
        try {
            String reply = switch (provider) {
                case OPENAI -> openAi(apiKey, model, system, messages);
                case GEMINI -> gemini(apiKey, model, system, messages);
                case CLAUDE -> claude(apiKey, model, system, messages);
            };
            if (reply == null || reply.isBlank()) {
                throw new BusinessException(ErrorCode.CHATBOT_UPSTREAM_ERROR);
            }
            return reply.strip();
        } catch (RestClientResponseException e) {
            log.warn("Chatbot provider {} answered {}: {}", provider, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.CHATBOT_UPSTREAM_ERROR, e);
        } catch (RestClientException e) {
            log.warn("Chatbot provider {} could not be reached", provider, e);
            throw new BusinessException(ErrorCode.CHATBOT_UPSTREAM_ERROR, e);
        }
    }

    private String openAi(final String apiKey, final String model, final String system, final List<ChatMessage> messages) {
        List<Map<String, String>> body = new ArrayList<>();
        body.add(Map.of("role", "system", "content", system));
        messages.forEach(m -> body.add(Map.of("role", ChatModelClient.roleName(m.role(), "assistant"), "content", m.content())));
        JsonNode response = restClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", model, "messages", body, "max_completion_tokens", MAX_OUTPUT_TOKENS))
                .retrieve()
                .body(JsonNode.class);
        return response == null ? null : response.path("choices").path(0).path("message").path("content").asString(null);
    }

    private String gemini(final String apiKey, final String model, final String system, final List<ChatMessage> messages) {
        List<Map<String, Object>> contents = messages.stream()
                .map(m -> Map.<String, Object>of("role", ChatModelClient.roleName(m.role(), "model"),
                        "parts", List.of(Map.of("text", m.content()))))
                .toList();
        JsonNode response = restClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                        "contents", contents,
                        "generationConfig", Map.of("maxOutputTokens", MAX_OUTPUT_TOKENS)))
                .retrieve()
                .body(JsonNode.class);
        return response == null ? null : joinText(response.path("candidates").path(0).path("content").path("parts"));
    }

    private String claude(final String apiKey, final String model, final String system, final List<ChatMessage> messages) {
        List<Map<String, String>> body = messages.stream()
                .map(m -> Map.of("role", ChatModelClient.roleName(m.role(), "assistant"), "content", m.content()))
                .toList();
        JsonNode response = restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", model, "max_tokens", MAX_OUTPUT_TOKENS, "system", system, "messages", body))
                .retrieve()
                .body(JsonNode.class);
        return response == null ? null : joinText(response.path("content"));
    }

    private static String joinText(final JsonNode blocks) {
        return String.join("", StreamSupport.stream(blocks.spliterator(), false)
                .map(block -> block.path("text").asString(""))
                .toList());
    }
}
