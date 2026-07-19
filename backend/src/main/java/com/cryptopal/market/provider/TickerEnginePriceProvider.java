package com.cryptopal.market.provider;

import com.cryptopal.market.model.CryptoPrice;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "data-provider.profile", havingValue = "ticker", matchIfMissing = true)
public class TickerEnginePriceProvider implements PriceDataProvider {

    private static final Map<String, BigDecimal> SEED = Map.of(
            "BTC", new BigDecimal("65000.00"),
            "ETH", new BigDecimal("3500.00"),
            "SOL", new BigDecimal("150.00"),
            "BNB", new BigDecimal("600.00"),
            "AVAX", new BigDecimal("35.00"));

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    private final Map<String, BigDecimal> prices = new ConcurrentHashMap<>(SEED);

    @Override
    public List<CryptoPrice> currentPrices() {
        List<CryptoPrice> snapshot = new ArrayList<>();
        prices.forEach((symbol, price) -> snapshot.add(new CryptoPrice(symbol, price)));
        snapshot.sort((a, b) -> a.symbol().compareTo(b.symbol()));
        return snapshot;
    }

    @Scheduled(fixedRate = 1000)
    void fluctuate() {
        prices.replaceAll((symbol, price) -> nextPrice(price));
    }

    private BigDecimal nextPrice(BigDecimal current) {
        double changePercent = ThreadLocalRandom.current().nextGaussian() * 0.004;
        BigDecimal next = current.multiply(BigDecimal.valueOf(1 + changePercent));
        if (next.compareTo(MIN_PRICE) < 0) {
            next = MIN_PRICE;
        }
        return next.setScale(2, RoundingMode.HALF_UP);
    }
}
