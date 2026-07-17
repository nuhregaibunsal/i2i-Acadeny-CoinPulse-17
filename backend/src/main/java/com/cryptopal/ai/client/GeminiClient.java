package com.cryptopal.ai.client;

import com.cryptopal.ai.config.GeminiProperties;
import com.cryptopal.ai.exception.LlmUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private final GeminiProperties properties;
    private final RestClient restClient;

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public String generate(String prompt) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new LlmUnavailableException("Gemini API key is not configured");
        }

        JsonNode response = restClient.post()
                .uri("/models/{model}:generateContent?key={key}", properties.model(), properties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GeminiRequest(prompt))
                .retrieve()
                .body(JsonNode.class);

        return extractText(response);
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new LlmUnavailableException("Empty response from Gemini");
        }
        JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text.isMissingNode() || text.asText().isBlank()) {
            throw new LlmUnavailableException("Gemini returned no usable content");
        }
        return text.asText();
    }
}
