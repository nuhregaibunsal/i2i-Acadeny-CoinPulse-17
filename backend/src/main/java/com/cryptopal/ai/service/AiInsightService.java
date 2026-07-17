package com.cryptopal.ai.service;

import com.cryptopal.ai.client.GeminiClient;
import com.cryptopal.ai.dto.AiQueryRequest;
import com.cryptopal.ai.dto.AiQueryResponse;
import com.cryptopal.ai.exception.LlmUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiInsightService {

    private static final Logger log = LoggerFactory.getLogger(AiInsightService.class);

    private final AiContextBuilder contextBuilder;
    private final GeminiClient geminiClient;

    public AiInsightService(AiContextBuilder contextBuilder, GeminiClient geminiClient) {
        this.contextBuilder = contextBuilder;
        this.geminiClient = geminiClient;
    }

    @CircuitBreaker(name = "gemini", fallbackMethod = "fallback")
    @Retry(name = "gemini", fallbackMethod = "fallback")
    public AiQueryResponse answer(String username, Long userId, AiQueryRequest request) {
        String prompt = contextBuilder.build(username, userId, request.question());
        return new AiQueryResponse(geminiClient.generate(prompt));
    }

    private AiQueryResponse fallback(String username, Long userId, AiQueryRequest request, Throwable throwable) {
        log.warn("Gemini call failed for user {}: {}", username, throwable.toString());
        throw new LlmUnavailableException(
                "The AI assistant is temporarily unavailable. Please try again in a moment.");
    }
}
