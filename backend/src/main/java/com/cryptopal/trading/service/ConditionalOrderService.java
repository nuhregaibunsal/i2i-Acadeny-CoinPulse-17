package com.cryptopal.trading.service;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.trading.dto.ConditionalOrderRequest;
import com.cryptopal.trading.dto.ConditionalOrderView;
import com.cryptopal.trading.dto.OrderRequest;
import com.cryptopal.trading.entity.PendingOrder;
import com.cryptopal.trading.exception.UnknownAssetException;
import com.cryptopal.trading.model.OrderSide;
import com.cryptopal.trading.repository.PendingOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConditionalOrderService {

    private static final Logger log = LoggerFactory.getLogger(ConditionalOrderService.class);

    private final PendingOrderRepository pendingOrderRepository;
    private final MarketPriceCache marketPriceCache;
    private final TradingService tradingService;

    public ConditionalOrderService(PendingOrderRepository pendingOrderRepository,
                                   MarketPriceCache marketPriceCache,
                                   TradingService tradingService) {
        this.pendingOrderRepository = pendingOrderRepository;
        this.marketPriceCache = marketPriceCache;
        this.tradingService = tradingService;
    }

    @Transactional
    public ConditionalOrderView create(Long userId, ConditionalOrderRequest request) {
        String symbol = request.symbol().toUpperCase();
        BigDecimal current = marketPriceCache.getPrice(symbol)
                .orElseThrow(() -> new UnknownAssetException(symbol));

        String direction = request.targetPrice().compareTo(current) <= 0 ? "BELOW" : "ABOVE";
        PendingOrder order = new PendingOrder(userId, request.side().name(), symbol, direction,
                request.targetPrice(), request.volume(), "PENDING");
        return toView(pendingOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<ConditionalOrderView> list(Long userId) {
        return pendingOrderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public void cancel(Long userId, Long orderId) {
        pendingOrderRepository.findByIdAndUserId(orderId, userId).ifPresent(order -> {
            if ("PENDING".equals(order.getStatus())) {
                order.setStatus("CANCELLED");
                pendingOrderRepository.save(order);
            }
        });
    }

    @Scheduled(fixedRate = 3000)
    @Transactional
    public void processPendingOrders() {
        for (PendingOrder order : pendingOrderRepository.findByStatus("PENDING")) {
            BigDecimal price = marketPriceCache.getPrice(order.getAssetSymbol()).orElse(null);
            if (price == null) {
                continue;
            }
            boolean triggered = "BELOW".equals(order.getDirection())
                    ? price.compareTo(order.getTargetPrice()) <= 0
                    : price.compareTo(order.getTargetPrice()) >= 0;
            if (!triggered) {
                continue;
            }
            try {
                tradingService.execute(order.getUserId(), new OrderRequest(
                        order.getAssetSymbol(), OrderSide.valueOf(order.getType()), order.getVolume()));
                order.setStatus("FILLED");
            } catch (RuntimeException ex) {
                log.warn("Conditional order {} failed: {}", order.getId(), ex.getMessage());
                order.setStatus("FAILED");
            }
            pendingOrderRepository.save(order);
        }
    }

    private ConditionalOrderView toView(PendingOrder order) {
        return new ConditionalOrderView(order.getId(), order.getAssetSymbol(), order.getType(),
                order.getDirection(), order.getTargetPrice(), order.getVolume(), order.getStatus(),
                order.getCreatedAt());
    }
}
