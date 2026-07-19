package com.cryptopal.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cryptopal.ai.client.GeminiClient;
import com.cryptopal.ai.dto.AiQueryRequest;
import com.cryptopal.ai.dto.AiQueryResponse;
import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.trading.service.ConditionalOrderService;
import com.cryptopal.trading.service.TradingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AiInsightServiceTest {

    private final AiContextBuilder contextBuilder = Mockito.mock(AiContextBuilder.class);
    private final GeminiClient geminiClient = Mockito.mock(GeminiClient.class);
    private final TradingService tradingService = Mockito.mock(TradingService.class);
    private final ConditionalOrderService conditionalOrderService = Mockito.mock(ConditionalOrderService.class);
    private final MarketPriceCache marketPriceCache = Mockito.mock(MarketPriceCache.class);
    private final AiInsightService aiInsightService = new AiInsightService(
            contextBuilder, geminiClient, tradingService, conditionalOrderService, marketPriceCache);

    @Test
    void answer_buildsContextAndReturnsGeneratedText() {
        when(contextBuilder.build("nuh", 1L, "What do I hold?")).thenReturn("PROMPT");
        when(geminiClient.generate("PROMPT")).thenReturn("You hold 0.05 BTC.");

        AiQueryResponse response = aiInsightService.answer("nuh", 1L, new AiQueryRequest("What do I hold?"));

        assertThat(response.answer()).isEqualTo("You hold 0.05 BTC.");
        verify(geminiClient).generate("PROMPT");
    }
}
