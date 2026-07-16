package com.cryptopal.market.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.market.entity.PriceSnapshot;
import com.cryptopal.market.model.CryptoPrice;
import com.cryptopal.market.provider.PriceDataProvider;
import com.cryptopal.market.repository.PriceSnapshotRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class MarketSchedulerTest {

    private final PriceDataProvider priceDataProvider = Mockito.mock(PriceDataProvider.class);
    private final MarketPriceCache marketPriceCache = Mockito.mock(MarketPriceCache.class);
    private final PriceSnapshotRepository priceSnapshotRepository = Mockito.mock(PriceSnapshotRepository.class);
    private final MarketScheduler scheduler =
            new MarketScheduler(priceDataProvider, marketPriceCache, priceSnapshotRepository);

    @Test
    void refreshCache_writesProviderPricesToCache() {
        List<CryptoPrice> prices = List.of(new CryptoPrice("BTC", new BigDecimal("100.00")));
        when(priceDataProvider.currentPrices()).thenReturn(prices);

        scheduler.refreshCache();

        verify(marketPriceCache).putAll(prices);
    }

    @Test
    void persistSnapshots_savesEachCachedPrice() {
        when(marketPriceCache.getAll()).thenReturn(List.of(
                new CryptoPrice("BTC", new BigDecimal("100.00")),
                new CryptoPrice("ETH", new BigDecimal("50.00"))));

        scheduler.persistSnapshots();

        ArgumentCaptor<PriceSnapshot> captor = ArgumentCaptor.forClass(PriceSnapshot.class);
        verify(priceSnapshotRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(PriceSnapshot::getAssetSymbol)
                .containsExactly("BTC", "ETH");
    }
}
