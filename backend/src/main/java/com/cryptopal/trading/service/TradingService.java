package com.cryptopal.trading.service;

import com.cryptopal.auth.entity.Wallet;
import com.cryptopal.auth.repository.WalletRepository;
import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.trading.dto.HoldingView;
import com.cryptopal.trading.dto.OrderRequest;
import com.cryptopal.trading.dto.OrderResponse;
import com.cryptopal.trading.dto.PortfolioResponse;
import com.cryptopal.trading.dto.TransactionView;
import com.cryptopal.trading.entity.CashTransaction;
import com.cryptopal.trading.entity.Holding;
import com.cryptopal.trading.entity.Transaction;
import com.cryptopal.trading.exception.InsufficientFundsException;
import com.cryptopal.trading.exception.InsufficientHoldingsException;
import com.cryptopal.trading.exception.UnknownAssetException;
import com.cryptopal.trading.model.OrderSide;
import com.cryptopal.trading.repository.CashTransactionRepository;
import com.cryptopal.trading.repository.HoldingRepository;
import com.cryptopal.trading.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradingService {

    private final WalletRepository walletRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final MarketPriceCache marketPriceCache;

    public TradingService(WalletRepository walletRepository,
                          HoldingRepository holdingRepository,
                          TransactionRepository transactionRepository,
                          CashTransactionRepository cashTransactionRepository,
                          MarketPriceCache marketPriceCache) {
        this.walletRepository = walletRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.cashTransactionRepository = cashTransactionRepository;
        this.marketPriceCache = marketPriceCache;
    }

    @Transactional
    public OrderResponse execute(Long userId, OrderRequest request) {
        return switch (request.side()) {
            case BUY -> buy(userId, request);
            case SELL -> sell(userId, request);
        };
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

    private OrderResponse sell(Long userId, OrderRequest request) {
        String symbol = request.symbol().toUpperCase();
        BigDecimal price = currentPrice(symbol);

        Holding holding = holdingRepository.findByUserIdAndAssetSymbol(userId, symbol)
                .orElseThrow(() -> new InsufficientHoldingsException(symbol));
        if (holding.getVolume().compareTo(request.volume()) < 0) {
            throw new InsufficientHoldingsException(symbol);
        }

        BigDecimal proceeds = price.multiply(request.volume()).setScale(2, RoundingMode.HALF_UP);
        holding.setVolume(holding.getVolume().subtract(request.volume()));
        holdingRepository.save(holding);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
        wallet.setCashBalance(wallet.getCashBalance().add(proceeds));
        walletRepository.save(wallet);

        transactionRepository.save(
                new Transaction(userId, OrderSide.SELL.name(), symbol, request.volume(), price, proceeds));

        return new OrderResponse(symbol, OrderSide.SELL, request.volume(), price, proceeds, wallet.getCashBalance());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<HoldingView> holdings = holdingRepository.findByUserId(userId).stream()
                .filter(holding -> holding.getVolume().compareTo(BigDecimal.ZERO) > 0)
                .map(holding -> toHoldingView(holding, transactions))
                .sorted(Comparator.comparing(HoldingView::symbol))
                .toList();

        BigDecimal holdingsValue = holdings.stream()
                .map(HoldingView::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfitLoss = holdings.stream()
                .map(HoldingView::profitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioResponse(wallet.getCashBalance(), holdingsValue, totalProfitLoss, holdings);
    }

    @Transactional(readOnly = true)
    public List<TransactionView> getTransactions(Long userId) {
        List<TransactionView> all = new ArrayList<>();
        for (Transaction transaction : transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            all.add(new TransactionView(
                    transaction.getType(),
                    transaction.getAssetSymbol(),
                    transaction.getVolume(),
                    transaction.getPrice(),
                    transaction.getTotalValue(),
                    transaction.getCreatedAt()));
        }
        for (CashTransaction cash : cashTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId)) {
            all.add(new TransactionView(
                    cash.getType(),
                    cash.getCounterparty(),
                    null,
                    null,
                    cash.getAmount(),
                    cash.getCreatedAt()));
        }
        all.sort(Comparator.comparing(TransactionView::createdAt).reversed());
        return all;
    }

    private HoldingView toHoldingView(Holding holding, List<Transaction> transactions) {
        String symbol = holding.getAssetSymbol();
        BigDecimal price = marketPriceCache.getPrice(symbol).orElse(BigDecimal.ZERO);
        BigDecimal value = price.multiply(holding.getVolume()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal boughtVolume = BigDecimal.ZERO;
        BigDecimal boughtCost = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getAssetSymbol().equals(symbol) && "BUY".equals(transaction.getType())) {
                boughtVolume = boughtVolume.add(transaction.getVolume());
                boughtCost = boughtCost.add(transaction.getTotalValue());
            }
        }

        BigDecimal avgBuyPrice = boughtVolume.signum() > 0
                ? boughtCost.divide(boughtVolume, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal costBasis = avgBuyPrice.multiply(holding.getVolume()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal profitLoss = value.subtract(costBasis);
        BigDecimal profitLossPct = costBasis.signum() > 0
                ? profitLoss.multiply(BigDecimal.valueOf(100)).divide(costBasis, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new HoldingView(symbol, holding.getVolume(), price, value, avgBuyPrice, profitLoss, profitLossPct);
    }

    private BigDecimal currentPrice(String symbol) {
        return marketPriceCache.getPrice(symbol).orElseThrow(() -> new UnknownAssetException(symbol));
    }
}
