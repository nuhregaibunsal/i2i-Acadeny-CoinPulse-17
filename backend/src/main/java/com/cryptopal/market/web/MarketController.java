package com.cryptopal.market.web;

import com.cryptopal.market.cache.MarketPriceCache;
import com.cryptopal.market.dto.PricePoint;
import com.cryptopal.market.entity.PriceSnapshot;
import com.cryptopal.market.model.CryptoPrice;
import com.cryptopal.market.repository.PriceSnapshotRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketPriceCache marketPriceCache;
    private final PriceSnapshotRepository priceSnapshotRepository;

    public MarketController(MarketPriceCache marketPriceCache,
                            PriceSnapshotRepository priceSnapshotRepository) {
        this.marketPriceCache = marketPriceCache;
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    @GetMapping("/prices")
    public ResponseEntity<List<CryptoPrice>> prices() {
        return ResponseEntity.ok(marketPriceCache.getAll());
    }

    @GetMapping("/prices/{symbol}/history")
    public ResponseEntity<List<PricePoint>> history(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String range) {
        OffsetDateTime cutoff = switch (range) {
            case "1h" -> OffsetDateTime.now().minusHours(1);
            case "1w" -> OffsetDateTime.now().minusWeeks(1);
            default -> OffsetDateTime.now().minusDays(1);
        };
        List<PriceSnapshot> snapshots = priceSnapshotRepository
                .findByAssetSymbolAndRecordedAtGreaterThanEqualOrderByRecordedAtAsc(symbol.toUpperCase(), cutoff);

        int maxPoints = 120;
        int step = Math.max(1, snapshots.size() / maxPoints);
        List<PricePoint> series = new ArrayList<>();
        for (int i = 0; i < snapshots.size(); i += step) {
            PriceSnapshot snapshot = snapshots.get(i);
            series.add(new PricePoint(snapshot.getPrice(), snapshot.getRecordedAt().toInstant().toEpochMilli()));
        }
        return ResponseEntity.ok(series);
    }
}
