package com.cryptopal.market.scheduler;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.market.entity.PriceSnapshot;
import com.cryptopal.market.model.CryptoPrice;
import com.cryptopal.market.provider.PriceDataProvider;
import com.cryptopal.market.repository.PriceSnapshotRepository;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarketScheduler {

    private final PriceDataProvider priceDataProvider;
    private final MarketPriceCache marketPriceCache;
    private final PriceSnapshotRepository priceSnapshotRepository;

    public MarketScheduler(PriceDataProvider priceDataProvider,
                           MarketPriceCache marketPriceCache,
                           PriceSnapshotRepository priceSnapshotRepository) {
        this.priceDataProvider = priceDataProvider;
        this.marketPriceCache = marketPriceCache;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    @Scheduled(fixedRate = 2000)
    public void refreshCache() {
        marketPriceCache.putAll(priceDataProvider.currentPrices());
    }

    @Scheduled(fixedRate = 60000)
    public void persistSnapshots() {
        List<CryptoPrice> prices = marketPriceCache.getAll();
        for (CryptoPrice price : prices) {
            priceSnapshotRepository.save(new PriceSnapshot(price.symbol(), price.price()));
        }
    }
}
