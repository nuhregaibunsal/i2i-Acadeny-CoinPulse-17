package com.cryptopal.market.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.cryptopal.market.model.CryptoPrice;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class TickerEnginePriceProviderTest {

    private final TickerEnginePriceProvider provider = new TickerEnginePriceProvider();

    @Test
    void currentPrices_returnsAllSeededSymbolsSorted() {
        List<CryptoPrice> prices = provider.currentPrices();

        assertThat(prices).hasSize(5);
        assertThat(prices).extracting(CryptoPrice::symbol)
                .containsExactly("AVAX", "BNB", "BTC", "ETH", "SOL");
    }

    @Test
    void fluctuate_movesPricesButKeepsThemPositive() {
        List<CryptoPrice> before = provider.currentPrices();

        for (int i = 0; i < 25; i++) {
            provider.fluctuate();
        }

        List<CryptoPrice> after = provider.currentPrices();
        assertThat(after).allSatisfy(price -> assertThat(price.price()).isGreaterThan(BigDecimal.ZERO));
        assertThat(after).isNotEqualTo(before);
    }
}
