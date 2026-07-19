package com.cryptopal.market.provider;

import com.cryptopal.market.model.CryptoPrice;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "data-provider.profile", havingValue = "live")
public class LiveApiPriceProvider implements PriceDataProvider {

    private static final Logger log = LoggerFactory.getLogger(LiveApiPriceProvider.class);

    private static final Map<String, String> SYMBOL_TO_ID = new LinkedHashMap<>();

    static {
        SYMBOL_TO_ID.put("BTC", "bitcoin");
        SYMBOL_TO_ID.put("ETH", "ethereum");
        SYMBOL_TO_ID.put("SOL", "solana");
        SYMBOL_TO_ID.put("BNB", "binancecoin");
        SYMBOL_TO_ID.put("AVAX", "avalanche-2");
    }

    private final RestClient restClient;

    public LiveApiPriceProvider(
            @Value("${data-provider.live-url:https://api.coingecko.com/api/v3}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public List<CryptoPrice> currentPrices() {
        try {
            String ids = String.join(",", SYMBOL_TO_ID.values());
            Map<String, Map<String, BigDecimal>> response = restClient.get()
                    .uri(builder -> builder.path("/simple/price")
                            .queryParam("ids", ids)
                            .queryParam("vs_currencies", "usd")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null) {
                return List.of();
            }

            List<CryptoPrice> prices = new ArrayList<>();
            SYMBOL_TO_ID.forEach((symbol, id) -> {
                Map<String, BigDecimal> entry = response.get(id);
                if (entry != null && entry.get("usd") != null) {
                    prices.add(new CryptoPrice(symbol, entry.get("usd").setScale(2, RoundingMode.HALF_UP)));
                }
            });
            prices.sort((a, b) -> a.symbol().compareTo(b.symbol()));
            return prices;
        } catch (Exception ex) {
            log.warn("Live price fetch failed: {}", ex.getMessage());
            return List.of();
        }
    }
}
