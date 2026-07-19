package com.cryptopal.trading.repository;

import com.cryptopal.trading.entity.PendingOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingOrderRepository extends JpaRepository<PendingOrder, Long> {

    List<PendingOrder> findByStatus(String status);

    List<PendingOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PendingOrder> findByIdAndUserId(Long id, Long userId);
}
