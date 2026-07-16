package com.cryptopal.market.web;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.market.model.CryptoPrice;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketPriceCache marketPriceCache;

    public MarketController(MarketPriceCache marketPriceCache) {
        this.marketPriceCache = marketPriceCache;
    }

    @GetMapping("/prices")
    public ResponseEntity<List<CryptoPrice>> prices() {
        return ResponseEntity.ok(marketPriceCache.getAll());
    }
}
