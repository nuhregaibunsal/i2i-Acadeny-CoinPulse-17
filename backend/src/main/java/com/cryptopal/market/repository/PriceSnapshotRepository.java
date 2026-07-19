package com.cryptopal.market.repository;

import com.cryptopal.market.entity.PriceSnapshot;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    List<PriceSnapshot> findTop20ByAssetSymbolOrderByRecordedAtDesc(String assetSymbol);

    List<PriceSnapshot> findByAssetSymbolAndRecordedAtGreaterThanEqualOrderByRecordedAtAsc(
            String assetSymbol, OffsetDateTime cutoff);
}
