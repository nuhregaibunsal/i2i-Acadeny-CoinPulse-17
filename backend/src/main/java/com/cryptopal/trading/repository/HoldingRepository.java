package com.cryptopal.trading.repository;

import com.cryptopal.trading.entity.Holding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserIdAndAssetSymbol(Long userId, String assetSymbol);

    List<Holding> findByUserId(Long userId);
}
