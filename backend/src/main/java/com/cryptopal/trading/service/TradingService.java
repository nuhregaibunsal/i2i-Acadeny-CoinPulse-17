package com.cryptopal.trading.service;

import com.cryptopal.auth.entity.Wallet;
import com.cryptopal.auth.repository.WalletRepository;
import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.trading.dto.OrderRequest;
import com.cryptopal.trading.dto.OrderResponse;
import com.cryptopal.trading.entity.Holding;
import com.cryptopal.trading.entity.Transaction;
import com.cryptopal.trading.exception.InsufficientFundsException;
import com.cryptopal.trading.exception.UnknownAssetException;
import com.cryptopal.trading.model.OrderSide;
import com.cryptopal.trading.repository.HoldingRepository;
import com.cryptopal.trading.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradingService {

    private final WalletRepository walletRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final MarketPriceCache marketPriceCache;

    public TradingService(WalletRepository walletRepository,
                          HoldingRepository holdingRepository,
                          TransactionRepository transactionRepository,
                          MarketPriceCache marketPriceCache) {
        this.walletRepository = walletRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.marketPriceCache = marketPriceCache;
    }

    @Transactional
    public OrderResponse execute(Long userId, OrderRequest request) {
        if (request.side() == OrderSide.BUY) {
            return buy(userId, request);
        }
        throw new UnsupportedOperationException("SELL not implemented yet");
    }

    private OrderResponse buy(Long userId, OrderRequest request) {
        String symbol = request.symbol().toUpperCase();
        BigDecimal price = currentPrice(symbol);
        BigDecimal cost = price.multiply(request.volume()).setScale(2, RoundingMode.HALF_UP);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
        if (wallet.getCashBalance().compareTo(cost) < 0) {
            throw new InsufficientFundsException();
        }
        wallet.setCashBalance(wallet.getCashBalance().subtract(cost));
        walletRepository.save(wallet);

        Holding holding = holdingRepository.findByUserIdAndAssetSymbol(userId, symbol)
                .orElseGet(() -> new Holding(userId, symbol, BigDecimal.ZERO));
        holding.setVolume(holding.getVolume().add(request.volume()));
        holdingRepository.save(holding);

        transactionRepository.save(
                new Transaction(userId, OrderSide.BUY.name(), symbol, request.volume(), price, cost));

        return new OrderResponse(symbol, OrderSide.BUY, request.volume(), price, cost, wallet.getCashBalance());
    }

    private BigDecimal currentPrice(String symbol) {
        return marketPriceCache.getPrice(symbol).orElseThrow(() -> new UnknownAssetException(symbol));
    }
}
