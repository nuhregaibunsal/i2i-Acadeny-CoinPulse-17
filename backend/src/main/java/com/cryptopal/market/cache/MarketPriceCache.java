package com.cryptopal.market.cache;

import com.cryptopal.market.model.CryptoPrice;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MarketPriceCache {

    private static final String KEY = "market:prices";

    private final StringRedisTemplate redisTemplate;

    public MarketPriceCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void putAll(List<CryptoPrice> prices) {
        Map<String, String> entries = new HashMap<>();
        for (CryptoPrice price : prices) {
            entries.put(price.symbol(), price.price().toPlainString());
        }
        redisTemplate.opsForHash().putAll(KEY, entries);
    }

    public List<CryptoPrice> getAll() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(KEY);
        List<CryptoPrice> prices = new ArrayList<>();
        entries.forEach((symbol, price) ->
                prices.add(new CryptoPrice(symbol.toString(), new BigDecimal(price.toString()))));
        prices.sort((a, b) -> a.symbol().compareTo(b.symbol()));
        return prices;
    }
}
