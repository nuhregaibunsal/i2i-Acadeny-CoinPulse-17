package com.cryptopal.trading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cryptopal.auth.entity.Wallet;
import com.cryptopal.auth.repository.WalletRepository;
import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.trading.dto.OrderRequest;
import com.cryptopal.trading.dto.OrderResponse;
import com.cryptopal.trading.entity.Holding;
import com.cryptopal.trading.entity.Transaction;
import com.cryptopal.trading.exception.InsufficientFundsException;
import com.cryptopal.trading.exception.InsufficientHoldingsException;
import com.cryptopal.trading.exception.UnknownAssetException;
import com.cryptopal.trading.model.OrderSide;
import com.cryptopal.trading.repository.HoldingRepository;
import com.cryptopal.trading.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TradingServiceTest {

    private WalletRepository walletRepository;
    private HoldingRepository holdingRepository;
    private TransactionRepository transactionRepository;
    private MarketPriceCache marketPriceCache;
    private TradingService tradingService;

    @BeforeEach
    void setUp() {
        walletRepository = Mockito.mock(WalletRepository.class);
        holdingRepository = Mockito.mock(HoldingRepository.class);
        transactionRepository = Mockito.mock(TransactionRepository.class);
        marketPriceCache = Mockito.mock(MarketPriceCache.class);
        tradingService = new TradingService(
                walletRepository, holdingRepository, transactionRepository, marketPriceCache);
    }

    @Test
    void buy_deductsCashAddsHoldingAndLogsTransaction() {
        when(marketPriceCache.getPrice("BTC")).thenReturn(Optional.of(new BigDecimal("100.00")));
        Wallet wallet = new Wallet(1L, new BigDecimal("10000.00"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(holdingRepository.findByUserIdAndAssetSymbol(1L, "BTC")).thenReturn(Optional.empty());

        OrderResponse response = tradingService.execute(1L,
                new OrderRequest("BTC", OrderSide.BUY, new BigDecimal("2")));

        assertThat(response.totalValue()).isEqualByComparingTo("200.00");
        assertThat(response.cashBalance()).isEqualByComparingTo("9800.00");
        assertThat(wallet.getCashBalance()).isEqualByComparingTo("9800.00");
        verify(holdingRepository).save(any(Holding.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void buy_insufficientFunds_throwsAndWritesNothing() {
        when(marketPriceCache.getPrice("BTC")).thenReturn(Optional.of(new BigDecimal("100000.00")));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(new Wallet(1L, new BigDecimal("100.00"))));

        assertThatThrownBy(() -> tradingService.execute(1L,
                new OrderRequest("BTC", OrderSide.BUY, new BigDecimal("1"))))
                .isInstanceOf(InsufficientFundsException.class);

        verify(walletRepository, never()).save(any());
        verify(holdingRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buy_unknownAsset_throws() {
        when(marketPriceCache.getPrice("DOGE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradingService.execute(1L,
                new OrderRequest("DOGE", OrderSide.BUY, new BigDecimal("1"))))
                .isInstanceOf(UnknownAssetException.class);
    }

    @Test
    void sell_reducesHoldingAndCreditsCash() {
        when(marketPriceCache.getPrice("BTC")).thenReturn(Optional.of(new BigDecimal("100.00")));
        Holding holding = new Holding(1L, "BTC", new BigDecimal("5"));
        when(holdingRepository.findByUserIdAndAssetSymbol(1L, "BTC")).thenReturn(Optional.of(holding));
        Wallet wallet = new Wallet(1L, new BigDecimal("1000.00"));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        OrderResponse response = tradingService.execute(1L,
                new OrderRequest("BTC", OrderSide.SELL, new BigDecimal("2")));

        assertThat(response.cashBalance()).isEqualByComparingTo("1200.00");
        assertThat(holding.getVolume()).isEqualByComparingTo("3");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void sell_insufficientHoldings_throwsAndWritesNothing() {
        when(marketPriceCache.getPrice("BTC")).thenReturn(Optional.of(new BigDecimal("100.00")));
        when(holdingRepository.findByUserIdAndAssetSymbol(1L, "BTC"))
                .thenReturn(Optional.of(new Holding(1L, "BTC", new BigDecimal("1"))));

        assertThatThrownBy(() -> tradingService.execute(1L,
                new OrderRequest("BTC", OrderSide.SELL, new BigDecimal("5"))))
                .isInstanceOf(InsufficientHoldingsException.class);

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
