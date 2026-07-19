package com.cryptopal.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.market.model.CryptoPrice;
import com.cryptopal.market.repository.PriceSnapshotRepository;
import com.cryptopal.trading.dto.HoldingView;
import com.cryptopal.trading.dto.PortfolioResponse;
import com.cryptopal.trading.entity.Transaction;
import com.cryptopal.trading.repository.TransactionRepository;
import com.cryptopal.trading.service.TradingService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AiContextBuilderTest {

    private TradingService tradingService;
    private TransactionRepository transactionRepository;
    private PriceSnapshotRepository priceSnapshotRepository;
    private MarketPriceCache marketPriceCache;
    private AiContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        tradingService = Mockito.mock(TradingService.class);
        transactionRepository = Mockito.mock(TransactionRepository.class);
        priceSnapshotRepository = Mockito.mock(PriceSnapshotRepository.class);
        marketPriceCache = Mockito.mock(MarketPriceCache.class);
        contextBuilder = new AiContextBuilder(tradingService, transactionRepository,
                priceSnapshotRepository, marketPriceCache);
    }

    @Test
    void build_includesPortfolioPricesTransactionsAndQuestion() {
        when(tradingService.getPortfolio(1L)).thenReturn(new PortfolioResponse(
                new BigDecimal("5000.00"),
                new BigDecimal("3200.00"),
                new BigDecimal("200.00"),
                List.of(new HoldingView("BTC", new BigDecimal("0.05"),
                        new BigDecimal("64000.00"), new BigDecimal("3200.00"),
                        new BigDecimal("60000.00"), new BigDecimal("200.00"), new BigDecimal("6.67")))));
        when(marketPriceCache.getAll()).thenReturn(List.of(new CryptoPrice("BTC", new BigDecimal("64000.00"))));

        Transaction transaction = Mockito.mock(Transaction.class);
        when(transaction.getType()).thenReturn("BUY");
        when(transaction.getVolume()).thenReturn(new BigDecimal("0.05"));
        when(transaction.getAssetSymbol()).thenReturn("BTC");
        when(transaction.getPrice()).thenReturn(new BigDecimal("64000.00"));
        when(transaction.getTotalValue()).thenReturn(new BigDecimal("3200.00"));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(transaction));
        when(priceSnapshotRepository.findTop20ByAssetSymbolOrderByRecordedAtDesc("BTC")).thenReturn(List.of());

        String prompt = contextBuilder.build("nuh", 1L, "What do I hold?");

        assertThat(prompt).contains("nuh");
        assertThat(prompt).contains("Cash balance: 5000.00");
        assertThat(prompt).contains("Holdings value: 3200.00");
        assertThat(prompt).contains("BTC");
        assertThat(prompt).contains("BUY");
        assertThat(prompt).contains("What do I hold?");
        assertThat(prompt).contains("not a licensed financial advisor");
    }

    @Test
    void build_handlesEmptyPortfolioAndNoTransactions() {
        when(tradingService.getPortfolio(2L)).thenReturn(
                new PortfolioResponse(new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO, List.of()));
        when(marketPriceCache.getAll()).thenReturn(List.of());
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());

        String prompt = contextBuilder.build("ayse", 2L, "Any trades?");

        assertThat(prompt).contains("No crypto holdings.");
        assertThat(prompt).contains("No transactions yet.");
        assertThat(prompt).contains("Any trades?");
    }
}
