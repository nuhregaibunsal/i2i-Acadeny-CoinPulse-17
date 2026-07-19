package com.cryptopal.ai.service;

import com.cryptopal.ai.client.GeminiClient;
import com.cryptopal.ai.dto.AiQueryRequest;
import com.cryptopal.ai.dto.AiQueryResponse;
import com.cryptopal.ai.exception.LlmUnavailableException;
import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.trading.dto.ConditionalOrderRequest;
import com.cryptopal.trading.dto.OrderRequest;
import com.cryptopal.trading.dto.OrderResponse;
import com.cryptopal.trading.model.OrderSide;
import com.cryptopal.trading.service.ConditionalOrderService;
import com.cryptopal.trading.service.TradingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiInsightService {

    private static final Logger log = LoggerFactory.getLogger(AiInsightService.class);

    private final AiContextBuilder contextBuilder;
    private final GeminiClient geminiClient;
    private final TradingService tradingService;
    private final ConditionalOrderService conditionalOrderService;
    private final MarketPriceCache marketPriceCache;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiInsightService(AiContextBuilder contextBuilder,
                            GeminiClient geminiClient,
                            TradingService tradingService,
                            ConditionalOrderService conditionalOrderService,
                            MarketPriceCache marketPriceCache) {
        this.contextBuilder = contextBuilder;
        this.geminiClient = geminiClient;
        this.tradingService = tradingService;
        this.conditionalOrderService = conditionalOrderService;
        this.marketPriceCache = marketPriceCache;
    }

    @CircuitBreaker(name = "gemini", fallbackMethod = "fallback")
    @Retry(name = "gemini", fallbackMethod = "fallback")
    public AiQueryResponse answer(String username, Long userId, AiQueryRequest request) {
        String prompt = contextBuilder.build(username, userId, request.question());
        String response = geminiClient.generate(prompt);
        JsonNode order = extractOrder(response);
        if (order != null) {
            return new AiQueryResponse(handleOrder(userId, order));
        }
        return new AiQueryResponse(response);
    }

    private JsonNode extractOrder(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(text.substring(start, end + 1));
            if (node.hasNonNull("action") && "order".equalsIgnoreCase(node.get("action").asText())) {
                return node;
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String handleOrder(Long userId, JsonNode order) {
        try {
            String symbol = order.get("symbol").asText().toUpperCase();
            OrderSide side = OrderSide.valueOf(order.get("side").asText().toUpperCase());
            BigDecimal price = marketPriceCache.getPrice(symbol)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown symbol " + symbol));
            String orderType = order.has("orderType") ? order.get("orderType").asText() : "instant";
            boolean conditional = "conditional".equalsIgnoreCase(orderType);

            BigDecimal volume = order.hasNonNull("volume") ? new BigDecimal(order.get("volume").asText()) : null;
            if (volume == null && order.hasNonNull("amount")) {
                BigDecimal base = conditional && order.hasNonNull("targetPrice")
                        ? new BigDecimal(order.get("targetPrice").asText())
                        : price;
                volume = new BigDecimal(order.get("amount").asText()).divide(base, 8, RoundingMode.HALF_UP);
            }
            if (volume == null || volume.signum() <= 0) {
                return "I couldn't work out how much to trade. Please give an amount or quantity.";
            }

            if (conditional) {
                BigDecimal target = new BigDecimal(order.get("targetPrice").asText());
                conditionalOrderService.create(userId,
                        new ConditionalOrderRequest(symbol, side, target, volume));
                return String.format("✅ Conditional order set: **%s %s %s** when the price reaches **$%s**.",
                        side, volume.toPlainString(), symbol, target.toPlainString());
            }

            OrderResponse result = tradingService.execute(userId, new OrderRequest(symbol, side, volume));
            return String.format("✅ **%s %s %s** executed for **$%s**. Cash balance: **$%s**.",
                    side, result.volume().toPlainString(), symbol,
                    result.totalValue().toPlainString(), result.cashBalance().toPlainString());
        } catch (Exception ex) {
            log.warn("AI order placement failed: {}", ex.toString());
            return "I couldn't place that order: " + ex.getMessage();
        }
    }

    private AiQueryResponse fallback(String username, Long userId, AiQueryRequest request, Throwable throwable) {
        log.warn("Gemini call failed for user {}: {}", username, throwable.toString());
        throw new LlmUnavailableException(
                "The AI assistant is temporarily unavailable. Please try again in a moment.");
    }
}
